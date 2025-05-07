package org.codeus.database.fundamentals.concurrency_control;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TransactionConcurrencyTest {

    protected static EmbeddedPostgres postgres;
    protected static Connection connection;
    protected static DataSource dataSource;

    private static final String SCHEMA_FILE = "schema.sql";
    private static final String TEST_DATA_FILE = "test-data.sql";

    private static final String TASK_01_DIR = "tasks/01-anomaly-non-repeatable-read/";
    private static final String TASK_02_DIR = "tasks/02-fix-non-repeatable-read/";
    private static final String TASK_03_DIR = "tasks/03-anomaly-phantom-read/";
    private static final String TASK_04_DIR = "tasks/04-anomaly-serialization/";
    private static final String TASK_05_DIR = "tasks/05-fix-anomaly-serialization/";
    private static final String TASK_06_DIR = "tasks/06-pessimistic-locking/";
    private static final String TASK_07_DIR = "tasks/07-optimistic-locking/";
    private static final String TASK_08_DIR = "tasks/08-deadlock-scenario/";
    private static final String TASK_09_DIR = "tasks/09-row-locks-for-share/";

    @BeforeAll
    static void startDatabase() throws IOException {
        System.out.println("Starting embedded PostgreSQL...");
        postgres = EmbeddedPostgres.start();
        postgres.getPostgresDatabase();
        try {
            dataSource = postgres.getPostgresDatabase();
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            System.out.println("PostgreSQL started successfully");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get database connection", e);
        }
    }

    @AfterAll
    static void stopDatabase() throws IOException {
        System.out.println("Stopping embedded PostgreSQL...");
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

    @BeforeEach
    void setupSchema() throws SQLException, IOException {
        System.out.println("Setting up database schema and test data...");
        // Start a transaction that will be rolled back after each test
        connection.setAutoCommit(false);

        // Clear any existing data
        clearDatabase();

        // Initialize database schema
        executeSqlFile(getResourcePath(SCHEMA_FILE));

        // Load test data
        executeSqlFile(getResourcePath(TEST_DATA_FILE));

        connection.commit();
        System.out.println("Setup complete");
    }

    @Test
    @Order(1)
    void testReadCommitted() throws IOException, SQLException, InterruptedException, ExecutionException {
        String readTransaction = Files.readString(Paths.get(getResourcePath(TASK_01_DIR + "1-1-repeatable-read-transaction.sql")));
        String updateTransaction = Files.readString(Paths.get(getResourcePath(TASK_01_DIR + "1-2-concurrent-update-transaction.sql")));

        executeConcurrently(readTransaction, updateTransaction);

        List<Map<String, Object>> results = executeQuery(
                "SELECT * FROM isolation_level_test ORDER BY test_id");

        assertFalse(results.isEmpty(), "You must demonstrate NON-REPEATABLE-READ anomaly");

        Map<String, Object> readCommittedResult = findIsolationLevelResult(results, "READ COMMITTED");
        assertNotNull(readCommittedResult, "You must include a test for READ COMMITTED isolation level");

        Object firstRead = readCommittedResult.get("first_read");
        Object secondRead = readCommittedResult.get("second_read");
        assertNotEquals(convertToDouble(firstRead), convertToDouble(secondRead),
                "READ COMMITTED should demonstrate non-repeatable read (different values)");
    }

    @Test
    @Order(2)
    void testNonRepeatableRead() throws IOException, SQLException, InterruptedException, ExecutionException {
        String readTransaction = Files.readString(Paths.get(getResourcePath(TASK_02_DIR + "2-1-fix-repeatable-read-transaction.sql")));
        String updateTransaction = Files.readString(Paths.get(getResourcePath(TASK_02_DIR + "2-2-fix-concurrent-update-transaction.sql")));

        executeConcurrently(readTransaction, updateTransaction);

        List<Map<String, Object>> results = executeQuery(
                "SELECT * FROM isolation_level_test ORDER BY test_id");

        assertFalse(results.isEmpty(), "You must demonstrate REPEATABLE READ isolation levels");

        Map<String, Object> repeatableReadResult = findIsolationLevelResult(results, "REPEATABLE READ");
        assertNotNull(repeatableReadResult, "You must include a test for READ COMMITTED isolation level");

        var firstRead = repeatableReadResult.get("first_read");
        var secondRead = repeatableReadResult.get("second_read");
        assertEquals(convertToDouble(firstRead), convertToDouble(secondRead),
                "REPEATABLE READ should prevent non-repeatable read (same values)");
    }

    @Test
    @Order(3)
    void testReadCommittedPhantomRead() throws IOException, SQLException, InterruptedException, ExecutionException {
        String readTransaction = Files.readString(Paths.get(getResourcePath(TASK_03_DIR + "3-1-phantom-read-transaction.sql")));
        String updateTransaction = Files.readString(Paths.get(getResourcePath(TASK_03_DIR + "3-2-phantom-read-update-transaction.sql")));

        executeConcurrently(readTransaction, updateTransaction);

        List<Map<String, Object>> results = executeQuery(
                "SELECT * FROM isolation_level_test ORDER BY test_id");

        int expectedNumberOfResults = 2;
        assertFalse(results.isEmpty() || results.size() != expectedNumberOfResults,
          "You must demonstrate PHANTOM READ anomaly. Two row results are expected");

        // Account 2 verification
        Map<String, Object> accountResult1 = results.get(0);
        var firstReadBalance1 = convertToDouble(accountResult1.get("first_read"));
        var secondReadBalance1 = convertToDouble(accountResult1.get("second_read"));
        assertEquals(firstReadBalance1, secondReadBalance1,
          "Balance value between read operation should be the same for account " + accountResult1.get("account_id"));

        // Account 4 verification
        Map<String, Object> accountResult2 = results.get(1);
        var firstReadBalance2 = convertToDouble(accountResult2.get("first_read"));
        var secondReadBalance2 = convertToDouble(accountResult2.get("second_read"));
        assertNotEquals(firstReadBalance2, secondReadBalance2,
          "Balance value between read operation should be different for account " + accountResult2.get("account_id"));
    }

    @Test
    @Order(4)
    void testReadCommittedSerializableLostUpdate() throws IOException, SQLException, InterruptedException, ExecutionException {
        String readTransaction = Files.readString(Paths.get(getResourcePath(TASK_04_DIR + "4-1-non-serialization-transaction.sql")));
        String updateTransaction = Files.readString(Paths.get(getResourcePath(TASK_04_DIR + "4-2-concurrent-update-transaction.sql")));

        executeConcurrently(readTransaction, updateTransaction);

        List<Map<String, Object>> results = executeQuery(
                "SELECT * FROM serialization_test_results ORDER BY result_id");

        int expectedNumberOfResults = 2;
        assertFalse(results.isEmpty() || results.size() != expectedNumberOfResults,
          "You must demonstrate LOST UPDATE anomaly. Two row results are expected");

        Map<String, Object> repeatableReadResult = findIsolationLevelResult(results, "READ COMMITTED");
        assertNotNull(repeatableReadResult, "You must include a test for READ COMMITTED isolation level");

        // Transaction 1 result verification
        Map<String, Object> transactionResult1 = results.get(1);
        var finalBalance1 = convertToDouble(transactionResult1.get("final_balance"));
        assertEquals(-1000.0, finalBalance1,
          "Final balance value of the 1st transaction should be invalid (negative number)");

        // Transaction 2 result verification
        Map<String, Object> transactionResult2 = results.get(0);
        var finalBalance2 = convertToDouble(transactionResult2.get("final_balance"));
        assertEquals(500.0, finalBalance2,
          "Final balance value of the 2st transaction should be valid");
    }

    @Test
    @Order(5)
    void testSerializableSerialization() throws IOException, SQLException, InterruptedException {
        String firstTr = Files.readString(Paths.get(getResourcePath(TASK_05_DIR + "5-1-fix-serialization-transaction.sql")));
        String secondTr = Files.readString(Paths.get(getResourcePath(TASK_05_DIR + "5-2-fix-concurrent-update-transaction.sql")));

        SQLException exception = assertThrows(SQLException.class, () -> {
            executeConcurrently(firstTr, secondTr);
        }, "You must demonstrate SERIALIZATION FAILURE");

        Throwable initialException = exception.getCause().getCause();
        assertTrue(initialException.getMessage().contains("could not serialize access"),
                "You must demonstrate SERIALIZATION FAILURE, " +
                        "which contains message 'could not serialize access', but was: " + initialException.getMessage());
    }

    @Test
    @Order(5)
    void testPessimisticLocking() throws IOException, SQLException, InterruptedException, ExecutionException {
        String firstTr = Files.readString(Paths.get(getResourcePath(TASK_06_DIR + "6-1-pessimistic-lock.sql")));
        String secondTr = Files.readString(Paths.get(getResourcePath(TASK_06_DIR + "6-2-update-during-pessimistic-lock.sql")));

        executeConcurrently(firstTr, secondTr);

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            // Check the final account balances
            ResultSet balanceRs = stmt.executeQuery(
                    "SELECT account_id, balance FROM accounts ORDER BY account_id");

            // Account 1 should be decreased by 500 - 100 = 400 (500 from transfer, +100 from second tx)
            balanceRs.next();
            assertEquals(1, balanceRs.getInt("account_id"));
            assertEquals(600.0, balanceRs.getDouble("balance"), 0.01);

            // Account 2 should be increased by 500
            balanceRs.next();
            assertEquals(2, balanceRs.getInt("account_id"));
            assertEquals(2500.0, balanceRs.getDouble("balance"), 0.01);

            // Check the test results
            ResultSet rs = stmt.executeQuery(
                    "SELECT * FROM lock_test_results ORDER BY transaction_id");

            // First transaction
            rs.next();
            assertEquals(1, rs.getInt("transaction_id"));
            assertEquals("PESSIMISTIC", rs.getString("lock_type"));
            assertEquals(1, rs.getInt("account_id"));
            assertEquals(1000.0, rs.getDouble("initial_balance"), 0.01);
            assertEquals(500.0, rs.getDouble("final_balance"), 0.01);
            assertTrue(rs.getBoolean("success"));

            // Second transaction
            rs.next();
            assertEquals(2, rs.getInt("transaction_id"));
            assertEquals("PESSIMISTIC", rs.getString("lock_type"));
            assertEquals(1, rs.getInt("account_id"));
            assertEquals(500.0, rs.getDouble("initial_balance"), 0.01);
            assertEquals(600.0, rs.getDouble("final_balance"), 0.01);
            assertTrue(rs.getBoolean("success"));

            // Verify the blocking time - should be roughly 2 seconds or more
            double blockingTime = rs.getDouble("blocking_time_ms");
            System.out.println("Transaction 2 was blocked for " + blockingTime + " ms");
            assertTrue(blockingTime >= 1500,
                    "Second transaction should have been blocked by the first transaction's lock");

            System.out.println("Pessimistic locking test passed: transactions executed sequentially as expected");
        }
    }

    @Test
    @Order(6)
    void testOptimisticLocking() throws IOException, SQLException, InterruptedException, ExecutionException {
        String firstTr = Files.readString(Paths.get(getResourcePath(TASK_07_DIR + "7-1-optimistic-lock.sql")));
        String secondTr = Files.readString(Paths.get(getResourcePath(TASK_07_DIR + "7-2-update-during-optimistic-lock.sql")));

        executeConcurrently(firstTr, secondTr);

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            // Check the final account balances
            ResultSet balanceRs = stmt.executeQuery(
                    "SELECT account_id, balance, version FROM accounts ORDER BY account_id");

            // Account 1 should be modified by transaction 2 only (increased by 100)
            balanceRs.next();
            assertEquals(1, balanceRs.getInt("account_id"));
            assertEquals(1100.0, balanceRs.getDouble("balance"), 0.01);
            assertEquals(2, balanceRs.getInt("version"));

            // Account 2 should be unchanged
            balanceRs.next();
            assertEquals(2, balanceRs.getInt("account_id"));
            assertEquals(2500.0, balanceRs.getDouble("balance"), 0.01);
            assertEquals(2, balanceRs.getInt("version"));

            // Check the test results
            ResultSet rs = stmt.executeQuery(
                    "SELECT * FROM lock_test_results ORDER BY transaction_id");

            // Second transaction should have failed due to optimistic lock conflict
            rs.next();
            assertEquals(1, rs.getInt("transaction_id"));
            assertEquals("OPTIMISTIC", rs.getString("lock_type"));
            assertEquals(1, rs.getInt("account_id"));
            assertEquals(1000.0, rs.getDouble("initial_balance"), 0.01);
            assertFalse(rs.getBoolean("success"));
            assertEquals(1, rs.getInt("version_before"));

            // Second transaction should have success
            rs.next();
            assertEquals(2, rs.getInt("transaction_id"));
            assertEquals("OPTIMISTIC", rs.getString("lock_type"));
            assertEquals(1, rs.getInt("account_id"));
            assertEquals(1000.0, rs.getDouble("initial_balance"), 0.01);
            assertEquals(1100.0, rs.getDouble("final_balance"), 0.01);
            assertTrue(rs.getBoolean("success"));
            assertEquals(1, rs.getInt("version_before"));
            assertEquals(2, rs.getInt("version_after"));

            // Unlike pessimistic locking, optimistic locking should not block
            double executionTime = rs.getDouble("execution_time_ms");
            System.out.println("Transaction 2 executed in " + executionTime + " ms");

            System.out.println("Optimistic locking test passed: concurrent modification detected as expected");
        }
    }

    @Test
    @Order(7)
    void testDeadlockDetection() throws IOException, SQLException, InterruptedException, ExecutionException {
        String firstTr = Files.readString(Paths.get(getResourcePath(TASK_08_DIR + "8-1-deadlock-tr-1.sql")));
        String secondTr = Files.readString(Paths.get(getResourcePath(TASK_08_DIR + "8-2-deadlock-tr-2.sql")));

        SQLException exception = assertThrows(SQLException.class, () -> {
            executeConcurrently(firstTr, secondTr);
        }, "Dead lock demo");


        Throwable initialException = exception.getCause().getCause();
        assertTrue(initialException.getMessage().contains("deadlock"),
                "You must demonstrate deadlock, " +
                        "which contains message 'deadlock', but was: " + initialException.getMessage());

        System.out.println("\nDeadlock test validation completed");
    }

    @Test
    @Order(9)
    void testSelectForShare() throws IOException, SQLException, InterruptedException, ExecutionException {
        String firstTr = Files.readString(Paths.get(getResourcePath(TASK_09_DIR + "9-1-row-lock-share-model.sql")));
        String secondTr = Files.readString(Paths.get(getResourcePath(TASK_09_DIR + "9-2-row-lock-share-concurrent.sql")));

        executeConcurrently(firstTr, secondTr);

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            // Check if both transactions acquired SHARE locks
            ResultSet rs = stmt.executeQuery(
                    "SELECT COUNT(*) FROM lock_test_results WHERE lock_mode = 'FOR SHARE'");
            rs.next();
            int shareLockCount = rs.getInt(1);

            // Both transactions should have acquired SHARE locks
            assertEquals(2, shareLockCount,
                    "Both transactions should have acquired SHARE locks concurrently");

            // Check if both operations succeeded
            ResultSet successRs = stmt.executeQuery(
                    "SELECT transaction_id, operation, success FROM lock_test_results " +
                            "WHERE operation IN ('READ', 'UPDATE') ORDER BY transaction_id");

            // Transaction 1 should have successfully updated
            successRs.next();
            assertEquals(1, successRs.getInt("transaction_id"));
            assertEquals("UPDATE", successRs.getString("operation"));
            assertTrue(successRs.getBoolean("success"));

            // Transaction 2 should have successfully read
            successRs.next();
            assertEquals(2, successRs.getInt("transaction_id"));
            assertEquals("READ", successRs.getString("operation"));
            assertTrue(successRs.getBoolean("success"));

            // Verify final balance is correct after the UPDATE operation
            ResultSet inventoryRs = stmt.executeQuery(
                    "SELECT balance FROM accounts WHERE customer_id = 1");
            inventoryRs.next();
            int finalBalance = inventoryRs.getInt("balance");
            assertEquals(900, finalBalance,
                    "Final balance should be 900 after Transaction 1's update of -100");

            System.out.println("FOR SHARE lock test passed: Multiple transactions can acquire SHARE locks concurrently");
        }
    }

    protected String getResourcePath(String resourceName) {
        // In a real application, you would use a resource loader
        // Here we're simplifying by using a relative path
        return "src/test/resources/" + resourceName;
    }

    private void clearDatabase() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("DROP SCHEMA public CASCADE; CREATE SCHEMA public;");
        }
    }

    private Map<String, Object> findIsolationLevelResult(List<Map<String, Object>> results, String isolationLevel) {
        return results.stream()
                .filter(row -> isolationLevel.equalsIgnoreCase(String.valueOf(row.get("isolation_level"))))
                .findFirst()
                .orElse(null);
    }

    private double convertToDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return Double.parseDouble(value.toString());
    }

    private void executeConcurrently(String firstTransactionSql, String secondTransactionSql)
            throws InterruptedException, SQLException, ExecutionException {
        System.out.println("Starting concurrent transactions");

        CountDownLatch firstTransactionStarted = new CountDownLatch(1);
        CountDownLatch secondTransactionFinished = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        Future<?> firstTask = executor.submit(() -> {
            System.out.println("First transaction: starting");
            try {
                try (Connection conn = dataSource.getConnection()) {
                    String[] parts = firstTransactionSql.split("--WAIT_HERE");

                    System.out.println("First transaction: executing SQL");
                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute(parts[0]);
                    }
                    System.out.println("First transaction: SQL executed, signaling start");
                    firstTransactionStarted.countDown();

                    System.out.println("First transaction: sleeping to allow second transaction to execute");
                    Thread.sleep(3000);

                    System.out.println("First transaction: awake, executing one more read");
                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute(parts[1]);
                    }

                    System.out.println("First transaction: waiting for second transaction to finish");
                    boolean finished = secondTransactionFinished.await(30, TimeUnit.SECONDS);
                    if (!finished) {
                        System.out.println("First transaction: timeout waiting for second transaction to finish");
                    } else {
                        System.out.println("First transaction: second transaction finished, committing");
                    }
                } finally {
                    System.out.println("First transaction: closing connection");
                }
            } catch (Exception cause) {
                System.out.println("Exception in first transaction");
                throw new RuntimeException("Exception in first transaction", cause);
            }
        });

        Future<?> secondTask = executor.submit(() -> {
            System.out.println("Second transaction: waiting for first transaction to start");
            try {
                boolean started = firstTransactionStarted.await(30, TimeUnit.SECONDS);
                if (!started) {
                    System.out.println("Second transaction: timeout waiting for first transaction to start");
                    return;
                }

                System.out.println("Second transaction: first transaction started, executing SQL");
                try (Connection conn = dataSource.getConnection()) {
                    System.out.println("Second transaction: executing SQL");
                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute(secondTransactionSql);
                    }
                    System.out.println("Second transaction: SQL executed, committing");
                } finally {
                    System.out.println("Second transaction: closing connection");
                    System.out.println("Second transaction: signaling completion");
                    secondTransactionFinished.countDown();
                }
            } catch (Exception e) {
                System.out.println("Exception in second transaction");
                secondTransactionFinished.countDown();
                throw new RuntimeException("Exception in second transaction", e);
            }
        });


        try {
            System.out.println("Waiting for both transactions to complete");
            firstTask.get(60, TimeUnit.SECONDS);
            secondTask.get(60, TimeUnit.SECONDS);
            System.out.println("Both transactions completed successfully");
        } catch (ExecutionException e) {
            System.out.println("Exception during transaction execution");
            throw new SQLException("Exception during transaction execution", e.getCause());
        } catch (TimeoutException e) {
            System.out.println("Timeout during transaction execution");
            throw new SQLException("Timeout during transaction execution", e);
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.out.println("Thread pull not terminated, forcing shutdown");
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                System.out.println("Terminating thread pool interrupted, forcing shutdown");
                executor.shutdownNow();
            }
        }
    }

    public static Throwable getRootCause(Exception throwable) {
        // Keep going until we find the root cause (an exception with no cause)
        Throwable cause = throwable;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause;
    }

    private List<Map<String, Object>> executeQuery(String sql) throws SQLException {
        System.out.printf("Executing query:%n%s%n%n", sql);

        List<Map<String, Object>> results = new ArrayList<>();

        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            List<String> columnNames = new ArrayList<>();
            int[] columnWidths = new int[columnCount];

            for (int i = 1; i <= columnCount; i++) {
                String name = metaData.getColumnLabel(i);
                columnNames.add(name);
                columnWidths[i - 1] = name.length();
            }

            while (resultSet.next()) {
                Map<String, Object> row = new HashMap<>();

                for (int i = 1; i <= columnCount; i++) {
                    String columnName = columnNames.get(i - 1);
                    Object value = resultSet.getObject(i);
                    String valueStr = value == null ? "NULL" : value.toString();
                    columnWidths[i - 1] = Math.max(columnWidths[i - 1], valueStr.length());
                    row.put(columnName, value);
                }

                results.add(row);
            }

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

            System.out.println("\n-------------------------------------------------------\n");
        }

        return results;
    }

    private void printRow(List<String> values, int[] widths) {
        StringBuilder sb = new StringBuilder("|");
        for (int i = 0; i < values.size(); i++) {
            sb.append(" ").append(String.format("%-" + widths[i] + "s", values.get(i))).append(" |");
        }
        System.out.println(sb);
    }

    private void printSeparator(int[] widths) {
        StringBuilder sb = new StringBuilder("|");
        for (int width : widths) {
            sb.append("-".repeat(width + 2)).append("|");
        }
        System.out.println(sb);
    }

    private static void executeSqlFile(String fileName) throws IOException, SQLException {
        String sql = Files.readString(Paths.get(fileName)).trim();
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
}