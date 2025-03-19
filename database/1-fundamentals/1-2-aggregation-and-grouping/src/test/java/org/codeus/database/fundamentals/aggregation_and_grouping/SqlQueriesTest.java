package org.codeus.database.fundamentals.aggregation_and_grouping;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SqlQueriesTest {

    private static EmbeddedPostgres postgres;
    private static Connection connection;

    // File paths relative to src/test/resources
    private static final String SCHEMA_FILE = "schema.sql";
    private static final String TEST_DATA_FILE = "test-data.sql";
    private static final String QUERIES_DIR = "queries/";
    private static final String MANDATORY = "mandatory/";
    private static final String OPTIONAL = "optional/";
    private static final String COUNT = "01_count/";
    private static final String SUM = "02_sum/";
    private static final String AVG = "03_avg/";
    private static final String MIN_MAX = "04_min_max/";
    private static final String GROUPING_HAVING = "05_grouping_having/";

    private static final String MANDATORY_COUNT_01 = MANDATORY + COUNT + "01_count_total_customers.sql";
    private static final String MANDATORY_COUNT_02 = MANDATORY + COUNT + "02_count_accounts_by_type.sql";
    private static final String MANDATORY_COUNT_03 = MANDATORY + COUNT + "03_count_customers_with_loans.sql";

    private static final String MANDATORY_SUM_04 = MANDATORY + SUM + "04_sum_find_total_balance.sql";
    private static final String MANDATORY_SUM_05 = MANDATORY + SUM + "05_sum_total_amount_by_transaction_type.sql";
    private static final String MANDATORY_SUM_06 = MANDATORY + SUM + "06_sum_top_customers_by_balance.sql";

    private static final String MANDATORY_AVG_07 = MANDATORY + AVG + "07_average_balance_across_accounts.sql";
    private static final String MANDATORY_AVG_08 = MANDATORY + AVG + "08_average_amount_by_transaction_type.sql";
    private static final String MANDATORY_AVG_09 = MANDATORY + AVG + "09_average_interest_by_loan_status.sql";

    private static final String MANDATORY_MIN_MAX_10 = MANDATORY + MIN_MAX + "10_highest_and_lowest_account_balances.sql";
    private static final String MANDATORY_MIN_MAX_11 = MANDATORY + MIN_MAX + "11_highest_transaction_per_account.sql";
    private static final String MANDATORY_MIN_MAX_12 = MANDATORY + MIN_MAX + "12_min_max_balance.sql";

    private static final String MANDATORY_GROUPING_HAVING_13 = MANDATORY + GROUPING_HAVING + "13_gr_h_customers_with_both_account_types.sql";
    private static final String MANDATORY_GROUPING_HAVING_14 = MANDATORY + GROUPING_HAVING + "14_gr_h_customers_large_loans.sql";
    private static final String MANDATORY_GROUPING_HAVING_15 = MANDATORY + GROUPING_HAVING + "15_gr_h_branches_with_high_salary.sql";

    private static final String OPTIONAL_COUNT_16 = OPTIONAL + COUNT + "16_count_transactions_by_type.sql";
    private static final String OPTIONAL_COUNT_17 = OPTIONAL + COUNT + "17_count_loans_by_status.sql";
    private static final String OPTIONAL_COUNT_18 = OPTIONAL + COUNT + "18_count_employees_by_branch.sql";
    private static final String OPTIONAL_COUNT_19 = OPTIONAL + COUNT + "19_count_accounts_per_customer.sql";
    private static final String OPTIONAL_COUNT_20 = OPTIONAL + COUNT + "20_count_transactions_per_account.sql";
    private static final String OPTIONAL_COUNT_21 = OPTIONAL + COUNT + "21_count_branch_with_most_employees.sql";

    private static final String OPTIONAL_SUM_22 = OPTIONAL + SUM + "22_sum_total_amount_by_loan_status.sql";
    private static final String OPTIONAL_SUM_23 = OPTIONAL + SUM + "23_sum_total_salary_by_branch.sql";
    private static final String OPTIONAL_SUM_24 = OPTIONAL + SUM + "24_sum_total_balance_per_customer.sql";
    private static final String OPTIONAL_SUM_25 = OPTIONAL + SUM + "25_sum_total_amount_per_account.sql";
    private static final String OPTIONAL_SUM_26 = OPTIONAL + SUM + "26_sum_total_balance_by_account_type.sql";

    private static final String OPTIONAL_AVG_27 = OPTIONAL + AVG + "27_average_salary_per_branch.sql";
    private static final String OPTIONAL_AVG_28 = OPTIONAL + AVG + "28_average_balance_per_customer.sql";
    private static final String OPTIONAL_AVG_29 = OPTIONAL + AVG + "29_average_balance_for_account_where_balance_exceeds.sql";
    private static final String OPTIONAL_AVG_30 = OPTIONAL + AVG + "30_average_loan_term.sql";

    private static final String OPTIONAL_MIN_MAX_31 = OPTIONAL + MIN_MAX + "31_min_max_rates_for_loans.sql";
    private static final String OPTIONAL_MIN_MAX_32 = OPTIONAL + MIN_MAX + "32_min_max_loan_amounts.sql";
    private static final String OPTIONAL_MIN_MAX_33 = OPTIONAL + MIN_MAX + "33_min_max_deposit_transactions.sql";
    private static final String OPTIONAL_MIN_MAX_34 = OPTIONAL + MIN_MAX + "34_min_max_saving_balances.sql";

    private static final String OPTIONAL_GROUPING_HAVING_35 = OPTIONAL + GROUPING_HAVING + "35_accounts_withdrawals_exceed_deposits.sql";
    private static final String OPTIONAL_GROUPING_HAVING_36 = OPTIONAL + GROUPING_HAVING + "36_customers_with_multiple_accounts.sql";
    private static final String OPTIONAL_GROUPING_HAVING_37 = OPTIONAL + GROUPING_HAVING + "37_loans_status_exceeds_3.sql";
    private static final String OPTIONAL_GROUPING_HAVING_38 = OPTIONAL + GROUPING_HAVING + "38_branches_total_salaries_exceeds_100k.sql";
    private static final String OPTIONAL_GROUPING_HAVING_39 = OPTIONAL + GROUPING_HAVING + "39_accounts_with_deposits_bigger_then_500.sql";
    private static final String OPTIONAL_GROUPING_HAVING_40 = OPTIONAL + GROUPING_HAVING + "40_avg_balance_multiple_accounts.sql";
    private static final String OPTIONAL_GROUPING_HAVING_41 = OPTIONAL + GROUPING_HAVING + "41_loan_statuses_with_avg_rate.sql";
    private static final String OPTIONAL_GROUPING_HAVING_42 = OPTIONAL + GROUPING_HAVING + "42_tx_types_with_avg_tx_amount_exceeds.sql";
    private static final String OPTIONAL_GROUPING_HAVING_43 = OPTIONAL + GROUPING_HAVING + "43_branches_where_max_salary_exceeds.sql";
    private static final String OPTIONAL_GROUPING_HAVING_44 = OPTIONAL + GROUPING_HAVING + "44_customers_loans_no_accounts.sql";
    private static final String OPTIONAL_GROUPING_HAVING_45 = OPTIONAL + GROUPING_HAVING + "45_branches_with_min_employee_percentage.sql";

    @BeforeAll
    static void startDatabase() throws IOException {
        System.out.println("Starting embedded PostgreSQL...");
        postgres = EmbeddedPostgres.start();
        try {
            connection = postgres.getPostgresDatabase().getConnection();
            connection.setAutoCommit(false); // For transaction control
            System.out.println("PostgreSQL started successfully");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get database connection", e);
        }
    }

    @AfterAll
    static void stopDatabase() throws IOException {
        System.out.println("Stopping embedded PostgreSQL...");
        if (Objects.nonNull(connection)) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }

        if (Objects.nonNull(postgres)) {
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

        System.out.println("Setup complete");
    }

    private void clearDatabase() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("DROP SCHEMA public CASCADE; CREATE SCHEMA public;");
        }
    }

    private void executeSqlFile(String filePath) throws IOException, SQLException {
        Path path = Paths.get(filePath);
        String sql = Files.readString(path);

        try (Statement statement = connection.createStatement()) {
            // Execute each statement separately
            for (String query : sql.split(";")) {
                if (!query.trim().isEmpty()) {
                    statement.execute(query);
                }
            }
        }
    }

    private String getResourcePath(String resourceName) {
        // In a real application, you would use a resource loader
        return "src/test/resources/" + resourceName;
    }

    @Test
    @Order(1)
    void test_01_CountAllCustomers() throws IOException, SQLException {
        List<Map<String, Object>> returnedEntities = executeQueryFromFile(MANDATORY_COUNT_01);

        assertFalse(returnedEntities.isEmpty(), "Result should not be empty");

        Map<String, Object> result = returnedEntities.get(0);

        // Check that the record has exactly 1 field (total_customers)
        assertEquals(1, result.size(), "Result should have exactly 1 field");

        // Verify that the field is named correctly
        assertTrue(result.containsKey("total_customers"), "Result should contain 'total_customers' field");

        assertEquals(1, returnedEntities.size(), "Should return exactly 1 record");
        assertEquals(10, ((Number) result.get("total_customers")).intValue(), "Should have 10 customers");
    }

    @Test
    @Order(2)
    void test_02_CountAccountsByType() throws IOException, SQLException {
        List<Map<String, Object>> returnedEntities = executeQueryFromFile(MANDATORY_COUNT_02);

        assertFalse(returnedEntities.isEmpty(), "Result should not be empty");

        checkSameAmountOfFieldsInAllRecords(returnedEntities);

        Map<String, Object> firstEntity = returnedEntities.get(0);

        // Check that each record has exactly 2 fields (account_type and total_accounts)
        assertEquals(2, firstEntity.size(), "Each record should have exactly 2 fields");

        // Verify that the fields are named correctly
        assertTrue(firstEntity.containsKey("account_type"), "Should contain account_type field");
        assertTrue(firstEntity.containsKey("total_accounts"), "Should contain total_accounts field");

        assertEquals(2, returnedEntities.size(), "Should return exactly 2 records");

        // Create a map of account types to their counts for easy verification
        Map<String, Integer> accountTypeCounts = new HashMap<>();
        for (Map<String, Object> entity : returnedEntities) {
            String accountType = entity.get("account_type").toString().trim();
            int count = ((Number) entity.get("total_accounts")).intValue();
            accountTypeCounts.put(accountType, count);
        }

        // Verify the expected account types and their counts
        assertTrue(accountTypeCounts.containsKey("savings"), "Should include 'savings' account type");
        assertTrue(accountTypeCounts.containsKey("checking"), "Should include 'checking' account type");
        assertEquals(5, accountTypeCounts.get("savings"), "Should have 5 savings accounts");
        assertEquals(5, accountTypeCounts.get("checking"), "Should have 5 checking accounts");
    }

    @Test
    @Order(3)
    void test_03_CustomersWithLoans() throws IOException, SQLException {
        List<Map<String, Object>> returnedEntities = executeQueryFromFile(MANDATORY_COUNT_03);

        assertFalse(returnedEntities.isEmpty(), "Result should not be empty");

        // Check that the record has exactly 1 field (customers_with_loans)
        Map<String, Object> result = returnedEntities.get(0);
        assertEquals(1, result.size(), "Result should have exactly 1 field");

        // Verify that the field is named correctly
        assertTrue(result.containsKey("customers_with_loans"), "Result should contain 'customers_with_loans' field");

        assertEquals(1, returnedEntities.size(), "Should return exactly 1 record");

        // Verify the count value based on the expected result
        int customersWithLoans = ((Number) result.get("customers_with_loans")).intValue();
        assertEquals(10, customersWithLoans, "Total number of customers with loans should be 10");
    }

    @Test
    @Order(4)
    void test_04_FindTotalBalance() throws IOException, SQLException {
        List<Map<String, Object>> returnedEntities = executeQueryFromFile(MANDATORY_SUM_04);

        assertFalse(returnedEntities.isEmpty(), "Result should not be empty");

        // Check that the record has exactly 1 field (total_balance)
        Map<String, Object> result = returnedEntities.get(0);
        assertEquals(1, result.size(), "Result should have exactly 1 field");

        // Verify that the field is named correctly
        assertTrue(result.containsKey("total_balance"), "Result should contain 'total_balance' field");
        assertEquals(1, returnedEntities.size(), "Should return exactly 1 record");

        // Verify the total balance value based on the expected result
        assertEquals(41700, ((Number) result.get("total_balance")).doubleValue(), "Total balance should be 41700");
    }

    @Test
    @Order(5)
    void test_05_SumTransactionAmountsByType() throws IOException, SQLException {
        List<Map<String, Object>> returnedEntities = executeQueryFromFile(MANDATORY_SUM_05);

        assertFalse(returnedEntities.isEmpty(), "Result should not be empty");

        checkSameAmountOfFieldsInAllRecords(returnedEntities);

        // Check that each record has exactly 2 fields (transaction_type and total_amount)
        assertEquals(2, returnedEntities.get(0).size(), "Each record should have exactly 2 fields");

        // Verify that the fields are named correctly
        Map<String, Object> firstEntity = returnedEntities.get(0);
        assertTrue(firstEntity.containsKey("transaction_type"), "Should contain transaction_type field");
        assertTrue(firstEntity.containsKey("total_amount"), "Should contain total_amount field");

        assertEquals(3, returnedEntities.size(), "Should return exactly 3 records");

        // Create a map of transaction types to their total amounts for easy verification
        Map<String, Double> transactionTypeAmounts = new HashMap<>();
        for (Map<String, Object> entity : returnedEntities) {
            String transactionType = entity.get("transaction_type").toString().trim();
            double amount = ((Number) entity.get("total_amount")).doubleValue();
            transactionTypeAmounts.put(transactionType, amount);
        }

        // Verify the expected transaction types and their total amounts
        assertTrue(transactionTypeAmounts.containsKey("deposit"), "Should include 'deposit' transaction type");
        assertTrue(transactionTypeAmounts.containsKey("transfer"), "Should include 'transfer' transaction type");
        assertTrue(transactionTypeAmounts.containsKey("withdrawal"), "Should include 'withdrawal' transaction type");
        assertEquals(2200, transactionTypeAmounts.get("deposit"), "Total deposit amount should be 2200");
        assertEquals(500, transactionTypeAmounts.get("transfer"), "Total transfer amount should be 500");
        assertEquals(1000, transactionTypeAmounts.get("withdrawal"), "Total withdrawal amount should be 1000");
    }

    @Test
    @Order(6)
    void test_06_TopCustomersByBalance() throws IOException, SQLException {
        List<Map<String, Object>> returnedEntities = executeQueryFromFile(MANDATORY_SUM_06);

        assertFalse(returnedEntities.isEmpty(), "Result should not be empty");

        checkSameAmountOfFieldsInAllRecords(returnedEntities);

        // Check that each record has exactly 2 fields (customer_id and total_balance)
        assertEquals(2, returnedEntities.get(0).size(), "Each record should have exactly 2 fields");

        // Verify that the fields are named correctly
        Map<String, Object> firstEntity = returnedEntities.get(0);
        assertTrue(firstEntity.containsKey("customer_id"), "Should contain 'customer_id' field");
        assertTrue(firstEntity.containsKey("total_balance"), "Should contain 'total_balance' field");

        assertEquals(3, returnedEntities.size(), "Should return exactly 3 records");

        Map<Integer, Double> customersBalances = new HashMap<>();
        for (Map<String, Object> entry : returnedEntities) {
            int customerId = ((Number) entry.get("customer_id")).intValue();
            double totalBalance = ((Number) entry.get("total_balance")).doubleValue();
            customersBalances.put(customerId, totalBalance);
        }

        // Verify the expected account types and their total balances
        assertTrue(customersBalances.containsKey(1), "Should include customer with ID 1");
        assertTrue(customersBalances.containsKey(5), "Should include customer with ID 5");
        assertTrue(customersBalances.containsKey(9), "Should include customer with ID 9");

        assertEquals(1, ((Number) returnedEntities.get(0).get("customer_id")).intValue(), "First place should be customer ID 1");
        assertEquals(5, ((Number) returnedEntities.get(2).get("customer_id")).intValue(), "Third place should be customer ID 5");
        assertEquals(5, ((Number) returnedEntities.get(2).get("customer_id")).intValue(), "Third place should be customer ID 5");

        assertEquals(11500.0, customersBalances.get(1), "Customer 1 should have total balance of 11500");
        assertEquals(7000.0, customersBalances.get(5), "Customer 5 should have total balance of 7000");
        assertEquals(11000.0, customersBalances.get(9), "Customer 9 should have total balance of 11000");
    }

    @Test
    @Order(7)
    void test_07_FindAverageBalanceAcrossAllAccounts() throws IOException, SQLException {
        List<Map<String, Object>> returnedEntities = executeQueryFromFile(MANDATORY_AVG_07);

        assertFalse(returnedEntities.isEmpty(), "Result should not be empty");

        // Check that the record has exactly 1 field (avg_balance)
        Map<String, Object> result = returnedEntities.get(0);
        assertEquals(1, result.size(), "Result should have exactly 1 field");

        // Verify that the field is named correctly
        assertTrue(result.containsKey("avg_balance"), "Result should contain 'avg_balance' field");

        assertEquals(1, returnedEntities.size(), "Should return exactly 1 record");

        // Verify the average balance value based on the expected result
        assertEquals(4170, ((Number) result.get("avg_balance")).doubleValue(), "Total balance should be 4170");
    }

    @Test
    @Order(8)
    void test_08_AverageTransactionAmountsByType() throws IOException, SQLException {
        List<Map<String, Object>> returnedEntities = executeQueryFromFile(MANDATORY_AVG_08);

        assertFalse(returnedEntities.isEmpty(), "Result should not be empty");

        checkSameAmountOfFieldsInAllRecords(returnedEntities);

        // Check that each record has exactly 2 fields (transaction_type and avg_transaction)
        assertEquals(2, returnedEntities.get(0).size(), "Each record should have exactly 2 fields");

        // Verify that the fields are named correctly
        Map<String, Object> firstEntity = returnedEntities.get(0);
        assertTrue(firstEntity.containsKey("transaction_type"), "Should contain transaction_type field");
        assertTrue(firstEntity.containsKey("avg_transaction"), "Should contain avg_transaction field");

        assertEquals(3, returnedEntities.size(), "Should return exactly 3 records");

        // Create a map of transaction types to their average amounts for easy verification
        Map<String, Double> transactionTypeAvgAmounts = new HashMap<>();
        for (Map<String, Object> entity : returnedEntities) {
            String transactionType = entity.get("transaction_type").toString().trim();
            double avgAmount = ((Number) entity.get("avg_transaction")).doubleValue();
            transactionTypeAvgAmounts.put(transactionType, avgAmount);
        }

        // Verify the expected transaction types and their average amounts
        assertTrue(transactionTypeAvgAmounts.containsKey("deposit"), "Should include 'deposit' transaction type");
        assertTrue(transactionTypeAvgAmounts.containsKey("transfer"), "Should include 'transfer' transaction type");
        assertTrue(transactionTypeAvgAmounts.containsKey("withdrawal"), "Should include 'withdrawal' transaction type");

        // Using delta for floating point comparison
        double delta = 0.001;
        assertEquals(733.333, transactionTypeAvgAmounts.get("deposit"), delta,
                "Average deposit amount should be approximately 733.33");
        assertEquals(250.0, transactionTypeAvgAmounts.get("transfer"), delta,
                "Average transfer amount should be 250.0");
        assertEquals(333.333, transactionTypeAvgAmounts.get("withdrawal"), delta,
                "Average withdrawal amount should be approximately 333.33");
    }

    @Test
    @Order(9)
    void test_09_AverageInterestRateByLoanStatus() throws IOException, SQLException {
        List<Map<String, Object>> returnedEntities = executeQueryFromFile(MANDATORY_AVG_09);

        assertFalse(returnedEntities.isEmpty(), "Result should not be empty");

        checkSameAmountOfFieldsInAllRecords(returnedEntities);

        // Check that each record has exactly 2 fields (status and avg_interest)
        assertEquals(2, returnedEntities.get(0).size(), "Each record should have exactly 2 fields");

        // Verify that the fields are named correctly
        Map<String, Object> firstEntity = returnedEntities.get(0);
        assertTrue(firstEntity.containsKey("status"), "Should contain status field");
        assertTrue(firstEntity.containsKey("avg_interest"), "Should contain avg_interest field");

        assertEquals(3, returnedEntities.size(), "Should return exactly 3 records");

        // Create a map of loan statuses to their average interest rates for easy verification
        Map<String, Double> loanStatusAvgInterest = new HashMap<>();
        for (Map<String, Object> entity : returnedEntities) {
            String status = entity.get("status").toString().trim();
            double avgInterest = ((Number) entity.get("avg_interest")).doubleValue();
            loanStatusAvgInterest.put(status, avgInterest);
        }

        // Verify the expected loan statuses and their average interest rates
        assertTrue(loanStatusAvgInterest.containsKey("active"), "Should include 'active' loan status");
        assertTrue(loanStatusAvgInterest.containsKey("closed"), "Should include 'closed' loan status");
        assertTrue(loanStatusAvgInterest.containsKey("defaulted"), "Should include 'defaulted' loan status");

        // Using delta for floating point comparison
        double delta = 0.001;
        assertEquals(4.667, loanStatusAvgInterest.get("active"), delta,
                "Average interest rate for active loans should be approximately 4.67%");
        assertEquals(5.367, loanStatusAvgInterest.get("closed"), delta,
                "Average interest rate for closed loans should be approximately 5.37%");
        assertEquals(3.9, loanStatusAvgInterest.get("defaulted"), delta,
                "Average interest rate for defaulted loans should be 3.9%");
    }

    @Test
    @Order(10)
    void test_10_FindHighestAndLowestAccountBalances() throws IOException, SQLException {
        List<Map<String, Object>> returnedEntities = executeQueryFromFile(MANDATORY_MIN_MAX_10);

        assertFalse(returnedEntities.isEmpty(), "Result should not be empty");

        // Check that the record has exactly 2 fields
        Map<String, Object> result = returnedEntities.get(0);
        assertEquals(2, result.size(), "Result should have exactly 2 fields");

        // Verify that the fields are named correctly
        assertTrue(result.containsKey("max_balance"), "Result should contain 'max_balance' field");
        assertTrue(result.containsKey("min_balance"), "Result should contain 'min_balance' field");

        assertEquals(1, returnedEntities.size(), "Should return exactly 1 record");

        assertEquals(11000, ((Number) result.get("max_balance")).doubleValue(), "Maximum balance should be 11000");
        assertEquals(500, ((Number) result.get("min_balance")).doubleValue(), "Minimum balance should be 500");
    }

    @Test
    @Order(11)
    void test_11_HighestTransactionPerAccount() throws IOException, SQLException {
        List<Map<String, Object>> returnedEntities = executeQueryFromFile(MANDATORY_MIN_MAX_11);

        assertFalse(returnedEntities.isEmpty(), "Result should not be empty");

        checkSameAmountOfFieldsInAllRecords(returnedEntities);

        // Check that each record has exactly 2 fields (account_id and max_transaction)
        assertEquals(2, returnedEntities.get(0).size(), "Each record should have exactly 2 fields");

        // Verify that the fields are named correctly
        Map<String, Object> firstEntity = returnedEntities.get(0);
        assertTrue(firstEntity.containsKey("account_id"), "Should contain 'account_id' field");
        assertTrue(firstEntity.containsKey("max_transaction"), "Should contain 'max_transaction' field");

        assertEquals(8, returnedEntities.size(), "Should return exactly 8 records");

        // Create a map of account IDs to their highest transaction amounts for easy verification
        Map<Integer, Double> accountMaxTransactions = new HashMap<>();
        for (Map<String, Object> entity : returnedEntities) {
            int accountId = ((Number) entity.get("account_id")).intValue();
            double maxTransaction = ((Number) entity.get("max_transaction")).doubleValue();
            accountMaxTransactions.put(accountId, maxTransaction);
        }

        // Verify the expected account IDs and their highest transaction amounts
        assertTrue(accountMaxTransactions.containsKey(1), "Should include account with ID 1");
        assertTrue(accountMaxTransactions.containsKey(2), "Should include account with ID 2");
        assertTrue(accountMaxTransactions.containsKey(3), "Should include account with ID 3");
        assertTrue(accountMaxTransactions.containsKey(5), "Should include account with ID 5");
        assertTrue(accountMaxTransactions.containsKey(6), "Should include account with ID 6");
        assertTrue(accountMaxTransactions.containsKey(7), "Should include account with ID 7");
        assertTrue(accountMaxTransactions.containsKey(9), "Should include account with ID 9");
        assertTrue(accountMaxTransactions.containsKey(10), "Should include account with ID 10");

        // Using delta for floating point comparison
        double delta = 0.001;
        assertEquals(500.0, accountMaxTransactions.get(1), delta, "Account 1 should have highest transaction amount of 500");
        assertEquals(200.0, accountMaxTransactions.get(2), delta, "Account 2 should have highest transaction amount of 200");
        assertEquals(300.0, accountMaxTransactions.get(3), delta, "Account 3 should have highest transaction amount of 300");
        assertEquals(1000.0, accountMaxTransactions.get(5), delta, "Account 5 should have highest transaction amount of 1000");
        assertEquals(500.0, accountMaxTransactions.get(6), delta, "Account 6 should have highest transaction amount of 500");
        assertEquals(200.0, accountMaxTransactions.get(7), delta, "Account 7 should have highest transaction amount of 200");
        assertEquals(700.0, accountMaxTransactions.get(9), delta, "Account 9 should have highest transaction amount of 700");
        assertEquals(300.0, accountMaxTransactions.get(10), delta, "Account 10 should have highest transaction amount of 300");
    }

    @Test
    @Order(12)
    void test_12_MinAndMaxBalancesByCondition() throws IOException, SQLException {
        List<Map<String, Object>> returnedEntities = executeQueryFromFile(MANDATORY_MIN_MAX_12);

        assertFalse(returnedEntities.isEmpty(), "Result should not be empty");

        // Check that the record has exactly 2 fields
        Map<String, Object> result = returnedEntities.get(0);
        assertEquals(2, result.size(), "Result should have exactly 2 fields");

        // Verify that the field is named correctly
        assertTrue(result.containsKey("lowest_balance"), "Result should contain 'lowest_balance' field");
        assertTrue(result.containsKey("highest_balance"), "Result should contain 'highest_balance' field");

        assertEquals(1, returnedEntities.size(), "Should return exactly 1 record");

        // Assuming the timestamp is returned as a Timestamp object
        assertEquals(500.00, ((Number) result.get("lowest_balance")).doubleValue(), "Lowest balance should be 500.00");
        assertEquals(3000.00, ((Number) result.get("highest_balance")).doubleValue(), "Highest balance should be 3000.00");
    }

    @Test
    @Order(13)
    void test_13_CustomersWithBothAccountTypes() throws IOException, SQLException {
        List<Map<String, Object>> returnedEntities = executeQueryFromFile(MANDATORY_GROUPING_HAVING_13);

        assertFalse(returnedEntities.isEmpty(), "Result should not be empty");

        // Check that the record has exactly 1 field (customer_id)
        assertEquals(1, returnedEntities.get(0).size(), "Each record should have exactly 1 field");

        // Verify that the field is named correctly
        Map<String, Object> firstEntity = returnedEntities.get(0);
        assertTrue(firstEntity.containsKey("customer_id"), "Should contain 'customer_id' field");
        assertEquals(1, returnedEntities.size(), "Should return exactly 1 record");

        // Verify the expected customer ID
        int customerId = ((Number) firstEntity.get("customer_id")).intValue();
        assertEquals(1, customerId, "Should be customer ID 1 who has both account types");
    }

    @Test
    @Order(14)
    void test_14_CustomersWithLargeLoans() throws IOException, SQLException {
        List<Map<String, Object>> returnedEntities = executeQueryFromFile(MANDATORY_GROUPING_HAVING_14);

        assertFalse(returnedEntities.isEmpty(), "Result should not be empty");

        checkSameAmountOfFieldsInAllRecords(returnedEntities);

        // Check that each record has exactly 2 fields (customer_id and total_loan)
        assertEquals(2, returnedEntities.get(0).size(), "Each record should have exactly 2 fields");

        // Verify that the fields are named correctly
        Map<String, Object> firstEntity = returnedEntities.get(0);
        assertTrue(firstEntity.containsKey("customer_id"), "Should contain 'customer_id' field");
        assertTrue(firstEntity.containsKey("total_loan"), "Should contain 'total_loan' field");
        assertEquals(2, returnedEntities.size(), "Should return exactly 2 records");

        // Create a map of customer IDs to their total loan amounts for verification
        Map<Integer, Double> customerLoans = new HashMap<>();
        for (Map<String, Object> entity : returnedEntities) {
            int customerId = ((Number) entity.get("customer_id")).intValue();
            double totalLoan = ((Number) entity.get("total_loan")).doubleValue();
            customerLoans.put(customerId, totalLoan);
        }

        // Verify that all returned loan amounts are greater than 15000
        for (Double loanAmount : customerLoans.values()) {
            assertTrue(loanAmount > 15000, "All loan amounts should be greater than $15,000");
        }

        // Verify the expected customer IDs and their total loan amounts
        assertTrue(customerLoans.containsKey(5), "Should include customer ID 5");
        assertTrue(customerLoans.containsKey(10), "Should include customer ID 10");
        assertEquals(20000.0, customerLoans.get(5), 0.01, "Customer 5 should have $20,000 in loans");
        assertEquals(18000.0, customerLoans.get(10), 0.01, "Customer 10 should have $18,000 in loans");
    }

    @Test
    @Order(15)
    void test_15_BranchesWithHighSalary() throws IOException, SQLException {
        List<Map<String, Object>> returnedEntities = executeQueryFromFile(MANDATORY_GROUPING_HAVING_15);

        assertFalse(returnedEntities.isEmpty(), "Result should not be empty");

        checkSameAmountOfFieldsInAllRecords(returnedEntities);

        // Check that each record has exactly 2 fields (branch_id and avg_salary)
        assertEquals(2, returnedEntities.get(0).size(), "Each record should have exactly 2 fields");

        // Verify that the fields are named correctly
        Map<String, Object> firstEntity = returnedEntities.get(0);
        assertTrue(firstEntity.containsKey("branch_id"), "Should contain 'branch_id' field");
        assertTrue(firstEntity.containsKey("avg_salary"), "Should contain 'avg_salary' field");
        assertEquals(2, returnedEntities.size(), "Should return exactly 4 records");

        // Create a map of branch IDs to their avg salaries for verification
        Map<Integer, Double> branchSalaries = new HashMap<>();
        for (Map<String, Object> entity : returnedEntities) {
            int branchId = ((Number) entity.get("branch_id")).intValue();
            double avgSalary = ((Number) entity.get("avg_salary")).doubleValue();
            branchSalaries.put(branchId, avgSalary);
        }

        // Verify that all returned average salaries are greater than 50000
        for (Double salary : branchSalaries.values()) {
            assertTrue(salary > 50000, "All average salaries amounts should be greater than $50,000");
        }

        // Verify the expected branch IDs and their average salaries
        assertTrue(branchSalaries.containsKey(1), "Should include branch ID 1");
        assertTrue(branchSalaries.containsKey(3), "Should include branch ID 3");
        assertEquals(55000.0, branchSalaries.get(1), "Branch 1 should have $55,000 as avg salary");
        assertEquals(51500.0, branchSalaries.get(3), "Branch 3 should have $51,500 as avg salary");
    }

    @Test
    @Order(16)
    void test_16_CountTransactionsByType() throws IOException, SQLException {
        List<Map<String, Object>> returnedEntities = executeQueryFromFile(OPTIONAL_COUNT_16);

        assertFalse(returnedEntities.isEmpty(), "Result should not be empty");

        checkSameAmountOfFieldsInAllRecords(returnedEntities);

        // Check that each record has exactly 2 fields (transaction_type and transaction_count)
        assertEquals(2, returnedEntities.get(0).size(), "Each record should have exactly 2 fields");

        // Verify that the fields are named correctly
        Map<String, Object> firstEntity = returnedEntities.get(0);
        assertTrue(firstEntity.containsKey("transaction_type"), "Should contain transaction_type field");
        assertTrue(firstEntity.containsKey("transaction_count"), "Should contain transaction_count field");
        assertEquals(3, returnedEntities.size(), "Should return exactly 3 records");

        // Create a map of transaction types to their counts for easy verification
        Map<String, Integer> transactionTypeCounts = new HashMap<>();
        for (Map<String, Object> entity : returnedEntities) {
            String transactionType = entity.get("transaction_type").toString().trim();
            int count = ((Number) entity.get("transaction_count")).intValue();
            transactionTypeCounts.put(transactionType, count);
        }

        // Verify the expected transaction types and their counts
        assertTrue(transactionTypeCounts.containsKey("deposit"), "Should include 'deposit' transaction type");
        assertTrue(transactionTypeCounts.containsKey("transfer"), "Should include 'transfer' transaction type");
        assertTrue(transactionTypeCounts.containsKey("withdrawal"), "Should include 'withdrawal' transaction type");
        assertEquals(3, transactionTypeCounts.get("deposit"), "Should have 3 deposit transactions");
        assertEquals(2, transactionTypeCounts.get("transfer"), "Should have 2 transfer transactions");
        assertEquals(3, transactionTypeCounts.get("withdrawal"), "Should have 3 withdrawal transactions");
    }

    @Test
    @Order(17)
    void test_17_CountLoansByStatus() throws IOException, SQLException {
        List<Map<String, Object>> returnedEntities = executeQueryFromFile(OPTIONAL_COUNT_17);

        assertFalse(returnedEntities.isEmpty(), "Result should not be empty");

        checkSameAmountOfFieldsInAllRecords(returnedEntities);

        // Check that each record has exactly 2 fields (status and total_loans)
        assertEquals(2, returnedEntities.get(0).size(), "Each record should have exactly 2 fields");

        // Verify that the fields are named correctly
        Map<String, Object> firstEntity = returnedEntities.get(0);
        assertTrue(firstEntity.containsKey("status"), "Should contain status field");
        assertTrue(firstEntity.containsKey("total_loans"), "Should contain total_loans field");
        assertEquals(3, returnedEntities.size(), "Should return exactly 3 records");

        // Create a map of loan statuses to their counts for easy verification
        Map<String, Integer> loanStatusCounts = new HashMap<>();
        for (Map<String, Object> entity : returnedEntities) {
            String status = entity.get("status").toString().trim();
            int count = ((Number) entity.get("total_loans")).intValue();
            loanStatusCounts.put(status, count);
        }

        // Verify the expected loan statuses and their counts
        assertTrue(loanStatusCounts.containsKey("active"), "Should include 'active' loan status");
        assertTrue(loanStatusCounts.containsKey("closed"), "Should include 'closed' loan status");
        assertTrue(loanStatusCounts.containsKey("defaulted"), "Should include 'defaulted' loan status");
        assertEquals(6, loanStatusCounts.get("active"), "Should have 6 active loans");
        assertEquals(3, loanStatusCounts.get("closed"), "Should have 3 closed loans");
        assertEquals(1, loanStatusCounts.get("defaulted"), "Should have 1 defaulted loan");
    }

    @Test
    @Order(18)
    void test_18_CountEmployeesByBranch() throws IOException, SQLException {
        List<Map<String, Object>> returnedEntities = executeQueryFromFile(OPTIONAL_COUNT_18);

        assertFalse(returnedEntities.isEmpty(), "Result should not be empty");

        checkSameAmountOfFieldsInAllRecords(returnedEntities);

        // Check that each record has exactly 2 fields (branch_id and employee_count)
        assertEquals(2, returnedEntities.get(0).size(), "Each record should have exactly 2 fields");

        // Verify that the fields are named correctly
        Map<String, Object> firstEntity = returnedEntities.get(0);
        assertTrue(firstEntity.containsKey("branch_id"), "Should contain branch_id field");
        assertTrue(firstEntity.containsKey("employee_count"), "Should contain employee_count field");
        assertEquals(4, returnedEntities.size(), "Should return exactly 4 records");

        // Create a map of branch IDs to their employee counts for easy verification
        Map<Integer, Integer> branchEmployeeCounts = new HashMap<>();
        for (Map<String, Object> entity : returnedEntities) {
            int branchId = ((Number) entity.get("branch_id")).intValue();
            int count = ((Number) entity.get("employee_count")).intValue();
            branchEmployeeCounts.put(branchId, count);
        }

        // Verify the expected branch IDs and their employee counts
        assertTrue(branchEmployeeCounts.containsKey(1), "Should include branch with ID 1");
        assertTrue(branchEmployeeCounts.containsKey(2), "Should include branch with ID 2");
        assertTrue(branchEmployeeCounts.containsKey(3), "Should include branch with ID 3");
        assertTrue(branchEmployeeCounts.containsKey(4), "Should include branch with ID 4");
        assertEquals(2, branchEmployeeCounts.get(1), "Branch 1 should have 2 employees");
        assertEquals(2, branchEmployeeCounts.get(2), "Branch 2 should have 2 employees");
        assertEquals(2, branchEmployeeCounts.get(3), "Branch 3 should have 2 employees");
        assertEquals(2, branchEmployeeCounts.get(4), "Branch 4 should have 2 employees");
    }

    @Test
    @Order(19)
    void test_19_AccountsPerCustomer() throws IOException, SQLException {
        List<Map<String, Object>> returnedEntities = executeQueryFromFile(OPTIONAL_COUNT_19);

        assertFalse(returnedEntities.isEmpty(), "Result should not be empty");

        checkSameAmountOfFieldsInAllRecords(returnedEntities);

        // Check that each record has exactly 2 fields (customer_id and account_count)
        assertEquals(2, returnedEntities.get(0).size(), "Each record should have exactly 2 fields");

        // Verify that the fields are named correctly
        Map<String, Object> firstEntity = returnedEntities.get(0);
        assertTrue(firstEntity.containsKey("customer_id"), "Should contain customer_id field");
        assertTrue(firstEntity.containsKey("account_count"), "Should contain account_count field");
        assertEquals(9, returnedEntities.size(), "Should return exactly 9 records");

        // Create a map of customer IDs to their account counts for easy verification
        Map<Integer, Integer> customerAccountCounts = new HashMap<>();
        for (Map<String, Object> entity : returnedEntities) {
            int customerId = ((Number) entity.get("customer_id")).intValue();
            int accountCount = ((Number) entity.get("account_count")).intValue();
            customerAccountCounts.put(customerId, accountCount);
        }

        // Verify the expected customer IDs and their account counts
        assertTrue(customerAccountCounts.containsKey(1), "Should include customer with ID 1");
        assertTrue(customerAccountCounts.containsKey(2), "Should include customer with ID 2");
        assertTrue(customerAccountCounts.containsKey(3), "Should include customer with ID 3");
        assertTrue(customerAccountCounts.containsKey(4), "Should include customer with ID 4");
        assertTrue(customerAccountCounts.containsKey(5), "Should include customer with ID 5");
        assertTrue(customerAccountCounts.containsKey(6), "Should include customer with ID 6");
        assertTrue(customerAccountCounts.containsKey(7), "Should include customer with ID 7");
        assertTrue(customerAccountCounts.containsKey(8), "Should include customer with ID 8");
        assertTrue(customerAccountCounts.containsKey(9), "Should include customer with ID 9");

        assertEquals(2, customerAccountCounts.get(1), "Customer 1 should have 2 accounts");
        assertEquals(1, customerAccountCounts.get(2), "Customer 2 should have 1 account");
        assertEquals(1, customerAccountCounts.get(3), "Customer 3 should have 1 account");
        assertEquals(1, customerAccountCounts.get(4), "Customer 4 should have 1 account");
        assertEquals(1, customerAccountCounts.get(5), "Customer 5 should have 1 account");
        assertEquals(1, customerAccountCounts.get(6), "Customer 6 should have 1 account");
        assertEquals(1, customerAccountCounts.get(7), "Customer 7 should have 1 account");
        assertEquals(1, customerAccountCounts.get(8), "Customer 8 should have 1 account");
        assertEquals(1, customerAccountCounts.get(9), "Customer 9 should have 1 account");
    }

    @Test
    @Order(20)
    void test_20_CountTransactionsPerAccount() throws IOException, SQLException {
        List<Map<String, Object>> returnedEntities = executeQueryFromFile(OPTIONAL_COUNT_20);

        assertFalse(returnedEntities.isEmpty(), "Result should not be empty");

        checkSameAmountOfFieldsInAllRecords(returnedEntities);

        // Check that each record has exactly 2 fields (account_id and transaction_count)
        assertEquals(2, returnedEntities.get(0).size(), "Each record should have exactly 2 fields");

        // Verify that the fields are named correctly
        Map<String, Object> firstEntity = returnedEntities.get(0);
        assertTrue(firstEntity.containsKey("account_id"), "Should contain account_id field");
        assertTrue(firstEntity.containsKey("transaction_count"), "Should contain transaction_count field");
        assertEquals(8, returnedEntities.size(), "Should return exactly 8 records");

        // Create a map of account IDs to their transaction counts for easy verification
        Map<Integer, Integer> accountTransactionCounts = new HashMap<>();
        for (Map<String, Object> entity : returnedEntities) {
            int accountId = ((Number) entity.get("account_id")).intValue();
            int transactionCount = ((Number) entity.get("transaction_count")).intValue();
            accountTransactionCounts.put(accountId, transactionCount);
        }

        // Verify the expected account IDs and their transaction counts
        assertTrue(accountTransactionCounts.containsKey(1), "Should include account with ID 1");
        assertTrue(accountTransactionCounts.containsKey(2), "Should include account with ID 2");
        assertTrue(accountTransactionCounts.containsKey(3), "Should include account with ID 3");
        assertTrue(accountTransactionCounts.containsKey(5), "Should include account with ID 5");
        assertTrue(accountTransactionCounts.containsKey(6), "Should include account with ID 6");
        assertTrue(accountTransactionCounts.containsKey(7), "Should include account with ID 7");
        assertTrue(accountTransactionCounts.containsKey(9), "Should include account with ID 9");
        assertTrue(accountTransactionCounts.containsKey(10), "Should include account with ID 10");

        // Verify the exact transaction count for each account
        assertEquals(1, accountTransactionCounts.get(1), "Account 1 should have 1 transaction");
        assertEquals(1, accountTransactionCounts.get(2), "Account 2 should have 1 transaction");
        assertEquals(1, accountTransactionCounts.get(3), "Account 3 should have 1 transaction");
        assertEquals(1, accountTransactionCounts.get(5), "Account 5 should have 1 transaction");
        assertEquals(1, accountTransactionCounts.get(6), "Account 6 should have 1 transaction");
        assertEquals(1, accountTransactionCounts.get(7), "Account 7 should have 1 transaction");
        assertEquals(1, accountTransactionCounts.get(9), "Account 9 should have 1 transaction");
        assertEquals(1, accountTransactionCounts.get(10), "Account 10 should have 1 transaction");
    }

    @Test
    @Order(21)
    void test_21_BranchWithMostEmployees() throws IOException, SQLException {
        List<Map<String, Object>> returnedEntities = executeQueryFromFile(OPTIONAL_COUNT_21);

        assertFalse(returnedEntities.isEmpty(), "Result should not be empty");

        // Check that record has exactly 2 fields (branch_id and employee_count)
        assertEquals(2, returnedEntities.get(0).size(), "Record should have exactly 2 fields");

        // Verify that the fields are named correctly
        Map<String, Object> firstEntity = returnedEntities.get(0);
        assertTrue(firstEntity.containsKey("branch_id"), "Should contain 'branch_id' field");
        assertTrue(firstEntity.containsKey("employee_count"), "Should contain 'employee_count' field");
        assertEquals(1, returnedEntities.size(), "Should return exactly 1 record");

        assertEquals(3, ((Number) firstEntity.get("branch_id")).intValue(), "Should include branch with ID 3");
        assertEquals(2, ((Number) firstEntity.get("employee_count")).intValue(), "Branch should have 2 employees");
    }

    @Test
    @Order(22)
    void test_22_SumLoanAmountsByStatus() throws IOException, SQLException {
        List<Map<String, Object>> returnedEntities = executeQueryFromFile(OPTIONAL_SUM_22);

        assertFalse(returnedEntities.isEmpty(), "Result should not be empty");

        checkSameAmountOfFieldsInAllRecords(returnedEntities);

        // Check that each record has exactly 2 fields (status and total_loan_amount)
        assertEquals(2, returnedEntities.get(0).size(), "Each record should have exactly 2 fields");

        // Verify that the fields are named correctly
        Map<String, Object> firstEntity = returnedEntities.get(0);
        assertTrue(firstEntity.containsKey("status"), "Should contain status field");
        assertTrue(firstEntity.containsKey("total_loan_amount"), "Should contain total_loan_amount field");
        assertEquals(3, returnedEntities.size(), "Should return exactly 3 records");

        // Create a map of loan statuses to their total amounts for easy verification
        Map<String, Double> loanStatusAmounts = new HashMap<>();
        for (Map<String, Object> entity : returnedEntities) {
            String status = entity.get("status").toString().trim();
            double amount = ((Number) entity.get("total_loan_amount")).doubleValue();
            loanStatusAmounts.put(status, amount);
        }

        // Verify the expected loan statuses and their total amounts
        assertTrue(loanStatusAmounts.containsKey("active"), "Should include 'active' loan status");
        assertTrue(loanStatusAmounts.containsKey("closed"), "Should include 'closed' loan status");
        assertTrue(loanStatusAmounts.containsKey("defaulted"), "Should include 'defaulted' loan status");
        assertEquals(73000, loanStatusAmounts.get("active"), "Total amount for active loans should be 73000");
        assertEquals(32000, loanStatusAmounts.get("closed"), "Total amount for closed loans should be 32000");
        assertEquals(15000, loanStatusAmounts.get("defaulted"), "Total amount for defaulted loans should be 15000");
    }

    @Test
    @Order(23)
    void test_23_TotalSalaryExpenditurePerBranch() throws IOException, SQLException {
        List<Map<String, Object>> returnedEntities = executeQueryFromFile(OPTIONAL_SUM_23);

        assertFalse(returnedEntities.isEmpty(), "Result should not be empty");

        checkSameAmountOfFieldsInAllRecords(returnedEntities);

        // Check that each record has exactly 2 fields (branch_id and total_salary)
        assertEquals(2, returnedEntities.get(0).size(), "Each record should have exactly 2 fields");

        // Verify that the fields are named correctly
        Map<String, Object> firstEntity = returnedEntities.get(0);
        assertTrue(firstEntity.containsKey("branch_id"), "Should contain branch_id field");
        assertTrue(firstEntity.containsKey("total_salary"), "Should contain total_salary field");
        assertEquals(4, returnedEntities.size(), "Should return exactly 4 records");

        // Create a map of branch IDs to their total salary expenditure for easy verification
        Map<Integer, Integer> branchSalaryTotals = new HashMap<>();
        for (Map<String, Object> entity : returnedEntities) {
            int branchId = ((Number) entity.get("branch_id")).intValue();
            int totalSalary = ((Number) entity.get("total_salary")).intValue();
            branchSalaryTotals.put(branchId, totalSalary);
        }

        // Verify the expected branch IDs and their total salary expenditures
        assertTrue(branchSalaryTotals.containsKey(1), "Should include branch with ID 1");
        assertTrue(branchSalaryTotals.containsKey(2), "Should include branch with ID 2");
        assertTrue(branchSalaryTotals.containsKey(3), "Should include branch with ID 3");
        assertTrue(branchSalaryTotals.containsKey(4), "Should include branch with ID 4");
        assertEquals(110000, branchSalaryTotals.get(1), "Branch 1 should have total salary expenditure of 110000");
        assertEquals(85000, branchSalaryTotals.get(2), "Branch 2 should have total salary expenditure of 85000");
        assertEquals(103000, branchSalaryTotals.get(3), "Branch 3 should have total salary expenditure of 103000");
        assertEquals(92000, branchSalaryTotals.get(4), "Branch 4 should have total salary expenditure of 92000");
    }

    @Test
    @Order(24)
    void test_24_TotalBalancePerCustomer() throws IOException, SQLException {
        List<Map<String, Object>> returnedEntities = executeQueryFromFile(OPTIONAL_SUM_24);

        assertFalse(returnedEntities.isEmpty(), "Result should not be empty");

        checkSameAmountOfFieldsInAllRecords(returnedEntities);

        // Check that each record has exactly 2 fields (customer_id and total_balance)
        assertEquals(2, returnedEntities.get(0).size(), "Each record should have exactly 2 fields");

        // Verify that the fields are named correctly
        Map<String, Object> firstEntity = returnedEntities.get(0);
        assertTrue(firstEntity.containsKey("customer_id"), "Should contain customer_id field");
        assertTrue(firstEntity.containsKey("total_balance"), "Should contain total_balance field");
        assertEquals(9, returnedEntities.size(), "Should return exactly 9 records");

        // Create a map of customer IDs to their total balances for easy verification
        Map<Integer, Integer> customerTotalBalances = new HashMap<>();
        for (Map<String, Object> entity : returnedEntities) {
            int customerId = ((Number) entity.get("customer_id")).intValue();
            int totalBalance = ((Number) entity.get("total_balance")).intValue();
            customerTotalBalances.put(customerId, totalBalance);
        }

        // Verify the expected customer IDs and their total balances
        assertTrue(customerTotalBalances.containsKey(1), "Should include customer with ID 1");
        assertTrue(customerTotalBalances.containsKey(2), "Should include customer with ID 2");
        assertTrue(customerTotalBalances.containsKey(3), "Should include customer with ID 3");
        assertTrue(customerTotalBalances.containsKey(4), "Should include customer with ID 4");
        assertTrue(customerTotalBalances.containsKey(5), "Should include customer with ID 5");
        assertTrue(customerTotalBalances.containsKey(6), "Should include customer with ID 6");
        assertTrue(customerTotalBalances.containsKey(7), "Should include customer with ID 7");
        assertTrue(customerTotalBalances.containsKey(8), "Should include customer with ID 8");
        assertTrue(customerTotalBalances.containsKey(9), "Should include customer with ID 9");

        assertEquals(11500, customerTotalBalances.get(1), "Customer 1 should have total balance of 11500");
        assertEquals(1200, customerTotalBalances.get(2), "Customer 2 should have total balance of 1200");
        assertEquals(3000, customerTotalBalances.get(3), "Customer 3 should have total balance of 3000");
        assertEquals(2000, customerTotalBalances.get(4), "Customer 4 should have total balance of 2000");
        assertEquals(7000, customerTotalBalances.get(5), "Customer 5 should have total balance of 7000");
        assertEquals(500, customerTotalBalances.get(6), "Customer 6 should have total balance of 500");
        assertEquals(2500, customerTotalBalances.get(7), "Customer 7 should have total balance of 2500");
        assertEquals(3000, customerTotalBalances.get(8), "Customer 8 should have total balance of 3000");
        assertEquals(11000, customerTotalBalances.get(9), "Customer 9 should have total balance of 11000");
    }

    @Test
    @Order(25)
    void test_25_TotalAmountPerAccount() throws IOException, SQLException {
        List<Map<String, Object>> returnedEntities = executeQueryFromFile(OPTIONAL_SUM_25);

        assertFalse(returnedEntities.isEmpty(), "Result should not be empty");

        checkSameAmountOfFieldsInAllRecords(returnedEntities);

        // Check that each record has exactly 2 fields (account_id and total_amount)
        assertEquals(2, returnedEntities.get(0).size(), "Each record should have exactly 2 fields");

        // Verify that the fields are named correctly
        Map<String, Object> firstEntity = returnedEntities.get(0);
        assertTrue(firstEntity.containsKey("account_id"), "Should contain account_id field");
        assertTrue(firstEntity.containsKey("total_amount"), "Should contain total_amount field");
        assertEquals(8, returnedEntities.size(), "Should return exactly 8 records");

        // Create a map of account IDs to their total transaction amounts for easy verification
        Map<Integer, Double> accountTotalAmounts = new HashMap<>();
        for (Map<String, Object> entity : returnedEntities) {
            int accountId = ((Number) entity.get("account_id")).intValue();
            double totalAmount = ((Number) entity.get("total_amount")).doubleValue();
            accountTotalAmounts.put(accountId, totalAmount);
        }

        // Verify the expected account IDs and their total transaction amounts
        assertTrue(accountTotalAmounts.containsKey(1), "Should include account with ID 1");
        assertTrue(accountTotalAmounts.containsKey(2), "Should include account with ID 2");
        assertTrue(accountTotalAmounts.containsKey(3), "Should include account with ID 3");
        assertTrue(accountTotalAmounts.containsKey(5), "Should include account with ID 5");
        assertTrue(accountTotalAmounts.containsKey(6), "Should include account with ID 6");
        assertTrue(accountTotalAmounts.containsKey(7), "Should include account with ID 7");
        assertTrue(accountTotalAmounts.containsKey(9), "Should include account with ID 9");
        assertTrue(accountTotalAmounts.containsKey(10), "Should include account with ID 10");

        // Using delta for floating point comparison
        double delta = 0.001;
        assertEquals(500.0, accountTotalAmounts.get(1), delta, "Account 1 should have total amount of 500");
        assertEquals(200.0, accountTotalAmounts.get(2), delta, "Account 2 should have total amount of 200");
        assertEquals(300.0, accountTotalAmounts.get(3), delta, "Account 3 should have total amount of 300");
        assertEquals(1000.0, accountTotalAmounts.get(5), delta, "Account 5 should have total amount of 1000");
        assertEquals(500.0, accountTotalAmounts.get(6), delta, "Account 6 should have total amount of 500");
        assertEquals(200.0, accountTotalAmounts.get(7), delta, "Account 7 should have total amount of 200");
        assertEquals(700.0, accountTotalAmounts.get(9), delta, "Account 9 should have total amount of 700");
        assertEquals(300.0, accountTotalAmounts.get(10), delta, "Account 10 should have total amount of 300");
    }

    @Test
    @Order(26)
    void test_26_TotalBalanceByAccountType() throws IOException, SQLException {
        List<Map<String, Object>> returnedEntities = executeQueryFromFile(OPTIONAL_SUM_26);

        assertFalse(returnedEntities.isEmpty(), "Result should not be empty");

        checkSameAmountOfFieldsInAllRecords(returnedEntities);

        // Check that each record has exactly 2 fields (account_type and total_balance)
        assertEquals(2, returnedEntities.get(0).size(), "Each record should have exactly 2 fields");

        // Verify that the fields are named correctly
        Map<String, Object> firstEntity = returnedEntities.get(0);
        assertTrue(firstEntity.containsKey("account_type"), "Should contain 'account_type' field");
        assertTrue(firstEntity.containsKey("total_balance"), "Should contain 'total_balance' field");
        assertEquals(2, returnedEntities.size(), "Should return exactly 2 records");

        Map<String, Integer> accountTypeBalances = new HashMap<>();
        for (Map<String, Object> entry : returnedEntities) {
            String accountType = entry.get("account_type").toString().trim();
            int totalBalance = ((Number) entry.get("total_balance")).intValue();
            accountTypeBalances.put(accountType, totalBalance);
        }

        // Verify the expected account types and their total balances
        assertTrue(accountTypeBalances.containsKey("savings"), "Should include 'savings' account type");
        assertTrue(accountTypeBalances.containsKey("checking"), "Should include 'checking' account type");
        assertEquals(33500, accountTypeBalances.get("savings"), "Total balance for savings should be 33500");
        assertEquals(8200, accountTypeBalances.get("checking"), "Total balance for checking should be 8200");
    }

    @Test
    @Order(27)
    void test_27_AverageSalaryPerBranch() throws IOException, SQLException {
        List<Map<String, Object>> returnedEntities = executeQueryFromFile(OPTIONAL_AVG_27);

        assertFalse(returnedEntities.isEmpty(), "Result should not be empty");

        // Check that each record has exactly 2 fields (branch_id and avg_salary)
        assertEquals(2, returnedEntities.get(0).size(), "Each record should have exactly 2 fields");

        checkSameAmountOfFieldsInAllRecords(returnedEntities);

        // Verify that the fields are named correctly
        Map<String, Object> firstEntity = returnedEntities.get(0);
        assertTrue(firstEntity.containsKey("branch_id"), "Should contain branch_id field");
        assertTrue(firstEntity.containsKey("avg_salary"), "Should contain avg_salary field");
        assertEquals(4, returnedEntities.size(), "Should return exactly 4 records");

        // Create a map of branch IDs to their average salaries for easy verification
        Map<Integer, Double> branchAvgSalaries = new HashMap<>();
        for (Map<String, Object> entity : returnedEntities) {
            int branchId = ((Number) entity.get("branch_id")).intValue();
            double avgSalary = ((Number) entity.get("avg_salary")).doubleValue();
            branchAvgSalaries.put(branchId, avgSalary);
        }

        // Verify the expected branch IDs and their average salaries
        assertTrue(branchAvgSalaries.containsKey(1), "Should include branch with ID 1");
        assertTrue(branchAvgSalaries.containsKey(2), "Should include branch with ID 2");
        assertTrue(branchAvgSalaries.containsKey(3), "Should include branch with ID 3");
        assertTrue(branchAvgSalaries.containsKey(4), "Should include branch with ID 4");

        // Using delta for floating point comparison
        double delta = 0.001;
        assertEquals(55000.0, branchAvgSalaries.get(1), delta, "Branch 1 should have average salary of 55000");
        assertEquals(42500.0, branchAvgSalaries.get(2), delta, "Branch 2 should have average salary of 42500");
        assertEquals(51500.0, branchAvgSalaries.get(3), delta, "Branch 3 should have average salary of 51500");
        assertEquals(46000.0, branchAvgSalaries.get(4), delta, "Branch 4 should have average salary of 46000");
    }

    @Test
    @Order(28)
    void test_28_AverageBalancePerCustomer() throws IOException, SQLException {
        List<Map<String, Object>> returnedEntities = executeQueryFromFile(OPTIONAL_AVG_28);

        assertFalse(returnedEntities.isEmpty(), "Result should not be empty");

        checkSameAmountOfFieldsInAllRecords(returnedEntities);

        // Check that each record has exactly 2 fields (customer_id and avg_balance)
        assertEquals(2, returnedEntities.get(0).size(), "Each record should have exactly 2 fields");

        // Verify that the fields are named correctly
        Map<String, Object> firstEntity = returnedEntities.get(0);
        assertTrue(firstEntity.containsKey("customer_id"), "Should contain customer_id field");
        assertTrue(firstEntity.containsKey("avg_balance"), "Should contain avg_balance field");
        assertEquals(9, returnedEntities.size(), "Should return exactly 9 records");

        // Create a map of customer IDs to their average balances for easy verification
        Map<Integer, Double> customerAvgBalances = new HashMap<>();
        for (Map<String, Object> entity : returnedEntities) {
            int customerId = ((Number) entity.get("customer_id")).intValue();
            double avgBalance = ((Number) entity.get("avg_balance")).doubleValue();
            customerAvgBalances.put(customerId, avgBalance);
        }

        // Verify the expected customer IDs and their average balances
        assertTrue(customerAvgBalances.containsKey(1), "Should include customer with ID 1");
        assertTrue(customerAvgBalances.containsKey(2), "Should include customer with ID 2");
        assertTrue(customerAvgBalances.containsKey(3), "Should include customer with ID 3");
        assertTrue(customerAvgBalances.containsKey(4), "Should include customer with ID 4");
        assertTrue(customerAvgBalances.containsKey(5), "Should include customer with ID 5");
        assertTrue(customerAvgBalances.containsKey(6), "Should include customer with ID 6");
        assertTrue(customerAvgBalances.containsKey(7), "Should include customer with ID 7");
        assertTrue(customerAvgBalances.containsKey(8), "Should include customer with ID 8");
        assertTrue(customerAvgBalances.containsKey(9), "Should include customer with ID 9");

        // Using delta for floating point comparison
        double delta = 0.001;
        assertEquals(5750.0, customerAvgBalances.get(1), delta, "Customer 1 should have average balance of 5750");
        assertEquals(1200.0, customerAvgBalances.get(2), delta, "Customer 2 should have average balance of 1200");
        assertEquals(3000.0, customerAvgBalances.get(3), delta, "Customer 3 should have average balance of 3000");
        assertEquals(2000.0, customerAvgBalances.get(4), delta, "Customer 4 should have average balance of 2000");
        assertEquals(7000.0, customerAvgBalances.get(5), delta, "Customer 5 should have average balance of 7000");
        assertEquals(500.0, customerAvgBalances.get(6), delta, "Customer 6 should have average balance of 500");
        assertEquals(2500.0, customerAvgBalances.get(7), delta, "Customer 7 should have average balance of 2500");
        assertEquals(3000.0, customerAvgBalances.get(8), delta, "Customer 8 should have average balance of 3000");
        assertEquals(11000.0, customerAvgBalances.get(9), delta, "Customer 9 should have average balance of 11000");
    }

    @Test
    @Order(29)
    void test_29_FindAverageBalanceForAccountsWhereBalanceExceeds() throws IOException, SQLException {
        List<Map<String, Object>> returnedEntities = executeQueryFromFile(OPTIONAL_AVG_29);

        assertFalse(returnedEntities.isEmpty(), "Result should not be empty");

        // Check that the record has exactly 1 field (avg_balance)
        assertEquals(1, returnedEntities.get(0).size(), "Each record should have exactly 1 field");

        // Verify that the field is named correctly
        Map<String, Object> firstEntity = returnedEntities.get(0);
        assertTrue(firstEntity.containsKey("avg_balance"), "Should contain 'avg_balance' field");
        assertEquals(1, returnedEntities.size(), "Should return exactly 1 record");

        // Verify the expected avgBalance
        double avgBalance = ((Number) firstEntity.get("avg_balance")).doubleValue();
        assertEquals(6083.33, avgBalance, 0.01, "Should return average balance as 6083.33");
    }

    @Test
    @Order(30)
    void test_30_FindAverageLoanTerm() throws IOException, SQLException {
        List<Map<String, Object>> returnedEntities = executeQueryFromFile(OPTIONAL_AVG_30);

        assertFalse(returnedEntities.isEmpty(), "Result should not be empty");

        // Check that the record has exactly 1 field (avg_loan_term)
        assertEquals(1, returnedEntities.get(0).size(), "Each record should have exactly 1 field");

        // Verify that the field is named correctly
        Map<String, Object> firstEntity = returnedEntities.get(0);
        assertTrue(firstEntity.containsKey("avg_loan_term"), "Should contain 'avg_loan_term' field");
        assertEquals(1, returnedEntities.size(), "Should return exactly 1 record");

        // Verify the expected avgBalance
        double loanTerm = ((Number) firstEntity.get("avg_loan_term")).doubleValue();
        assertEquals(48.0, loanTerm, 0.1, "Should return average loan term as 48");
    }

    @Test
    @Order(31)
    void test_31_FindMinMaxRatesForLoans() throws IOException, SQLException {
        List<Map<String, Object>> returnedEntities = executeQueryFromFile(OPTIONAL_MIN_MAX_31);

        assertFalse(returnedEntities.isEmpty(), "Result should not be empty");

        // Check that record has exactly 2 fields (lowest_rate and total_loan)
        assertEquals(2, returnedEntities.get(0).size(), "Record should have exactly 2 fields");

        // Verify that the fields are named correctly
        Map<String, Object> entity = returnedEntities.get(0);
        assertTrue(entity.containsKey("lowest_rate"), "Should contain 'lowest_rate' field");
        assertTrue(entity.containsKey("highest_rate"), "Should contain 'highest_rate' field");
        assertEquals(1, returnedEntities.size(), "Should return exactly 1 record");

        // Verify the expected lowest and highest rates
        assertEquals(3.5, ((Number) entity.get("lowest_rate")).doubleValue(), "Lowest rate should be 3.5");
        assertEquals(6.0, ((Number) entity.get("highest_rate")).doubleValue(), "Highest rate should be 6");
    }

    @Test
    @Order(32)
    void test_32_FindLowestHighestLoanAmountsForActiveLoans() throws IOException, SQLException {
        List<Map<String, Object>> returnedEntities = executeQueryFromFile(OPTIONAL_MIN_MAX_32);

        assertFalse(returnedEntities.isEmpty(), "Result should not be empty");

        // Check that record has exactly 2 fields (smallest_loan and largest_loan)
        assertEquals(2, returnedEntities.get(0).size(), "Record should have exactly 2 fields");

        // Verify that the fields are named correctly
        Map<String, Object> entity = returnedEntities.get(0);
        assertTrue(entity.containsKey("smallest_loan"), "Should contain 'smallest_loan' field");
        assertTrue(entity.containsKey("largest_loan"), "Should contain 'largest_loan' field");
        assertEquals(1, returnedEntities.size(), "Should return exactly 1 record");

        // Verify the expected smallest and largest loans
        assertEquals(9000.0, ((Number) entity.get("smallest_loan")).doubleValue(), "Smallest loan should be 9000.0");
        assertEquals(20000.0, ((Number) entity.get("largest_loan")).doubleValue(), "Largest loan should be 20000.0");
    }

    @Test
    @Order(33)
    void test_33_FindSmallestAndLargestDepositTransactions() throws IOException, SQLException {
        List<Map<String, Object>> returnedEntities = executeQueryFromFile(OPTIONAL_MIN_MAX_33);

        assertFalse(returnedEntities.isEmpty(), "Result should not be empty");

        // Check that record has exactly 2 fields (smallest_deposit and largest_deposit)
        assertEquals(2, returnedEntities.get(0).size(), "Record should have exactly 2 fields");

        // Verify that the fields are named correctly
        Map<String, Object> entity = returnedEntities.get(0);
        assertTrue(entity.containsKey("smallest_deposit"), "Should contain 'smallest_deposit' field");
        assertTrue(entity.containsKey("largest_deposit"), "Should contain 'largest_deposit' field");
        assertEquals(1, returnedEntities.size(), "Should return exactly 1 record");

        // Verify the expected smallest and largest deposits
        assertEquals(500.0, ((Number) entity.get("smallest_deposit")).doubleValue(), "Smallest deposit should be 500.0");
        assertEquals(1000.0, ((Number) entity.get("largest_deposit")).doubleValue(), "Largest deposit should be 1000.0");
    }

    @Test
    @Order(34)
    void test_34_FindMinimumMaximumSavingBalances() throws IOException, SQLException {
        List<Map<String, Object>> returnedEntities = executeQueryFromFile(OPTIONAL_MIN_MAX_34);

        assertFalse(returnedEntities.isEmpty(), "Result should not be empty");

        // Check that record has exactly 2 fields (min_savings and max_savings)
        assertEquals(2, returnedEntities.get(0).size(), "Record should have exactly 2 fields");

        // Verify that the fields are named correctly
        Map<String, Object> entity = returnedEntities.get(0);
        assertTrue(entity.containsKey("min_savings"), "Should contain 'min_savings' field");
        assertTrue(entity.containsKey("max_savings"), "Should contain 'max_savings' field");
        assertEquals(1, returnedEntities.size(), "Should return exactly 1 record");

        // Verify the expected min and max saving balance
        assertEquals(7000.0, ((Number) entity.get("min_savings")).doubleValue(), "Min savings balance should be 7000.0");
        assertEquals(11000.0, ((Number) entity.get("max_savings")).doubleValue(), "Max savings balance should be 11000.0");
    }

    @Test
    @Order(35)
    void test_35_AccountsWithdrawalsExceedDeposits() throws IOException, SQLException {
        List<Map<String, Object>> returnedEntities = executeQueryFromFile(OPTIONAL_GROUPING_HAVING_35);

        assertFalse(returnedEntities.isEmpty(), "Result should not be empty");

        checkSameAmountOfFieldsInAllRecords(returnedEntities);

        // Check that each record has exactly 3 fields
        assertEquals(3, returnedEntities.get(0).size(), "Each record should have exactly 3 fields");

        // Verify that the fields are named correctly
        Map<String, Object> firstEntity = returnedEntities.get(0);
        assertTrue(firstEntity.containsKey("account_id"), "Should contain 'account_id' field");
        assertTrue(firstEntity.containsKey("total_deposits"), "Should contain 'total_deposits' field");
        assertTrue(firstEntity.containsKey("total_withdrawals"), "Should contain 'total_withdrawals' field");
        assertEquals(3, returnedEntities.size(), "Should return exactly 3 records");

        // Create a map of account IDs to their transactions data for verification
        Map<Integer, Map<String, Object>> accountTransactions = new HashMap<>();
        for (Map<String, Object> entity : returnedEntities) {
            int accountId = ((Number) entity.get("account_id")).intValue();
            accountTransactions.put(accountId, entity);
        }

        // Verify the expected account IDs are included
        assertTrue(accountTransactions.containsKey(2), "Should include account ID 2");
        assertTrue(accountTransactions.containsKey(6), "Should include account ID 6");
        assertTrue(accountTransactions.containsKey(10), "Should include account ID 10");

        // Verify the expected transaction amounts
        assertEquals(0.0, ((Number) accountTransactions.get(2).get("total_deposits")).doubleValue(),
                0.01, "Account 2 should have $0.00 in deposits");
        assertEquals(200.0, ((Number) accountTransactions.get(2).get("total_withdrawals")).doubleValue(),
                0.01, "Account 2 should have $200.00 in withdrawals");

        assertEquals(0.0, ((Number) accountTransactions.get(6).get("total_deposits")).doubleValue(),
                0.01, "Account 6 should have $0.00 in deposits");
        assertEquals(500.0, ((Number) accountTransactions.get(6).get("total_withdrawals")).doubleValue(),
                0.01, "Account 6 should have $500.00 in withdrawals");

        assertEquals(0.0, ((Number) accountTransactions.get(10).get("total_deposits")).doubleValue(),
                0.01, "Account 10 should have $0.00 in deposits");
        assertEquals(300.0, ((Number) accountTransactions.get(10).get("total_withdrawals")).doubleValue(),
                0.01, "Account 10 should have $300.00 in withdrawals");

        // Verify that all accounts have withdrawals exceeding deposits
        for (Map<String, Object> entity : returnedEntities) {
            double deposits = ((Number) entity.get("total_deposits")).doubleValue();
            double withdrawals = ((Number) entity.get("total_withdrawals")).doubleValue();
            assertTrue(withdrawals > deposits, "All returned accounts should have withdrawals exceeding deposits");
        }
    }

    @Test
    @Order(36)
    void test_36_CustomersWithMultipleAccounts() throws IOException, SQLException {
        List<Map<String, Object>> returnedEntities = executeQueryFromFile(OPTIONAL_GROUPING_HAVING_36);

        assertFalse(returnedEntities.isEmpty(), "Result should not be empty");

        // Check that record has exactly 2 fields (customer_id and account_count)
        assertEquals(2, returnedEntities.get(0).size(), "Each record should have exactly 2 fields");

        // Verify that the fields are named correctly
        Map<String, Object> firstEntity = returnedEntities.get(0);
        assertTrue(firstEntity.containsKey("customer_id"), "Should contain 'customer_id' field");
        assertTrue(firstEntity.containsKey("account_count"), "Should contain 'account_count' field");
        assertEquals(1, returnedEntities.size(), "Should return exactly 1 record");

        // Create a map of customer IDs to their account counts for easy verification
        Map<Integer, Integer> customerAccountCounts = new HashMap<>();
        for (Map<String, Object> entity : returnedEntities) {
            int customerId = ((Number) entity.get("customer_id")).intValue();
            int accountCount = ((Number) entity.get("account_count")).intValue();
            customerAccountCounts.put(customerId, accountCount);
        }

        // Verify the expected customer IDs and their account counts
        assertTrue(customerAccountCounts.containsKey(1), "Should include customer with ID 1");
        assertEquals(2, customerAccountCounts.get(1), "Customer 1 should have 2 accounts");
    }

    @Test
    @Order(37)
    void test_37_LoanStatusExceedsThree() throws IOException, SQLException {
        List<Map<String, Object>> returnedEntities = executeQueryFromFile(OPTIONAL_GROUPING_HAVING_37);

        assertFalse(returnedEntities.isEmpty(), "Result should not be empty");

        checkSameAmountOfFieldsInAllRecords(returnedEntities);

        // Check that the record has exactly 2 fields (status and loan_count)
        assertEquals(2, returnedEntities.get(0).size(), "Each record should have exactly 2 fields");

        // Verify that the fields are named correctly
        Map<String, Object> firstEntity = returnedEntities.get(0);
        assertTrue(firstEntity.containsKey("status"), "Should contain 'status' field");
        assertTrue(firstEntity.containsKey("loan_count"), "Should contain 'loan_count' field");
        assertEquals(1, returnedEntities.size(), "Should return exactly 1 record");

        // Verify the expected loan status and count
        String status = firstEntity.get("status").toString().trim();
        int count = ((Number) firstEntity.get("loan_count")).intValue();

        assertEquals("active", status, "Should be 'active' loan status");
        assertEquals(6, count, "Should have 6 active loans");
    }

    @Test
    @Order(38)
    void test_38_BranchesWithTotalSalariesExceeds() throws IOException, SQLException {
        List<Map<String, Object>> returnedEntities = executeQueryFromFile(OPTIONAL_GROUPING_HAVING_38);

        assertFalse(returnedEntities.isEmpty(), "Result should not be empty");

        checkSameAmountOfFieldsInAllRecords(returnedEntities);

        // Check that the record has exactly 2 fields (branch_id and total_salary)
        Map<String, Object> result = returnedEntities.get(0);
        assertEquals(2, result.size(), "Result should have exactly 2 fields");

        // Verify that the fields are named correctly
        Map<String, Object> firstEntity = returnedEntities.get(0);
        assertTrue(firstEntity.containsKey("branch_id"), "Should contain 'branch_id' field");
        assertTrue(firstEntity.containsKey("total_salary"), "Should contain 'total_salary' field");
        assertEquals(2, returnedEntities.size(), "Should return exactly 2 records");

        // Create a map of branch IDs to their total employees salaries for verification
        Map<Integer, Double> branchSalaries = new HashMap<>();
        for (Map<String, Object> entity : returnedEntities) {
            int branchId = ((Number) entity.get("branch_id")).intValue();
            double totalSalary = ((Number) entity.get("total_salary")).doubleValue();
            branchSalaries.put(branchId, totalSalary);
        }

        // Verify the expected branch IDs and their total employees salary amounts
        assertTrue(branchSalaries.containsKey(1), "Should include branch ID 1");
        assertTrue(branchSalaries.containsKey(3), "Should include branch ID 3");
        assertEquals(110000.0, branchSalaries.get(1), "Branch 1 should have $110,000 total salary");
        assertEquals(103000.0, branchSalaries.get(3), "Branch 3 should have $103,000 total salary");
    }

    @Test
    @Order(39)
    void test_39_FindAccountsWithDepositsExceeds() throws IOException, SQLException {
        List<Map<String, Object>> returnedEntities = executeQueryFromFile(OPTIONAL_GROUPING_HAVING_39);

        assertFalse(returnedEntities.isEmpty(), "Result should not be empty");

        checkSameAmountOfFieldsInAllRecords(returnedEntities);

        // Check that the record has exactly 2 fields (account_id and total_deposits)
        Map<String, Object> result = returnedEntities.get(0);
        assertEquals(2, result.size(), "Result should have exactly 2 fields");

        // Verify that the fields are named correctly
        assertTrue(result.containsKey("account_id"), "Should contain 'account_id' field");
        assertTrue(result.containsKey("total_deposits"), "Should contain 'total_deposits' field");
        assertEquals(2, returnedEntities.size(), "Should return exactly 2 records");

        // Create a map of account IDs to their total deposits for verification
        Map<Integer, Double> accountDeposits = new HashMap<>();
        for (Map<String, Object> entity : returnedEntities) {
            int accountId = ((Number) entity.get("account_id")).intValue();
            double totalDeposit = ((Number) entity.get("total_deposits")).doubleValue();
            accountDeposits.put(accountId, totalDeposit);
        }

        // Verify the expected account IDs and their total deposits amounts
        assertTrue(accountDeposits.containsKey(5), "Should include account ID 5");
        assertTrue(accountDeposits.containsKey(9), "Should include account ID 9");
        assertEquals(1000.0, accountDeposits.get(5), "Account 1 should have $1000 total deposit");
        assertEquals(700.0, accountDeposits.get(9), "Account 3 should have $700 total deposit");
    }

    @Test
    @Order(40)
    void test_40_AverageBalanceMultipleAccounts() throws IOException, SQLException {
        List<Map<String, Object>> returnedEntities = executeQueryFromFile(OPTIONAL_GROUPING_HAVING_40);

        assertFalse(returnedEntities.isEmpty(), "Result should not be empty");

        // Check that the record has exactly 2 fields (customer_id and avg_balance)
        Map<String, Object> result = returnedEntities.get(0);
        assertEquals(2, result.size(), "Result should have exactly 2 fields");

        // Verify that the fields are named correctly
        assertTrue(result.containsKey("customer_id"), "Should contain 'customer_id' field");
        assertTrue(result.containsKey("avg_balance"), "Should contain 'avg_balance' field");
        assertEquals(1, returnedEntities.size(), "Should return exactly 1 record");

        int customerId = ((Number) result.get("customer_id")).intValue();
        double avgBalance = ((Number) result.get("avg_balance")).doubleValue();

        assertEquals(1, customerId, "Should be customer ID 1");
        assertEquals(5750.0, avgBalance, 0.01, "Average balance should be $5,750.00");
    }

    @Test
    @Order(41)
    void test_41_FindLoanStatusesWithAvgInterestRateExceeds() throws IOException, SQLException {
        List<Map<String, Object>> returnedEntities = executeQueryFromFile(OPTIONAL_GROUPING_HAVING_41);

        assertFalse(returnedEntities.isEmpty(), "Result should not be empty");

        // Check that the record has exactly 2 fields (status and avg_interest_rate)
        Map<String, Object> result = returnedEntities.get(0);
        assertEquals(2, result.size(), "Result should have exactly 2 fields");

        // Verify that the fields are named correctly
        assertTrue(result.containsKey("status"), "Should contain 'status' field");
        assertTrue(result.containsKey("avg_interest_rate"), "Should contain 'avg_interest_rate' field");
        assertEquals(1, returnedEntities.size(), "Should return exactly 1 record");

        String status = result.get("status").toString();
        double avgInterestRate = ((Number) result.get("avg_interest_rate")).doubleValue();

        assertEquals("closed", status, "Should return status - closed");
        assertEquals(5.37, avgInterestRate, 0.01, "Average interest rate should be ~ 5.37");
    }

    @Test
    @Order(42)
    void test_42_FindTxTypesWhereAverageTxAmountExceeds() throws IOException, SQLException {
        List<Map<String, Object>> returnedEntities = executeQueryFromFile(OPTIONAL_GROUPING_HAVING_42);

        assertFalse(returnedEntities.isEmpty(), "Result should not be empty");

        checkSameAmountOfFieldsInAllRecords(returnedEntities);

        // Check that the record has exactly 2 fields (transaction_type and avg_tx_amount)
        Map<String, Object> result = returnedEntities.get(0);
        assertEquals(2, result.size(), "Result should have exactly 2 fields");

        // Verify that the fields are named correctly
        assertTrue(result.containsKey("transaction_type"), "Should contain 'transaction_type' field");
        assertTrue(result.containsKey("avg_tx_amount"), "Should contain 'avg_tx_amount' field");
        assertEquals(2, returnedEntities.size(), "Should return exactly 2 records");

        // Create a map of transaction types to their avg_tx_amount for verification
        Map<String, Double> txTypeToAmount = new HashMap<>();
        for (Map<String, Object> entity : returnedEntities) {
            String transactionType = entity.get("transaction_type").toString();
            double avgTxAmount = ((Number) entity.get("avg_tx_amount")).doubleValue();
            txTypeToAmount.put(transactionType, avgTxAmount);
        }

        assertTrue(txTypeToAmount.containsKey("deposit"), "Should include tx type as - deposit");
        assertTrue(txTypeToAmount.containsKey("withdrawal"), "Should include tx type as - withdrawal");
        assertEquals(733.33, txTypeToAmount.get("deposit"), 0.01, "Deposit should have value ~ 733.33");
        assertEquals(333.33, txTypeToAmount.get("withdrawal"), 0.01, "Withdrawal should have value ~ 333.33");
    }

    @Test
    @Order(43)
    void test_43_FindBranchesWhereAtLeastOneEmployeeSalaryExceeds() throws IOException, SQLException {
        List<Map<String, Object>> returnedEntities = executeQueryFromFile(OPTIONAL_GROUPING_HAVING_43);

        assertFalse(returnedEntities.isEmpty(), "Result should not be empty");

        // Check that the records has exactly 1 field
        Map<String, Object> result = returnedEntities.get(0);
        assertEquals(1, result.size(), "Result should have exactly 1 field");

        // Verify that the field is named correctly
        assertTrue(result.containsKey("branch_id"), "Should contain 'branch_id' field");
        assertEquals(3, returnedEntities.size(), "Should return exactly 3 records");

        List<Integer> list = new ArrayList<>();
        for (Map<String, Object> entity : returnedEntities) {
            int branchId = ((Number) entity.get("branch_id")).intValue();
            list.add(branchId);
        }

        assertTrue(list.contains(1), "Should include branch with ID - 1");
        assertTrue(list.contains(3), "Should include branch with ID - 3");
        assertTrue(list.contains(4), "Should include branch with ID - 4");
    }

    @Test
    @Order(44)
    void test_44_CustomersWithLoansButNoAccounts() throws IOException, SQLException {
        List<Map<String, Object>> returnedEntities = executeQueryFromFile(OPTIONAL_GROUPING_HAVING_44);

        assertFalse(returnedEntities.isEmpty(), "Result should not be empty");

        // Check that the record has exactly 1 field (customer_id)
        Map<String, Object> result = returnedEntities.get(0);
        assertEquals(1, result.size(), "Result should have exactly 1 field");
        assertEquals(1, returnedEntities.size(), "Should return exactly 1 record");

        // Verify that the field is named correctly
        assertTrue(result.containsKey("customer_id"), "Should contain 'customer_id' field");

        // Verify the expected customer ID
        int customerId = ((Number) result.get("customer_id")).intValue();
        assertEquals(10, customerId, "Should be customer ID 10");
    }

    @Test
    @Order(45)
    void test_45_BranchesWithMinEmployeePercentage() throws IOException, SQLException {
        List<Map<String, Object>> returnedEntities = executeQueryFromFile(OPTIONAL_GROUPING_HAVING_45);

        assertFalse(returnedEntities.isEmpty(), "Result should not be empty");

        checkSameAmountOfFieldsInAllRecords(returnedEntities);

        // Check that each record has exactly 2 fields (branch_id and employee_count)
        assertEquals(2, returnedEntities.get(0).size(), "Each record should have exactly 2 fields");

        // Verify that the fields are named correctly
        Map<String, Object> firstEntity = returnedEntities.get(0);
        assertTrue(firstEntity.containsKey("branch_id"), "Should contain 'branch_id' field");
        assertTrue(firstEntity.containsKey("employee_count"), "Should contain 'employee_count' field");
        assertEquals(4, returnedEntities.size(), "Should return exactly 4 records");

        // Create a map of branch IDs to their employee counts for verification
        Map<Integer, Integer> branchEmployeeCounts = new HashMap<>();
        for (Map<String, Object> entity : returnedEntities) {
            int branchId = ((Number) entity.get("branch_id")).intValue();
            int count = ((Number) entity.get("employee_count")).intValue();
            branchEmployeeCounts.put(branchId, count);
        }

        // Verify the expected branch IDs and their employee counts
        assertTrue(branchEmployeeCounts.containsKey(1), "Should include branch ID 1");
        assertTrue(branchEmployeeCounts.containsKey(2), "Should include branch ID 2");
        assertTrue(branchEmployeeCounts.containsKey(3), "Should include branch ID 3");
        assertTrue(branchEmployeeCounts.containsKey(4), "Should include branch ID 4");

        assertEquals(2, branchEmployeeCounts.get(1), "Branch 1 should have 2 employees");
        assertEquals(2, branchEmployeeCounts.get(2), "Branch 2 should have 2 employees");
        assertEquals(2, branchEmployeeCounts.get(3), "Branch 3 should have 2 employees");
        assertEquals(2, branchEmployeeCounts.get(4), "Branch 4 should have 2 employees");

        // Verify that all branches have at least 10% of total employees
        int totalEmployees = branchEmployeeCounts.values().stream().mapToInt(Integer::intValue).sum();
        double minThreshold = totalEmployees * 0.1;

        for (int employeeCount : branchEmployeeCounts.values()) {
            assertTrue(employeeCount >= minThreshold, "All returned branches should have at least 10% of total employees");
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            MANDATORY_COUNT_01, MANDATORY_COUNT_02, MANDATORY_COUNT_03, MANDATORY_SUM_04, MANDATORY_SUM_05,
            MANDATORY_SUM_06, MANDATORY_AVG_07, MANDATORY_AVG_08, MANDATORY_AVG_09, MANDATORY_MIN_MAX_10,
            MANDATORY_MIN_MAX_11, MANDATORY_MIN_MAX_12, MANDATORY_GROUPING_HAVING_13, MANDATORY_GROUPING_HAVING_14,
            MANDATORY_GROUPING_HAVING_15, OPTIONAL_COUNT_16, OPTIONAL_COUNT_17, OPTIONAL_COUNT_18, OPTIONAL_COUNT_19,
            OPTIONAL_COUNT_20, OPTIONAL_COUNT_21, OPTIONAL_SUM_22, OPTIONAL_SUM_23, OPTIONAL_SUM_24, OPTIONAL_SUM_25,
            OPTIONAL_SUM_26, OPTIONAL_AVG_27, OPTIONAL_AVG_28, OPTIONAL_AVG_29, OPTIONAL_AVG_30, OPTIONAL_MIN_MAX_31,
            OPTIONAL_MIN_MAX_32, OPTIONAL_MIN_MAX_33, OPTIONAL_MIN_MAX_34, OPTIONAL_GROUPING_HAVING_35,
            OPTIONAL_GROUPING_HAVING_36, OPTIONAL_GROUPING_HAVING_37, OPTIONAL_GROUPING_HAVING_38,
            OPTIONAL_GROUPING_HAVING_39, OPTIONAL_GROUPING_HAVING_40, OPTIONAL_GROUPING_HAVING_41,
            OPTIONAL_GROUPING_HAVING_42, OPTIONAL_GROUPING_HAVING_43, OPTIONAL_GROUPING_HAVING_44,
            OPTIONAL_GROUPING_HAVING_45
    })
    void testQuerySyntax(String queryFile) throws IOException {
        // This test just ensures the query can be executed without syntax errors
        assertDoesNotThrow(() -> executeQueryFromFile(queryFile));
    }

    private List<Map<String, Object>> executeQueryFromFile(String queryFileName) throws IOException, SQLException {
        String filePath = getResourcePath(QUERIES_DIR + queryFileName);
        String sql = Files.readString(Paths.get(filePath)).trim();

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

            System.out.println();
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


    private static void checkSameAmountOfFieldsInAllRecords(List<Map<String, Object>> returnedEntities) {
        // Check that all records have the same number of fields
        boolean allEntitiesHaveSameFieldCount = returnedEntities.stream().map(Map::size).distinct().count() == 1;
        assertTrue(allEntitiesHaveSameFieldCount, "All records should have the same number of fields");
    }
}