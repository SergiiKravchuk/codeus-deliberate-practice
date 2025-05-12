package org.codeus.fundamentals.basic_triggers;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SqlQueriesTest {

    protected static EmbeddedPostgres postgres;
    protected static Connection connection;

    @BeforeAll
    static void beforeAll() throws IOException {
        startDatabase();
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
    @DisplayName("1: Trigger BEFORE INSERT FOR EACH ROW")
    void beforeInsertRow() throws SQLException, IOException {
        System.out.println("1: Trigger BEFORE INSERT FOR EACH ROW\n");

        String insertPattern = "INSERT INTO customers (first_name, last_name, email) VALUES ('%s', '%s', '%s');";
        String selectPattern = "SELECT * FROM customers WHERE first_name = '%s' AND last_name = '%s' AND email = '%s';";

        System.out.println("Check that first and last names are left uncapitalized (before trigger creation)");
        executeCUD(insertPattern.formatted("roman", "carman", "roman_carman@gmail.com"), true);
        List<Map<String, Object>> uncapitalized = executeQuery(
                selectPattern.formatted("roman", "carman", "roman_carman@gmail.com"),
                true
        );
        printQueryResults(uncapitalized);

        System.out.println("Execute `1-before-insert-row.sql` with task!\n");
        executeSqlFile("task/1-before-insert-row.sql");

        System.out.println("Check that first and last names are capitalized (after trigger creation), for one row");
        executeCUD(insertPattern.formatted("grytsko", "stytsko", "grytsko_stytsko@gmail.com"), true);
        List<Map<String, Object>> capitalizedOne = executeQuery(
                selectPattern.formatted("Grytsko", "Stytsko", "grytsko_stytsko@gmail.com"),
                true
        );
        printQueryResults(capitalizedOne);

        assertEquals(1, uncapitalized.size(), "No results found. Trigger has not been executed");

        System.out.println("Check that first and last names are capitalized (after trigger creation), for one row");
        executeCUD("""
                        INSERT INTO customers (first_name, last_name, email)
                        VALUES ('victor', 'syniy', 'victor.syniy@example.com'),
                               ('juli', 'babuli', 'juli.babuli@example.com'),
                               ('nika', 'pika', 'nika.pika@example.com');
                        """,
                true
        );
        List<Map<String, Object>> capitalizedMany = executeQuery(
                "SELECT * FROM customers;",
                true
        );
        printQueryResults(capitalizedMany);
        List<String> firstNames = capitalizedMany.stream()
                .map(map -> map.get("first_name"))
                .map(String::valueOf)
                .toList();
        List<String> lastNames = capitalizedMany.stream()
                .map(map -> map.get("last_name"))
                .map(String::valueOf)
                .toList();

        for (String name : new String[]{"Grytsko", "Victor", "Juli", "Nika"}) {
            assertTrue(firstNames.contains(name), "First name %s hasn't been capitalized".formatted(name));
        }
        for (String name : new String[]{"Stytsko", "Syniy", "Babuli", "Pika"}) {
            assertTrue(lastNames.contains(name), "Last name %s hasn't been capitalized".formatted(name));
        }

        System.out.println("=========================================================================================================================================================================\n");
    }

    @Test
    @Order(5)
    @DisplayName("2: Trigger BEFORE INSERT FOR EACH STATEMENT (Logging)")
    void beforeInsertStatementLog() throws SQLException, IOException {
        System.out.println("2: Trigger BEFORE INSERT FOR EACH STATEMENT (Logging)\n");

        System.out.println("Execute `task/2-before-insert-statement.sql` with task!");
        executeSqlFile("task/2-before-insert-statement.sql"); //

        System.out.println("Attempting a single row insert into customers...");
        String singleInsertSql = "INSERT INTO customers (first_name, last_name, email) VALUES ('Vika', 'Chickchirika', 'vika.chickchirika@example.com');";
        executeCUD(singleInsertSql, true);

        List<Map<String, Object>> logsAfterSingle = executeQuery("SELECT * FROM audit_logs;", true);
        printQueryResults(logsAfterSingle);

        assertEquals(1, logsAfterSingle.size(), "Log entry not found after single insert.");
        Map<String, Object> singleLogRow = logsAfterSingle.get(0);
        assertEquals("INSERT", singleLogRow.get("operation_type").toString(), "Operation type mismatch for single insert.");
        assertEquals("customers", singleLogRow.get("table_name").toString(), "Table name mismatch for single insert.");
        assertTrue(
                singleLogRow.get("query_text").toString().contains("'Vika', 'Chickchirika'"),
                "Query text in log does not match for single insert. Logged: " + singleLogRow.get("query_text")
        );

        System.out.println("\nAttempting a multi-row insert into customers...");
        String multiInsertSql = "INSERT INTO customers (first_name, last_name, email) VALUES ('Dina', 'Verdina', 'dina.verdina@example.com'), ('Vasyl', 'Barbarys', 'vasyl.barbarys@example.com');";
        executeCUD(multiInsertSql, true);
        List<Map<String, Object>> logsAfterMulti = executeQuery("SELECT * FROM audit_logs;", true);
        printQueryResults(logsAfterMulti);
        assertEquals(2, logsAfterMulti.size(), "No new audit log found after multi-row insert");


        System.out.println("\nAttempting an INSERT statement that inserts zero actual rows...");
        String zeroRowInsertSql = "INSERT INTO customers (first_name, last_name, email) SELECT 'zeki', 'zero', 'zeki.zero@example.com' WHERE FALSE;";
        executeCUD(zeroRowInsertSql, true);
        List<Map<String, Object>> zekiCustomer = executeQuery("SELECT * FROM customers WHERE email = 'zeki.zero@example.com';", false);
        assertEquals(0, zekiCustomer.size(), "Customer 'zeki zero' should not have been inserted.");
        List<Map<String, Object>> logsAfterZeroRowInsert = executeQuery("SELECT * FROM audit_logs;", true);
        printQueryResults(logsAfterZeroRowInsert);
        assertEquals(3, logsAfterZeroRowInsert.size(), "No new audit log found after zero row insert");

        System.out.println("=========================================================================================================================================================================\n");
    }

    @Test
    @Order(10)
    @DisplayName("3: Trigger AFTER UPDATE FOR EACH ROW")
    void afterUpdateRow() throws SQLException, IOException {
        System.out.println("3: Trigger AFTER UPDATE FOR EACH ROW\n");

        System.out.println("Execute `task/3-after-update-row.sql` with task!\n");
        executeSqlFile("task/3-after-update-row.sql");

        executeCUD("DELETE FROM audit_logs;", false);

        executeCUD("UPDATE customers SET first_name = 'UpdatedName' WHERE id = (SELECT id FROM customers LIMIT 1);", true);

        List<Map<String, Object>> logs = executeQuery("SELECT * FROM audit_logs;", true);
        printQueryResults(logs);

        assertEquals(1, logs.size(), "Audit log should be created after update");
        assertEquals("UPDATE", logs.get(0).get("operation_type").toString(), "Operation type should be UPDATE");
        assertEquals("customers", logs.get(0).get("table_name").toString(), "Table name should be customers");

        System.out.println("=========================================================================================================================================================================\n");
    }

    @Test
    @Order(15)
    @DisplayName("4: Triggers for Transaction Transfers")
    void transactionTransferTriggers() throws SQLException, IOException {
        System.out.println("4: Triggers for Transaction Transfers\n");

        System.out.println("Preparation...");
        executeCUD("DELETE FROM transactions;", false);
        executeCUD("DELETE FROM accounts;", false);
        executeCUD("DELETE FROM customers;", false);

        System.out.println("Execute `task/4-transaction-transfer-trigger.sql` with task!\n");
        executeSqlFile("task/4-transaction-transfer-trigger.sql");

        System.out.println("Setting up test data - customers and accounts");
        executeCUD(
                "INSERT INTO customers (first_name, last_name, email) VALUES ('Ivan', 'Sender', 'ivan.sender@example.com'), ('Mary', 'Receiver', 'mary.receiver@example.com');",
                true
        );

        List<Map<String, Object>> customers = executeQuery("SELECT id, first_name, last_name FROM customers;", true);
        printQueryResults(customers);
        int senderId = Integer.parseInt(customers.get(0).get("id").toString());
        int receiverId = Integer.parseInt(customers.get(1).get("id").toString());

        executeCUD(
                "INSERT INTO accounts (customer_id, account_type, balance) VALUES (%s, 'checking', 1000.00), (%s, 'savings', 500.00);".formatted(senderId, receiverId),
                true
        );

        List<Map<String, Object>> initialAccounts = executeQuery(
                "SELECT a.id account_id, a.customer_id, c.first_name, c.last_name, a.account_type, a.balance FROM accounts a JOIN customers c ON a.customer_id = c.id;",
                true
        );
        printQueryResults(initialAccounts);
        int senderAccountId = Integer.parseInt(initialAccounts.get(0).get("account_id").toString());
        int receiverAccountId = Integer.parseInt(initialAccounts.get(1).get("account_id").toString());

        System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------------------------");

        System.out.println("\nTest 1: Successful transfer (sufficient balance)");
        executeCUD("INSERT INTO transactions (account_id, transaction_type, amount, target_account_id) VALUES (%s, 'transfer', 200.00, %s);"
                        .formatted(senderAccountId, receiverAccountId),
                true
        );

        List<Map<String, Object>> updatedAccounts = executeQuery(
                "SELECT a.id account_id, a.customer_id, c.first_name, c.last_name, a.account_type, a.balance FROM accounts a JOIN customers c ON a.customer_id = c.id;",
                true
        );
        printQueryResults(updatedAccounts);

        for (Map<String, Object> account : updatedAccounts) {
            int accountId = Integer.parseInt(account.get("account_id").toString());
            double balance = Double.parseDouble(account.get("balance").toString());

            if (accountId == senderAccountId) {
                assertEquals(800, balance, "Sender didn't send money: " + balance);
            }
            if (accountId == receiverAccountId) {
                assertEquals(700, balance, "Receiver didn't receive money: " + balance);
            }
        }
        System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------------------------");


        System.out.println("\nTest 2: Failed transfer (insufficient balance)");
        boolean exceptionThrown = false;
        try {
            executeCUD(
                    "INSERT INTO transactions (account_id, transaction_type, amount, target_account_id) VALUES (%s, 'transfer', 1000.00, %s);"
                            .formatted(senderAccountId, receiverAccountId),
                    true
            );
        } catch (SQLException e) {
            exceptionThrown = true;
            System.out.println("Expected exception caught: " + e.getMessage());
        }
        assertTrue(exceptionThrown, "Exception should be thrown when trying to transfer with insufficient funds");

        List<Map<String, Object>> finalAccounts = executeQuery(
                "SELECT a.id account_id, a.customer_id, c.first_name, c.last_name, a.account_type, a.balance FROM accounts a JOIN customers c ON a.customer_id = c.id;",
                true
        );
        printQueryResults(finalAccounts);

        for (Map<String, Object> account : finalAccounts) {
            int accountId = Integer.parseInt(account.get("account_id").toString());
            double balance = Double.parseDouble(account.get("balance").toString());

            if (accountId == senderAccountId) {
                assertEquals(800, balance, "Sender send money: " + balance);
            }
            if (accountId == receiverAccountId) {
                assertEquals(700, balance, "Receiver receive money: " + balance);
            }
        }

        System.out.println("=========================================================================================================================================================================\n");
    }

    @Test
    @Order(20)
    @DisplayName("5: Trigger INSTEAD OF UPDATE/DELETE FOR EACH ROW")
    void insteadOfTriggers() throws SQLException, IOException {
        System.out.println("5: Trigger INSTEAD OF UPDATE/DELETE FOR EACH ROW\n");

        System.out.println("Execute `task/5-instead-of-trigger.sql` with task!\n");
        executeSqlFile("task/5-instead-of-trigger.sql");

        // Clear any existing audit logs
        executeCUD("DELETE FROM transactions;", false);
        executeCUD("DELETE FROM accounts;", false);
        executeCUD("DELETE FROM customers;", false);
        connection.commit();

        // Set up test data - add some customers with accounts
        System.out.println("Setting up test data - customers with accounts");
        executeCUD(
                "INSERT INTO customers (first_name, last_name, email) VALUES " +
                        "('Kolya', 'Topolya', 'kolya.topolya@example.com'), " +
                        "('Taras', 'Carabas', 'taras.carabas@example.com');",
                true
        );

        List<Map<String, Object>> customers = executeQuery(
                "SELECT * FROM customers ORDER BY id DESC LIMIT 2;",
                true
        );
        printQueryResults(customers);
        int kolyaId = Integer.parseInt(customers.get(1).get("id").toString());
        int tarasId = Integer.parseInt(customers.get(0).get("id").toString());

        executeCUD(
                "INSERT INTO accounts (customer_id, account_type, balance) VALUES (%s, 'checking', 1500.00), (%s, 'savings', 2500.00), (%s, 'checking', 3000.00);"
                        .formatted(kolyaId, kolyaId, tarasId),
                true
        );

        // Test the view first
        System.out.println("\nViewing customer_account_summary_view before updates");
        List<Map<String, Object>> viewData = executeQuery(
                "SELECT * FROM customer_account_summary_view ORDER BY customer_id;",
                true
        );
        printQueryResults(viewData);

        assertEquals(2, viewData.size(), "Should have 2 customers in the view");

        Map<String, Object> johnSummary = viewData.get(0);
        assertEquals(kolyaId, Integer.parseInt(johnSummary.get("customer_id").toString()));
        assertEquals("Kolya", johnSummary.get("first_name"));
        assertEquals("Topolya", johnSummary.get("last_name"));
        assertEquals(2, Integer.parseInt(johnSummary.get("account_count").toString()));
        assertEquals(4000.0, Double.parseDouble(johnSummary.get("total_balance").toString()), 0.01);

        System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------------------------");

        // Test INSTEAD OF UPDATE
        System.out.println("\nTest 1: INSTEAD OF UPDATE trigger");
        executeCUD(
                "UPDATE customer_account_summary_view " +
                        "SET first_name = 'Mykola'" +
                        "WHERE email = 'kolya.topolya@example.com';",
                true
        );

        // Verify the update happened in the base table
        List<Map<String, Object>> updatedCustomer = executeQuery(
                "SELECT * FROM customers WHERE email = 'kolya.topolya@example.com';",
                true
        );
        printQueryResults(updatedCustomer);

        assertEquals("Mykola", updatedCustomer.get(0).get("first_name"));
        assertEquals("Topolya", updatedCustomer.get(0).get("last_name"));

        // Verify the update appears in the view
        List<Map<String, Object>> updatedView = executeQuery(
                "SELECT * FROM customer_account_summary_view WHERE email = 'kolya.topolya@example.com';",
                true
        );
        printQueryResults(updatedView);

        assertEquals("Mykola", updatedView.get(0).get("first_name"));
        assertEquals("Topolya", updatedView.get(0).get("last_name"));

        System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------------------------");

        // Test INSTEAD OF DELETE
        System.out.println("\nTest 2: INSTEAD OF DELETE trigger");

        // First, verify Taras's data exists
        System.out.println("View before deleting Taras:");
        printQueryResults(executeQuery(
                        "SELECT * FROM customer_account_summary_view;",
                        true
                )
        );

        // Execute the INSTEAD OF DELETE
        executeCUD(
                "DELETE FROM customer_account_summary_view WHERE email = 'taras.carabas@example.com';",
                true
        );

        System.out.println("\nView after deleting Taras:");
        printQueryResults(executeQuery(
                        "SELECT * FROM customer_account_summary_view;",
                        true
                )
        );

        // Verify the customer was deleted from the base table
        List<Map<String, Object>> tarasAfter = executeQuery(
                "SELECT * FROM customers WHERE email = 'taras.carabas@example.com';",
                false
        );
        assertEquals(0, tarasAfter.size(), "Taras should be deleted from customers table");

        // Verify cascading delete removed accounts
        List<Map<String, Object>> tarasAccountsAfter = executeQuery(
                "SELECT * FROM accounts WHERE customer_id = %s;".formatted(tarasId),
                false
        );
        assertEquals(0, tarasAccountsAfter.size(), "Taras's accounts should be deleted (CASCADE)");

        System.out.println("=========================================================================================================================================================================\n");
    }

    private static void executeSqlFile(String filePath) throws IOException, SQLException {
        String sql = getSqlScriptContent(filePath);
        try (Statement statement = connection.createStatement()) {
            boolean execute = statement.execute(sql);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            connection.rollback();
            throw e;
        }
    }

    private static String getSqlScriptContent(String filePath) throws IOException {
        return Files.readString(Paths.get("src/test/resources/" + filePath));
    }

    private static List<Map<String, Object>> executeQuery(String sql) throws SQLException {
        return executeQuery(sql, false);
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

        // Initialize with header widths
        for (int i = 0; i < columnNames.size(); i++) {
            columnWidths[i] = columnNames.get(i).length();
        }

        // Update with data widths
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