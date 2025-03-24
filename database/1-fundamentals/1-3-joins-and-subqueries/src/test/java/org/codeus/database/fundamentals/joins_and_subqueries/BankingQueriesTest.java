package org.codeus.database.fundamentals.joins_and_subqueries;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BankingQueriesTest {
    private static EmbeddedPostgres postgres;
    private static Connection connection;

    // File paths
    private static final String INIT_DB_FILE = "init_db.sql";
    private static final String TEST_DATA_FILE = "test_data.sql";

    private static final String MANDATORY = "mandatory/";

    // Inner Join paths
    private static final String INNER_JOIN = MANDATORY + "1_inner_join/";
    private static final String M1_BASIC_CUSTOMER_ACCOUNTS = INNER_JOIN + "m1_basic_customer_accounts.sql";
    private static final String M2_EMPLOYEE_BRANCH_LOCATION = INNER_JOIN + "m2_employee_branch_location.sql";

    // Left Join paths
    private static final String LEFT_JOIN = MANDATORY + "2_left_join/";
    private static final String M3_CUSTOMERS_WITH_OPTIONAL_LOANS = LEFT_JOIN + "m3_customers_with_optional_loans.sql";
    private static final String M4_BRANCHES_EMPLOYEE_COUNT = LEFT_JOIN + "m4_branches_employee_count.sql";

    // Right Join paths
    private static final String RIGHT_JOIN = MANDATORY + "3_right_join/";
    private static final String M5_ACCOUNTS_WITH_TRANSACTIONS = RIGHT_JOIN + "m5_accounts_with_transactions.sql";
    private static final String M6_LOANS_WITH_BRANCH_INFO = RIGHT_JOIN + "m6_loans_with_branch_info.sql";
    // CTE paths
    private static final String CTE = MANDATORY + "4_cte/";
    private static final String M7_CUSTOMER_TRANSACTION_TOTALS = CTE + "m7_customer_transaction_totals.sql";
    private static final String M8_ACTIVE_BRANCHES = CTE + "m8_active_branches.sql";

    private static final String OPTIONAL = "optional/";

    // Query files
    private static final String O9_BRANCH_SUMMARY = OPTIONAL + "o9_branch_summary.sql";
    private static final String O10_ACTIVE_EMPLOYEES = OPTIONAL + "o10_active_employees.sql";
    private static final String O11_CUSTOMER_PRODUCTS = OPTIONAL + "o11_customer_products.sql";
    private static final String O12_RICH_CUSTOMERS = OPTIONAL + "o12_rich_customers.sql";
    private static final String O13_BUSY_BRANCHES = OPTIONAL + "o13_busy_branches.sql";
    private static final String O14_CUSTOMER_TRANSACTION_SUMMARY = OPTIONAL + "o14_customer_transaction_summary.sql";
    private static final String O15_BRANCH_LOAN_SUMMARY = OPTIONAL + "o15_branch_loan_summary.sql";
    private static final String O16_ACTIVE_ACCOUNTS_SUMMARY = OPTIONAL + "o16_active_accounts_summary.sql";
    private static final String O17_EMPLOYEE_PERFORMANCE_SUMMARY = OPTIONAL + "o17_employee_performance_summary.sql";
    private static final String O18_CUSTOMER_PRODUCT_ANALYSIS = OPTIONAL + "o18_customer_product_analysis.sql";
    private static final String O19_BRANCH_PERFORMANCE_METRICS = OPTIONAL + "o19_branch_performance_metrics.sql";


    @BeforeAll
    static void startDatabase() throws IOException, SQLException {
        postgres = EmbeddedPostgres.start();
        connection = postgres.getPostgresDatabase().getConnection();
        executeSqlFile(INIT_DB_FILE);
        executeSqlFile(TEST_DATA_FILE);
    }

    @Test
    @Order(1)
    void testBasicCustomerAccounts() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile(M1_BASIC_CUSTOMER_ACCOUNTS);

        assertFalse(results.isEmpty(), "Should return customer-account records");

        // Verify structure
        Map<String, Object> firstRow = results.get(0);
        assertNotNull(firstRow.get("first_name"));
        assertNotNull(firstRow.get("last_name"));
        assertNotNull(firstRow.get("account_type"));
        assertNotNull(firstRow.get("balance"));

        // Verify all records have account information (INNER JOIN)
        results.forEach(row -> {
            assertNotNull(row.get("account_type"), "Account type should not be null");
            assertNotNull(row.get("balance"), "Balance should not be null");
        });
    }

    @Test
    @Order(2)
    void testEmployeeBranchLocation() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile(M2_EMPLOYEE_BRANCH_LOCATION);

        assertFalse(results.isEmpty(), "Should return employee-branch records");

        results.forEach(row -> {
            assertNotNull(row.get("first_name"));
            assertNotNull(row.get("last_name"));
            assertNotNull(row.get("branch_name"));
            assertNotNull(row.get("location"));
        });
    }

    @Test
    @Order(3)
    void testCustomersWithOptionalLoans() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile(M3_CUSTOMERS_WITH_OPTIONAL_LOANS);

        // Should return all customers (LEFT JOIN)
        assertEquals(5, results.size(), "Should return all customers");

    }

    @Test
    @Order(4)
    void testBranchesEmployeeCount() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile(M4_BRANCHES_EMPLOYEE_COUNT);

        // Should return all branches
        assertEquals(3, results.size(), "Should return all branches");

        results.forEach(row -> {
            assertNotNull(row.get("branch_name"));
            assertNotNull(row.get("employee_count"));
            assertTrue(((Number) row.get("employee_count")).intValue() >= 0);
        });
    }

    @Test
    @Order(5)
    void testAccountsWithTransactions() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile(M5_ACCOUNTS_WITH_TRANSACTIONS);

        assertFalse(results.isEmpty(), "Should return transaction records");

        results.forEach(row -> {
            assertNotNull(row.get("transaction_type"));
            assertNotNull(row.get("amount"));
        });
    }

    @Test
    @Order(6)
    void testLoansWithBranchInfo() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile(M6_LOANS_WITH_BRANCH_INFO);

        assertFalse(results.isEmpty(), "Should return loan records");

        results.forEach(row -> {
            assertNotNull(row.get("loan_type"));
            assertNotNull(row.get("amount"));
            // Branch name might be null for some loans
        });
    }

    @Test
    @Order(7)
    void testCustomerTransactionTotals() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile(M7_CUSTOMER_TRANSACTION_TOTALS);

        assertFalse(results.isEmpty(), "Should return customer totals");

        results.forEach(row -> {
            assertNotNull(row.get("first_name"));
            assertNotNull(row.get("last_name"));
            assertNotNull(row.get("total_amount"));
            assertTrue(((Number) row.get("total_amount")).doubleValue() >= 0);
        });

        // Verify descending order
        double previousAmount = Double.MAX_VALUE;
        for (Map<String, Object> row : results) {
            double currentAmount = ((Number) row.get("total_amount")).doubleValue();
            assertTrue(currentAmount <= previousAmount, "Results should be in descending order");
            previousAmount = currentAmount;
        }
    }

    @Test
    @Order(8)
    void testActiveBranches() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile(M8_ACTIVE_BRANCHES);

        // Should return all branches
        assertEquals(3, results.size(), "Should return all branches");

        results.forEach(row -> {
            assertNotNull(row.get("branch_name"));
            assertNotNull(row.get("status"));

            String status = (String) row.get("status");
            assertTrue(
                    status.equals("Active") || status.equals("Inactive"),
                    "Status should be either 'Active' or 'Inactive'"
            );
        });

        // Verify at least one active branch exists
        boolean hasActiveBranch = results.stream()
                .anyMatch(row -> "Active".equals(row.get("status")));
        assertTrue(hasActiveBranch, "Should have at least one active branch");
    }


    @Test
    @Order(9)
    void testBranchSummary() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile(O9_BRANCH_SUMMARY);

        assertFalse(results.isEmpty());
        assertEquals(3, results.size(), "Should return data for all 3 branches");

        Map<String, Object> firstBranch = results.get(0);
        assertTrue(firstBranch.containsKey("branch_name"));
        assertTrue(firstBranch.containsKey("employee_count"));
        assertTrue(firstBranch.containsKey("account_count"));
        assertTrue(firstBranch.containsKey("avg_account_balance"));

        // Verify specific branch data
        results.forEach(branch -> {
            int employeeCount = ((Number) branch.get("employee_count")).intValue();
            int accountCount = ((Number) branch.get("account_count")).intValue();
            assertTrue(employeeCount > 0, "Each branch should have employees");
            assertTrue(accountCount > 0, "Each branch should have accounts");
        });
    }

    @Test
    @Order(10)
    void testActiveEmployees() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile(O10_ACTIVE_EMPLOYEES);

        assertFalse(results.isEmpty());

        // Verify ranking logic
        int previousRank = 0;
        int previousLoans = Integer.MAX_VALUE;

        for (Map<String, Object> employee : results) {
            int currentRank = ((Number) employee.get("performance_rank")).intValue();
            int currentLoans = ((Number) employee.get("loans_processed")).intValue();

            assertTrue(currentRank >= previousRank, "Ranks should be in ascending order");
            assertTrue(currentLoans <= previousLoans, "Loan counts should be in descending order");

            previousRank = currentRank;
            previousLoans = currentLoans;
        }
    }

    @Test
    @Order(11)
    void testCustomerProducts() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile(O11_CUSTOMER_PRODUCTS);

        assertEquals(5, results.size(), "Should return data for all 5 customers");

        results.forEach(customer -> {
            int accountCount = ((Number) customer.get("account_count")).intValue();
            int loanCount = ((Number) customer.get("loan_count")).intValue();
            assertTrue(accountCount > 0, "Each customer should have at least one account");
            assertTrue(loanCount >= 0, "Loan count should be zero or more");
        });
    }

    @Test
    @Order(12)
    void testRichCustomers() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile(O12_RICH_CUSTOMERS);

        assertFalse(results.isEmpty());

        // Get average balance for comparison
        double avgBalance = getAverageBalance();

        results.forEach(customer -> {
            BigDecimal balance = (BigDecimal) customer.get("total_balance");
            assertTrue(balance.doubleValue() > avgBalance,
                    "Customer balance should be above average");
        });
    }

    @Test
    @Order(13)
    void testBusyBranches() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile(O13_BUSY_BRANCHES);

        assertFalse(results.isEmpty());

        // Get average transaction count
        double avgTransactions = getAverageTransactionCount();

        results.forEach(branch -> {
            int transactionCount = ((Number) branch.get("transaction_count")).intValue();
            assertTrue(transactionCount > avgTransactions,
                    "Branch transaction count should be above average");
        });
    }

    @Test
    @Order(14)
    void testCustomerTransactionSummary() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile(O14_CUSTOMER_TRANSACTION_SUMMARY);

        assertFalse(results.isEmpty());

        results.forEach(summary -> {
            BigDecimal deposits = (BigDecimal) summary.get("total_deposits");
            BigDecimal withdrawals = (BigDecimal) summary.get("total_withdrawals");
            int transactionCount = ((Number) summary.get("transaction_count")).intValue();

            assertTrue(deposits.compareTo(BigDecimal.ZERO) >= 0,
                    "Deposits should be non-negative");
            assertTrue(withdrawals.compareTo(BigDecimal.ZERO) >= 0,
                    "Withdrawals should be non-negative");
            assertTrue(transactionCount > 0,
                    "Transaction count should be positive");
        });
    }

    @Test
    @Order(15)
    void testBranchLoanSummary() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile(O15_BRANCH_LOAN_SUMMARY);

        // Should have data for all branches
        assertEquals(3, results.size(), "Should return data for all 3 branches");

        // Verify structure
        Map<String, Object> firstBranch = results.get(0);
        assertTrue(firstBranch.containsKey("branch_name"));
        assertTrue(firstBranch.containsKey("total_loans"));
        assertTrue(firstBranch.containsKey("approved_loans"));
        assertTrue(firstBranch.containsKey("average_loan_amount"));

        // Verify data integrity
        results.forEach(branch -> {
            int totalLoans = ((Number) branch.get("total_loans")).intValue();
            int approvedLoans = ((Number) branch.get("approved_loans")).intValue();
            BigDecimal avgAmount = (BigDecimal) branch.get("average_loan_amount");

            // Approved loans should not exceed total loans
            assertTrue(approvedLoans <= totalLoans,
                    "Approved loans cannot exceed total loans");

            // Average loan amount should be positive if there are loans
            if (totalLoans > 0) {
                assertTrue(avgAmount.compareTo(BigDecimal.ZERO) > 0,
                        "Average loan amount should be positive");
            }

            // Verify known test data
            String branchName = (String) branch.get("branch_name");
            switch (branchName) {
                case "Downtown":
                    assertEquals(1, totalLoans, "Downtown branch should have 1 loan");
                    break;
                case "Uptown":
                    assertEquals(2, totalLoans, "Uptown branch should have 2 loans");
                    break;
                case "Westside":
                    assertEquals(2, totalLoans, "Westside branch should have 2 loans");
                    break;
            }
        });
    }

    @Test
    @Order(16)
    void testActiveAccountsSummary() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile(O16_ACTIVE_ACCOUNTS_SUMMARY);

        // Should have data for both account types
        assertEquals(2, results.size(), "Should return data for both savings and checking accounts");

        // Create map for easy access to results by account type
        Map<String, Map<String, Object>> accountTypeMap = new HashMap<>();
        for (Map<String, Object> result : results) {
            accountTypeMap.put((String) result.get("account_type"), result);
        }

        // Verify savings accounts
        Map<String, Object> savings = accountTypeMap.get("savings");
        assertNotNull(savings, "Should have data for savings accounts");
        assertEquals(3, ((Number) savings.get("active_account_count")).intValue(),
                "Should have 3 active savings accounts");

        // Verify checking accounts
        Map<String, Object> checking = accountTypeMap.get("checking");
        assertNotNull(checking, "Should have data for checking accounts");
        assertEquals(3, ((Number) checking.get("active_account_count")).intValue(),
                "Should have 3 active checking accounts");

        // Verify most recent transaction date
        results.forEach(type -> {
            Timestamp mostRecent = (Timestamp) type.get("most_recent_transaction");
            assertNotNull(mostRecent, "Should have recent transaction date");
            assertTrue(mostRecent.after(Timestamp.valueOf("2023-01-01 00:00:00")),
                    "Transactions should be in 2023");
        });
    }

    @Test
    @Order(17)
    void testEmployeePerformanceSummary() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile(O17_EMPLOYEE_PERFORMANCE_SUMMARY);

        // Should have data for all employees
        assertEquals(5, results.size(), "Should return data for all 5 employees");

        // Create a map to store branch account counts
        Map<String, Integer> branchAccounts = new HashMap<>();
        branchAccounts.put("Downtown", 2);  // Branch 1 has 2 accounts
        branchAccounts.put("Uptown", 2);    // Branch 2 has 2 accounts
        branchAccounts.put("Westside", 2);  // Branch 3 has 2 accounts

        // Verify structure and data for each employee
        for (Map<String, Object> employee : results) {
            String firstName = (String) employee.get("first_name");
            String lastName = (String) employee.get("last_name");
            int accountsManaged = ((Number) employee.get("accounts_managed")).intValue();
            int loansProcessed = ((Number) employee.get("loans_processed")).intValue();
            BigDecimal portfolioBalance = new BigDecimal(employee.get("portfolio_balance").toString());

            // Each employee should have valid counts
            assertTrue(accountsManaged >= 0, "Accounts managed should be non-negative");
            assertTrue(loansProcessed >= 0, "Loans processed should be non-negative");
            assertTrue(portfolioBalance.compareTo(BigDecimal.ZERO) >= 0,
                    "Portfolio balance should be non-negative");

            // Verify specific employees
            if ("John".equals(firstName) && "Smith".equals(lastName)) {
                assertEquals(2, accountsManaged,
                        "Downtown branch employees should manage 2 accounts");
            } else if ("Bob".equals(firstName) && "Wilson".equals(lastName)) {
                assertEquals(2, accountsManaged,
                        "Uptown branch employees should manage 2 accounts");
            } else if ("Charlie".equals(firstName) && "Davis".equals(lastName)) {
                assertEquals(2, accountsManaged,
                        "Westside branch employees should manage 2 accounts");
            }
        }

        // Calculate total accounts managed (should be 6 total, as each account is counted once per branch)
        int totalAccountsManaged = results.stream()
                .mapToInt(e -> ((Number) e.get("accounts_managed")).intValue())
                .sum();

        // Each branch has 2 accounts, and each employee in the branch sees those accounts
        int expectedTotal = 10; // (2 employees × 2 accounts) + (2 employees × 2 accounts) + (1 employee × 2 accounts)
        assertEquals(expectedTotal, totalAccountsManaged,
                "Total accounts managed should match test data structure");

        // Verify total loans
        int totalLoansProcessed = results.stream()
                .mapToInt(e -> ((Number) e.get("loans_processed")).intValue())
                .sum();
        assertEquals(8, totalLoansProcessed, "Total loans should match test data");
    }

    @Test
    @Order(18)
    void testCustomerProductAnalysis() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile(O18_CUSTOMER_PRODUCT_ANALYSIS);

        assertFalse(results.isEmpty(), "Should return customer analysis records");

        // Verify structure
        Map<String, Object> firstRow = results.get(0);
        assertNotNull(firstRow.get("first_name"));
        assertNotNull(firstRow.get("last_name"));
        assertNotNull(firstRow.get("total_transactions"));
        assertNotNull(firstRow.get("total_deposits"));
        assertNotNull(firstRow.get("total_withdrawals"));
        assertNotNull(firstRow.get("avg_balance"));
        assertNotNull(firstRow.get("loan_count"));
        assertNotNull(firstRow.get("total_loan_amount"));
        assertNotNull(firstRow.get("customer_value_score"));

        // Verify calculations
        results.forEach(row -> {
            // Value score should be non-negative
            assertTrue(((Number) row.get("customer_value_score")).doubleValue() >= 0,
                    "Customer value score should be non-negative");

            // Verify that metrics are present and valid
            assertTrue(((Number) row.get("total_transactions")).intValue() >= 0,
                    "Total transactions should be non-negative");
            assertTrue(((Number) row.get("total_deposits")).doubleValue() >= 0,
                    "Total deposits should be non-negative");
            assertTrue(((Number) row.get("total_withdrawals")).doubleValue() >= 0,
                    "Total withdrawals should be non-negative");
        });

        // Verify sorting
        double previousScore = Double.MAX_VALUE;
        for (Map<String, Object> row : results) {
            double currentScore = ((Number) row.get("customer_value_score")).doubleValue();
            assertTrue(currentScore <= previousScore, "Results should be sorted by value score descending");
            previousScore = currentScore;
        }
    }


    @Test
    @Order(19)
    void testBranchPerformanceMetrics() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile(O19_BRANCH_PERFORMANCE_METRICS);

        assertFalse(results.isEmpty(), "Should return branch performance records");

        // Verify structure
        Map<String, Object> firstRow = results.get(0);
        assertNotNull(firstRow.get("branch_name"));
        assertNotNull(firstRow.get("employee_count"));
        assertNotNull(firstRow.get("processed_loans"));
        assertNotNull(firstRow.get("avg_employee_salary"));
        assertNotNull(firstRow.get("account_count"));
        assertNotNull(firstRow.get("transaction_count"));
        assertNotNull(firstRow.get("transactions_per_account"));
        assertNotNull(firstRow.get("deposits_per_employee"));
        assertNotNull(firstRow.get("performance_score"));

        // Verify calculations
        results.forEach(row -> {
            // Performance score should be non-negative
            assertTrue(((Number) row.get("performance_score")).doubleValue() >= 0,
                    "Performance score should be non-negative");

            // Verify ratio calculations
            if (((Number) row.get("employee_count")).intValue() > 0) {
                assertTrue(((Number) row.get("deposits_per_employee")).doubleValue() >= 0,
                        "Deposits per employee should be non-negative");
            }

            if (((Number) row.get("account_count")).intValue() > 0) {
                assertTrue(((Number) row.get("transactions_per_account")).doubleValue() >= 0,
                        "Transactions per account should be non-negative");
            }
        });

        // Verify sorting
        double previousScore = Double.MAX_VALUE;
        for (Map<String, Object> row : results) {
            double currentScore = ((Number) row.get("performance_score")).doubleValue();
            assertTrue(currentScore <= previousScore, "Results should be sorted by performance score descending");
            previousScore = currentScore;
        }
    }

    // Helper method to get total account balance
    private BigDecimal getTotalAccountBalance() throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT SUM(balance) FROM accounts")) {
            rs.next();
            return rs.getBigDecimal(1);
        }
    }

    // Helper methods
    private double getAverageBalance() throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT AVG(balance) FROM accounts")) {
            rs.next();
            return rs.getDouble(1);
        }
    }

    private double getAverageTransactionCount() throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT AVG(tx_count) FROM (SELECT COUNT(*) as tx_count FROM transactions GROUP BY account_id) sub")) {
            rs.next();
            return rs.getDouble(1);
        }
    }

    private List<Map<String, Object>> executeQueryFromFile(String queryFileName) throws IOException, SQLException {
        String filePath = "src/test/resources/" + queryFileName;
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
        String sql = Files.readString(Paths.get("src/test/resources/" + fileName));
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    @AfterAll
    static void stopDatabase() throws IOException, SQLException {
        if (connection != null) {
            connection.close();
        }
        if (postgres != null) {
            postgres.close();
        }
    }
}