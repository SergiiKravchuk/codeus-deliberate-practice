package org.codeus.fundamentals.event_triggers;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SqlQueriesTest {

    protected static EmbeddedPostgres postgres;
    protected static Connection connection;

    @BeforeAll
    static void beforeAll() throws IOException {
        startDatabase();
    }

    @BeforeEach
    void clearAllTables() throws SQLException {
        executeCUD("DELETE FROM ddl_audit_log;", false);
        executeCUD("DELETE FROM ddl_operations_stats;", false);
        executeCUD("DELETE FROM trigger_execution_stats;", false);
    }

    @AfterAll
    static void afterAll() throws IOException {
        stopDatabase();
    }

    static void startDatabase() throws IOException {
        postgres = EmbeddedPostgres.start();
        try {
            connection = postgres.getPostgresDatabase().getConnection();
            connection.setAutoCommit(false);
            executeSqlFile("schema.sql");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get database connection", e);
        }
    }

    static void stopDatabase() throws IOException {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }

        if (postgres != null) {
            postgres.close();
            System.out.println("PostgreSQL stopped successfully");
        }
    }

    @Test
    @Order(1)
    @DisplayName("1: DDL Audit Event Trigger")
    void ddlAuditEventTrigger() throws SQLException, IOException {
        System.out.println("1: DDL Audit Event Trigger\n");

        System.out.println("Execute `task/1-ddl-audit-event-trigger.sql` with task!\n");
        executeSqlFile("task/1-ddl-audit-event-trigger.sql");

        // Clear previous audit records
        executeCUD("DELETE FROM ddl_audit_log;", false);

        System.out.println("Test CREATE TABLE operation");
        executeCUD("CREATE TABLE audit_test_table (id SERIAL PRIMARY KEY, name VARCHAR(50));", true);

        List<Map<String, Object>> auditLogs =
                executeQuery("SELECT * FROM ddl_audit_log WHERE object_type = 'table' ORDER BY logged_at DESC LIMIT 1;", true);
        printQueryResults(auditLogs);

        assertEquals(1, auditLogs.size(), "Should have exactly 1 table record in audit log");
        Map<String, Object> logEntry = auditLogs.get(0);

        assertEquals("CREATE TABLE", logEntry.get("command_tag").toString(), "Command tag should be CREATE TABLE");
        assertEquals("table", logEntry.get("object_type").toString(), "Object type should be table");
        assertTrue(logEntry.get("object_name").toString().contains("audit_test_table"), "Object name should contain audit_test_table");

        System.out.println("\nTest ALTER TABLE operation");
        executeCUD("ALTER TABLE audit_test_table ADD COLUMN description TEXT;", true);

        auditLogs = executeQuery("SELECT * FROM ddl_audit_log ORDER BY logged_at DESC LIMIT 1;", true);
        printQueryResults(auditLogs);

        logEntry = auditLogs.get(0);
        assertEquals("ALTER TABLE", logEntry.get("command_tag").toString(), "Command tag should be ALTER TABLE");

        System.out.println("\nTest CREATE INDEX operation");
        executeCUD("CREATE INDEX idx_audit_test_name ON audit_test_table(name);", true);

        auditLogs = executeQuery("SELECT * FROM ddl_audit_log ORDER BY logged_at DESC LIMIT 1;", true);
        printQueryResults(auditLogs);

        logEntry = auditLogs.get(0);
        assertEquals("CREATE INDEX", logEntry.get("command_tag").toString(), "Command tag should be CREATE INDEX");
        assertEquals("index", logEntry.get("object_type").toString(), "Object type should be index");

        System.out.println("=========================================================================================================================================================================\n");
    }

    @Test
    @Order(2)
    @DisplayName("2: DDL Prevent DROP Operations Event Trigger")
    void preventDropOperations() throws SQLException, IOException {
        System.out.println("2: DDL Prevent DROP Operations Event Trigger\n");

        System.out.println("Execute `task/2-prevent-drop-event-trigger.sql` with task!\n");
        executeSqlFile("task/2-prevent-drop-event-trigger.sql");

        // Create a table to test with
        System.out.println("Creating test table for DROP prevention test");
        executeCUD("CREATE TABLE drop_prevention_test (id SERIAL, data TEXT);", true);

        // Verify table exists
        List<Map<String, Object>> tables = executeQuery(
                "SELECT table_name FROM information_schema.tables WHERE table_name = 'drop_prevention_test' AND table_schema = 'public';",
                true
        );
        assertEquals(1, tables.size(), "drop_prevention_test should exist");

        System.out.println("\nTest 1: DROP TABLE prevention");
        SQLException tableException = assertThrows(SQLException.class, () -> {
            executeCUD("DROP TABLE drop_prevention_test;", true);
        });

        System.out.println("Expected exception caught: " + tableException.getMessage());
        assertTrue(tableException.getMessage().contains("DROP TABLE is forbidden"),
                "Exception should mention DROP TABLE being forbidden");

        // Verify table still exists after failed DROP
        tables = executeQuery(
                "SELECT table_name FROM information_schema.tables WHERE table_name = 'drop_prevention_test' AND table_schema = 'public';",
                true
        );
        assertEquals(1, tables.size(), "drop_prevention_test should still exist after failed DROP");

        System.out.println("\nTest 2: DROP INDEX prevention");
        executeCUD("CREATE INDEX test_prevention_idx ON drop_prevention_test(id);", false);

        SQLException indexException = assertThrows(SQLException.class, () -> {
            executeCUD("DROP INDEX test_prevention_idx;", true);
        });

        System.out.println("Expected exception caught: " + indexException.getMessage());
        assertTrue(indexException.getMessage().contains("DROP INDEX is forbidden"),
                "Exception should mention DROP INDEX being forbidden");

        System.out.println("\nTest 3: Allow creation operations (should work normally)");
        executeCUD("CREATE TABLE allowed_table (id SERIAL);", true);

        List<Map<String, Object>> allowedTables = executeQuery(
                "SELECT table_name FROM information_schema.tables WHERE table_name = 'allowed_table' AND table_schema = 'public';",
                true
        );
        assertEquals(1, allowedTables.size(), "CREATE operations should still work");

        System.out.println("=========================================================================================================================================================================\n");
    }

    @Test
    @Order(3)
    @DisplayName("3: DDL Access Control Event Trigger")
    void simpleDdlAccessControl() throws SQLException, IOException {
        System.out.println("3: DDL Access Control Event Trigger\n");

        System.out.println("Execute `task/3-ddl-access-control-event-trigger.sql` with task!\n");
        executeSqlFile("task/3-ddl-access-control-event-trigger.sql");

        // Clear previous logs
        executeCUD("DELETE FROM ddl_audit_log;", false);

        System.out.println("Test 1: Normal DDL operation (should succeed for default user)");
        executeCUD("CREATE TABLE access_control_test (id SERIAL PRIMARY KEY, data TEXT);", true);

        List<Map<String, Object>> tables = executeQuery(
                "SELECT table_name FROM information_schema.tables WHERE table_name = 'access_control_test' AND table_schema = 'public';",
                true
        );
        assertEquals(1, tables.size(), "access_control_test table should be created successfully");

        System.out.println("\nTest 2: Check that access control mechanism is in place");
        // Test that the function exists and time checking works

        List<Map<String, Object>> timeTest = executeQuery("SELECT EXTRACT(hour FROM CURRENT_TIME) as current_hour;", true);
        printQueryResults(timeTest);

        assertFalse(timeTest.isEmpty(), "Should be able to extract current hour");

        // Verify the control function exists
        List<Map<String, Object>> functionTest = executeQuery(
                "SELECT proname FROM pg_proc WHERE proname = 'control_ddl_access';", true);
        assertEquals(1, functionTest.size(), "Access control function should exist");

        System.out.println("\nAccess control mechanism is in place and ready to block unauthorized operations");

        System.out.println("=========================================================================================================================================================================\n");
    }

    @Test
    @Order(4)
    @DisplayName("4: DDL Operations Counter Event Trigger")
    void ddlOperationsCounter() throws SQLException, IOException {
        System.out.println("4: DDL Operations Counter Event Trigger\n");

        System.out.println("Execute `task/4-ddl-operations-counter-event-trigger.sql` with task!\n");
        executeSqlFile("task/4-ddl-operations-counter-event-trigger.sql");

        // Clear previous stats
        executeCUD("DELETE FROM ddl_operations_stats;", false);

        System.out.println("Perform DDL operations to test counter");
        executeCUD("CREATE TABLE counter_test1 (id SERIAL PRIMARY KEY);", true);
        executeCUD("CREATE TABLE counter_test2 (id SERIAL PRIMARY KEY, name TEXT);", true);
        executeCUD("ALTER TABLE counter_test1 ADD COLUMN description TEXT;", true);

        List<Map<String, Object>> stats = executeQuery("SELECT * FROM ddl_operations_stats ORDER BY command_tag;", true);
        printQueryResults(stats);

        assertTrue(stats.size() >= 2, "Should have at least 2 different operation types");

        // Check CREATE TABLE counter
        boolean hasCreateTable = stats.stream().anyMatch(stat ->
                "CREATE TABLE".equals(stat.get("command_tag").toString()) &&
                        Integer.parseInt(stat.get("operation_count").toString()) >= 2
        );
        assertTrue(hasCreateTable, "Should have CREATE TABLE operations counted");

        // Check ALTER TABLE counter
        boolean hasAlterTable = stats.stream().anyMatch(stat ->
                "ALTER TABLE".equals(stat.get("command_tag").toString()) &&
                        Integer.parseInt(stat.get("operation_count").toString()) >= 1
        );
        assertTrue(hasAlterTable, "Should have ALTER TABLE operations counted");

        System.out.println("\nTest counter increment by performing same operation again");
        executeCUD("CREATE TABLE counter_test3 (id SERIAL);", true);

        List<Map<String, Object>> updatedStats = executeQuery(
                "SELECT * FROM ddl_operations_stats WHERE command_tag = 'CREATE TABLE';", true);
        printQueryResults(updatedStats);

        assertTrue(Integer.parseInt(updatedStats.get(0).get("operation_count").toString()) >= 3,
                "CREATE TABLE counter should be incremented");

        System.out.println("=========================================================================================================================================================================\n");
    }

    @Test
    @Order(5)
    @DisplayName("5: Management Monitoring Event Trigger")
    void simpleEventTriggerManagement() throws SQLException, IOException {
        System.out.println("5: Management Monitoring Event Trigger\n");

        System.out.println("Execute `task/5-management-monitoring-event-trigger.sql` with task!\n");
        executeSqlFile("task/5-management-monitoring-event-trigger.sql");

        // Clear previous activity logs
        executeCUD("DELETE FROM trigger_execution_stats;", false);

        System.out.println("Test 1: Track trigger activity");
        executeCUD("CREATE TABLE management_test (id SERIAL PRIMARY KEY);", true);

        List<Map<String, Object>> activityLogs = executeQuery("SELECT * FROM trigger_execution_stats ORDER BY execution_time DESC;", true);
        printQueryResults(activityLogs);

        assertTrue(activityLogs.size() >= 1, "Should have trigger activity logged");
        assertEquals("activity_logger", activityLogs.get(0).get("trigger_name").toString(), "Should log activity_logger trigger");
        assertEquals("CREATE TABLE", activityLogs.get(0).get("command_tag").toString(), "Should log CREATE TABLE command");

        System.out.println("\nTest 2: Disable event trigger");
        List<Map<String, Object>> disableResult = executeQuery("SELECT disable_event_trigger('ddl_audit_trigger') as result;", true);
        printQueryResults(disableResult);

        String disableMessage = disableResult.get(0).get("result").toString();
        assertTrue(disableMessage.contains("disabled successfully"), "Should successfully disable trigger");

        // Verify trigger is disabled
        List<Map<String, Object>> triggerStatus = executeQuery(
                "SELECT evtname, evtenabled FROM pg_event_trigger WHERE evtname = 'ddl_audit_trigger';", true);
        printQueryResults(triggerStatus);

        if (!triggerStatus.isEmpty()) {
            assertEquals("D", triggerStatus.get(0).get("evtenabled").toString(), "Trigger should be disabled");
        }

        System.out.println("\nTest 3: Enable event trigger");
        List<Map<String, Object>> enableResult = executeQuery("SELECT enable_event_trigger('ddl_audit_trigger') as result;", true);
        printQueryResults(enableResult);

        String enableMessage = enableResult.get(0).get("result").toString();
        assertTrue(enableMessage.contains("enabled successfully"), "Should successfully enable trigger");

        // Verify trigger is enabled
        triggerStatus = executeQuery(
                "SELECT evtname, evtenabled FROM pg_event_trigger WHERE evtname = 'ddl_audit_trigger';", true);
        printQueryResults(triggerStatus);

        if (!triggerStatus.isEmpty()) {
            assertEquals("O", triggerStatus.get(0).get("evtenabled").toString(), "Trigger should be enabled");
        }

        System.out.println("=========================================================================================================================================================================\n");
    }

    private static void executeSqlFile(String filePath) throws IOException, SQLException {
        String sql = getSqlScriptContent(filePath);
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            connection.rollback();
            throw e;
        }
    }

    private static String getSqlScriptContent(String filePath) throws IOException {
        return Files.readString(Paths.get("src/test/resources/" + filePath));
    }

    private static List<Map<String, Object>> executeQuery(String sql, boolean printQuery) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();

        if (printQuery) {
            System.out.println(sql);
        }

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            List<String> columnNames = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                columnNames.add(metaData.getColumnLabel(i));
            }

            while (resultSet.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(columnNames.get(i - 1), resultSet.getObject(i));
                }
                results.add(row);
            }
        }

        return results;
    }

    private static void executeCUD(String sql, boolean printQuery) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            if (printQuery) {
                System.out.println(sql);
            }
            statement.execute(sql);
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    private static void printQueryResults(List<Map<String, Object>> results) {
        if (results.isEmpty()) {
            System.out.println("No results found.");
            return;
        }

        List<String> columnNames = new ArrayList<>(results.get(0).keySet());
        int[] columnWidths = calculateColumnWidths(results, columnNames);

        var wholeLen = 1;
        for (int columnWidth : columnWidths) {
            wholeLen += columnWidth + 3;
        }
        System.out.println("_".repeat(wholeLen));
        printRow(columnNames, columnWidths);
        printSeparator(columnWidths);

        for (Map<String, Object> row : results) {
            List<String> values = new ArrayList<>();
            for (String col : columnNames) {
                Object value = row.get(col);
                values.add(value == null ? "NULL" : value.toString());
            }
            printRow(values, columnWidths);
        }

        System.out.println("_".repeat(wholeLen) + "\n");
    }

    private static int[] calculateColumnWidths(List<Map<String, Object>> results, List<String> columnNames) {
        int[] columnWidths = new int[columnNames.size()];

        for (int i = 0; i < columnNames.size(); i++) {
            columnWidths[i] = columnNames.get(i).length();
        }

        for (Map<String, Object> row : results) {
            for (int i = 0; i < columnNames.size(); i++) {
                String columnName = columnNames.get(i);
                Object value = row.get(columnName);
                String valueStr = value == null ? "NULL" : value.toString();
                columnWidths[i] = Math.max(columnWidths[i], valueStr.length());
            }
        }

        return columnWidths;
    }

    private static void printRow(List<String> values, int[] widths) {
        StringBuilder sb = new StringBuilder("|");
        for (int i = 0; i < values.size(); i++) {
            sb.append(" ").append(String.format("%-" + widths[i] + "s", values.get(i))).append(" |");
        }
        System.out.println(sb);
    }

    private static void printSeparator(int[] widths) {
        StringBuilder sb = new StringBuilder("|");
        for (int width : widths) {
            sb.append("-".repeat(width + 2)).append("|");
        }
        System.out.println(sb);
    }
}