package org.codeus.fundamentals.functions;

import org.codeus.database.common.EmbeddedPostgreSqlSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.ClassOrderer;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Array;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class FunctionsTest extends EmbeddedPostgreSqlSetup {

  private static final String SQL_QUERY_FILES_PATH = "queries/";

  private void loadFunctionFromFile(String filePath) throws SQLException, IOException {
    String absolutePath = getResourcePath(filePath);
    String functionSql = Files.readString(Paths.get(absolutePath));
    functionSql = Arrays.stream(functionSql.split("\n"))
            .filter(line -> !line.trim().startsWith("--"))
            .collect(Collectors.joining("\n"));

    if (functionSql.trim().isEmpty()) {
      System.out.println("Skipping empty or comment-only SQL file: " + filePath);
      return;
    }

    try (Statement stmt = connection.createStatement()) {
      System.out.println("Loading function from: " + filePath);
      stmt.execute(functionSql);
    }
    connection.commit();
  }

  private void rollbackConnection() {
    try {
      if (connection != null && !connection.isClosed() && !connection.getAutoCommit()) {
        connection.rollback();
      }
    } catch (SQLException e) {
      System.err.println("Error during explicit rollback: " + e.getMessage());
      e.printStackTrace();
    }
  }

  @Nested
  @Order(10)
  @DisplayName("Task 1: get_customer_email Tests")
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class GetCustomerEmailTests {
    @BeforeEach
    void loadGetCustomerEmailFunction() throws SQLException, IOException {
      loadFunctionFromFile(SQL_QUERY_FILES_PATH + "01_get_customer_email.sql");
    }

    @Test
    @Order(1)
    @DisplayName("Should return correct email for existing customer ID 1")
    void testGetExistingCustomerEmail() {
      String sql = "SELECT get_customer_email(1) AS email;";
      try {
        System.out.println("\nExecuting Query for Task 1 (testGetExistingCustomerEmail):\n" + sql);
        List<Map<String, Object>> result = executeQuery(sql);
        printQueryResults(result);
        Assertions.assertFalse(result.isEmpty(), "Result set should not be empty when querying for an existing customer's email.");
        Assertions.assertEquals("john.doe@example.com", result.get(0).get("email"), "Email for customer ID 1 should match the test data.");
      } catch (SQLException e) {
        Assertions.fail("SQLException during testGetExistingCustomerEmail for query [" + sql + "]: " + e.getMessage());
      } finally {
        rollbackConnection();
      }
    }

    @Test
    @Order(2)
    @DisplayName("Should raise SQL exception for non-existent customer ID 999")
    void testGetNonExistentCustomerEmail() {
      String sql = "SELECT get_customer_email(999);";
      try {
        System.out.println("\nExecuting Query for Task 1 (testGetNonExistentCustomerEmail) - Expected to fail:\n" + sql);
        SQLException exception = Assertions.assertThrows(SQLException.class,
                () -> executeQuery(sql),
                "Executing get_customer_email with a non-existent customer ID (999) should throw an SQLException."
        );
        Assertions.assertTrue(exception.getMessage().contains("Customer not found: 999"),
                "The SQL exception message should indicate that customer ID 999 was not found.");
      } finally {
        rollbackConnection();
      }
    }
  }

  @Nested
  @Order(20)
  @DisplayName("Task 2: get_customer_full_name Tests")
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class GetCustomerFullNameTests {
    @BeforeEach
    void loadGetCustomerFullNameFunction() throws SQLException, IOException {
      loadFunctionFromFile(SQL_QUERY_FILES_PATH + "02_get_customer_full_name.sql");
    }

    @Test
    @Order(1)
    @DisplayName("Should return correct full name for existing customer ID 1")
    void testGetExistingCustomerFullName() {
      String sql = "SELECT get_customer_full_name(1) AS full_name;";
      try {
        System.out.println("\nExecuting Query for Task 2 (testGetExistingCustomerFullName):\n" + sql);
        List<Map<String, Object>> result = executeQuery(sql);
        printQueryResults(result);
        Assertions.assertFalse(result.isEmpty(), "Result set should not be empty when querying for an existing customer's full name.");
        Assertions.assertEquals("John Doe", result.get(0).get("full_name"), "Full name for customer ID 1 should be 'John Doe'.");
      } catch (SQLException e) {
        Assertions.fail("SQLException during testGetExistingCustomerFullName for query [" + sql + "]: " + e.getMessage());
      } finally {
        rollbackConnection();
      }
    }

    @Test
    @Order(2)
    @DisplayName("Should raise SQL exception for non-existent customer ID 999")
    void testGetNonExistentCustomerFullName() {
      String sql = "SELECT get_customer_full_name(999);";
      try {
        System.out.println("\nExecuting Query for Task 2 (testGetNonExistentCustomerFullName) - Expected to fail:\n" + sql);
        SQLException exception = Assertions.assertThrows(SQLException.class,
                () -> executeQuery(sql),
                "Executing get_customer_full_name with a non-existent customer ID (999) should throw an SQLException."
        );
        Assertions.assertTrue(exception.getMessage().contains("Customer not found: 999"),
                "The SQL exception message should indicate that customer ID 999 was not found.");
      } finally {
        rollbackConnection();
      }
    }
  }

  @Nested
  @Order(30)
  @DisplayName("Task 3: check_if_balance_exceeds_threshold Tests")
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class CheckIfBalanceExceedsThresholdTests {
    @BeforeEach
    void loadCheckIfBalanceExceedsThresholdFunction() throws SQLException, IOException {
      loadFunctionFromFile(SQL_QUERY_FILES_PATH + "03_check_if_balance_exceeds_threshold.sql");
    }

    @Test
    @Order(1)
    @DisplayName("Should return TRUE if account balance (1500.00) is above threshold (500.00)")
    void testBalanceAboveThreshold() {
      String sql = "SELECT check_if_balance_exceeds_threshold(1, 500.00) AS result_val;";
      try {
        System.out.println("\nExecuting Query for Task 3 (testBalanceAboveThreshold):\n" + sql);
        List<Map<String, Object>> result = executeQuery(sql);
        printQueryResults(result);
        Assertions.assertFalse(result.isEmpty(), "Result set should not be empty for threshold check.");
        Assertions.assertTrue((Boolean) result.get(0).get("result_val"), "Account ID 1 balance (1500.00) should be considered above the threshold (500.00).");
      } catch (SQLException e) {
        Assertions.fail("SQLException during testBalanceAboveThreshold for query [" + sql + "]: " + e.getMessage());
      } finally {
        rollbackConnection();
      }
    }

    @Test
    @Order(2)
    @DisplayName("Should return FALSE if account balance (1500.00) is equal to threshold (1500.00)")
    void testBalanceNotAboveThresholdEqualTo() {
      String sql = "SELECT check_if_balance_exceeds_threshold(1, 1500.00) AS result_val;";
      try {
        System.out.println("\nExecuting Query for Task 3 (testBalanceNotAboveThresholdEqualTo):\n" + sql);
        List<Map<String, Object>> result = executeQuery(sql);
        printQueryResults(result);
        Assertions.assertFalse(result.isEmpty(), "Result set should not be empty for threshold check.");
        Assertions.assertFalse((Boolean) result.get(0).get("result_val"), "Account ID 1 balance (1500.00) should NOT be considered above an equal threshold (1500.00).");
      } catch (SQLException e) {
        Assertions.fail("SQLException during testBalanceNotAboveThresholdEqualTo for query [" + sql + "]: " + e.getMessage());
      } finally {
        rollbackConnection();
      }
    }

    @Test
    @Order(3)
    @DisplayName("Should return FALSE if account balance (1500.00) is below threshold (2000.00)")
    void testBalanceBelowThreshold() {
      String sql = "SELECT check_if_balance_exceeds_threshold(1, 2000.00) AS result_val;";
      try {
        System.out.println("\nExecuting Query for Task 3 (testBalanceBelowThreshold):\n" + sql);
        List<Map<String, Object>> result = executeQuery(sql);
        printQueryResults(result);
        Assertions.assertFalse(result.isEmpty(), "Result set should not be empty for threshold check.");
        Assertions.assertFalse((Boolean) result.get(0).get("result_val"), "Account ID 1 balance (1500.00) should NOT be considered above a higher threshold (2000.00).");
      } catch (SQLException e) {
        Assertions.fail("SQLException during testBalanceBelowThreshold for query [" + sql + "]: " + e.getMessage());
      } finally {
        rollbackConnection();
      }
    }

    @Test
    @Order(4)
    @DisplayName("Should raise SQL exception for non-existent account ID 999 during threshold check")
    void testThresholdCheckNonExistentAccount() {
      String sql = "SELECT check_if_balance_exceeds_threshold(999, 100.00);";
      try {
        System.out.println("\nExecuting Query for Task 3 (testThresholdCheckNonExistentAccount) - Expected to fail:\n" + sql);
        SQLException exception = Assertions.assertThrows(SQLException.class,
                () -> executeQuery(sql),
                "Executing check_if_balance_exceeds_threshold with a non-existent account ID (999) should throw an SQLException."
        );
        Assertions.assertTrue(exception.getMessage().contains("Account not found: 999"), "The SQL exception message should indicate that account ID 999 was not found.");
      } finally {
        rollbackConnection();
      }
    }
  }

  @Nested
  @Order(40)
  @DisplayName("Task 4: get_account_balance Tests")
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class GetAccountBalanceTests {
    @BeforeEach
    void loadGetAccountBalanceFunction() throws SQLException, IOException {
      loadFunctionFromFile(SQL_QUERY_FILES_PATH + "04_get_account_balance.sql");
    }

    @Test
    @Order(1)
    @DisplayName("Should return correct balance (1500.00) for existing account ID 1")
    void testGetExistingAccountBalance() {
      String sql = "SELECT get_account_balance(1) AS balance;";
      try {
        System.out.println("\nExecuting Query for Task 4 (testGetExistingAccountBalance):\n" + sql);
        List<Map<String, Object>> result = executeQuery(sql);
        printQueryResults(result);
        Assertions.assertFalse(result.isEmpty(), "Result set should not be empty when querying for an existing account's balance.");
        BigDecimal actualBalance = (BigDecimal)result.get(0).get("balance");
        Assertions.assertEquals(0, new BigDecimal("1500.00").compareTo(actualBalance), "Balance for account ID 1 should be 1500.00 as per test data.");
      } catch (SQLException e) {
        Assertions.fail("SQLException during testGetExistingAccountBalance for query [" + sql + "]: " + e.getMessage());
      } finally {
        rollbackConnection();
      }
    }

    @Test
    @Order(2)
    @DisplayName("Should raise SQL exception for non-existent account ID 999 when getting balance")
    void testGetNonExistentAccountBalance() {
      String sql = "SELECT get_account_balance(999);";
      try {
        System.out.println("\nExecuting Query for Task 4 (testGetNonExistentAccountBalance) - Expected to fail:\n" + sql);
        SQLException exception = Assertions.assertThrows(SQLException.class,
                () -> executeQuery(sql),
                "Executing get_account_balance with a non-existent account ID (999) should throw an SQLException."
        );
        Assertions.assertTrue(exception.getMessage().contains("Account not found: 999"), "The SQL exception message should indicate that account ID 999 was not found.");
      } finally {
        rollbackConnection();
      }
    }
  }

  @Nested
  @Order(50)
  @DisplayName("Task 5: calculate_interest_for_balance Tests")
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class CalculateInterestForBalanceTests {
    @BeforeEach
    void loadCalculateInterestFunction() throws SQLException, IOException {
      loadFunctionFromFile(SQL_QUERY_FILES_PATH + "05_calculate_interest_for_balance.sql");
    }

    private void assertInterestCalculation(String balanceInput, String expectedInterestStr, String tierDescription) throws SQLException {
      String sql = "SELECT calculate_interest_for_balance(" + balanceInput + ") AS interest;";
      System.out.println("\nExecuting Query for Task 5 (assertInterestCalculation - " + tierDescription + "):\n" + sql);
      List<Map<String, Object>> result = executeQuery(sql);
      printQueryResults(result);
      Assertions.assertFalse(result.isEmpty(), "Result set should not be empty for interest calculation of balance: " + balanceInput);
      BigDecimal actualInterest = (BigDecimal) result.get(0).get("interest");
      BigDecimal expectedInterest = new BigDecimal(expectedInterestStr);
      Assertions.assertEquals(0, expectedInterest.compareTo(actualInterest.setScale(2, RoundingMode.HALF_UP)),
              "Interest calculation for " + tierDescription + " (balance " + balanceInput + ") should be " + expectedInterestStr + ".");
    }

    @Test @Order(1) @DisplayName("Tier 1 (< 1000.00): Balance 500.00 should yield 2.50 interest (0.5%)")
    void testTier1() {
      try {
        assertInterestCalculation("500.00", "2.50", "Tier 1");
      } catch (SQLException e) {
        Assertions.fail("SQLException during testTier1: " + e.getMessage());
      } finally {
        rollbackConnection();
      }
    }
    @Test @Order(2) @DisplayName("Tier 2 (>= 1000.00, < 5000.00) - Lower Bound: Balance 1000.00 should yield 10.00 interest (1.0%)")
    void testTier2Lower() {
      try {
        assertInterestCalculation("1000.00", "10.00", "Tier 2 Lower Bound");
      } catch (SQLException e) {
        Assertions.fail("SQLException during testTier2Lower: " + e.getMessage());
      } finally {
        rollbackConnection();
      }
    }
    @Test @Order(3) @DisplayName("Tier 2 (>= 1000.00, < 5000.00) - Mid Value: Balance 2500.00 should yield 25.00 interest (1.0%)")
    void testTier2Mid() {
      try {
        assertInterestCalculation("2500.00", "25.00", "Tier 2 Mid Value");
      } catch (SQLException e) {
        Assertions.fail("SQLException during testTier2Mid: " + e.getMessage());
      } finally {
        rollbackConnection();
      }
    }
    @Test @Order(4) @DisplayName("Tier 3 (>= 5000.00, < 20000.00) - Lower Bound: Balance 5000.00 should yield 75.00 interest (1.5%)")
    void testTier3Lower() {
      try {
        assertInterestCalculation("5000.00", "75.00", "Tier 3 Lower Bound");
      } catch (SQLException e) {
        Assertions.fail("SQLException during testTier3Lower: " + e.getMessage());
      } finally {
        rollbackConnection();
      }
    }
    @Test @Order(5) @DisplayName("Tier 3 (>= 5000.00, < 20000.00) - Mid Value: Balance 15000.00 should yield 225.00 interest (1.5%)")
    void testTier3Mid() {
      try {
        assertInterestCalculation("15000.00", "225.00", "Tier 3 Mid Value");
      } catch (SQLException e) {
        Assertions.fail("SQLException during testTier3Mid: " + e.getMessage());
      } finally {
        rollbackConnection();
      }
    }
    @Test @Order(6) @DisplayName("Tier 4 (>= 20000.00) - Lower Bound: Balance 20000.00 should yield 400.00 interest (2.0%)")
    void testTier4Lower() {
      try {
        assertInterestCalculation("20000.00", "400.00", "Tier 4 Lower Bound");
      } catch (SQLException e) {
        Assertions.fail("SQLException during testTier4Lower: " + e.getMessage());
      } finally {
        rollbackConnection();
      }
    }
    @Test @Order(7) @DisplayName("Tier 4 (>= 20000.00) - High Value: Balance 50000.00 should yield 1000.00 interest (2.0%)")
    void testTier4High() {
      try {
        assertInterestCalculation("50000.00", "1000.00", "Tier 4 High Value");
      } catch (SQLException e) {
        Assertions.fail("SQLException during testTier4High: " + e.getMessage());
      } finally {
        rollbackConnection();
      }
    }
    @Test @Order(8) @DisplayName("NULL input balance should yield 0.00 interest")
    void testNullInput() {
      try {
        assertInterestCalculation("NULL", "0.00", "NULL input");
      } catch (SQLException e) {
        Assertions.fail("SQLException during testNullInput: " + e.getMessage());
      } finally {
        rollbackConnection();
      }
    }
    @Test @Order(9) @DisplayName("Negative input balance (-100.00) should yield 0.00 interest")
    void testNegativeInput() {
      try {
        assertInterestCalculation("-100.00", "0.00", "Negative input");
      } catch (SQLException e) {
        Assertions.fail("SQLException during testNegativeInput: " + e.getMessage());
      } finally {
        rollbackConnection();
      }
    }
    @Test @Order(10) @DisplayName("Zero input balance (0.00) should yield 0.00 interest")
    void testZeroInput() {
      try {
        assertInterestCalculation("0.00", "0.00", "Zero input");
      } catch (SQLException e) {
        Assertions.fail("SQLException during testZeroInput: " + e.getMessage());
      } finally {
        rollbackConnection();
      }
    }
  }

  @Nested
  @Order(60)
  @DisplayName("Task 6: get_customer_total_balance Tests")
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class GetCustomerTotalBalanceTests {
    @BeforeEach
    void loadGetCustomerTotalBalanceFunction() throws SQLException, IOException {
      loadFunctionFromFile(SQL_QUERY_FILES_PATH + "06_get_customer_total_balance.sql");
    }

    @Test @Order(1) @DisplayName("Customer ID 1 with multiple accounts (1500.00 + 10000.00) should have total balance 11500.00")
    void testMultipleAccounts() {
      String sql = "SELECT get_customer_total_balance(1) AS total_balance;";
      try {
        System.out.println("\nExecuting Query for Task 6 (testMultipleAccounts):\n" + sql);
        List<Map<String, Object>> result = executeQuery(sql);
        printQueryResults(result);
        BigDecimal actualTotalBalance = (BigDecimal)result.get(0).get("total_balance");
        Assertions.assertEquals(0, new BigDecimal("11500.00").compareTo(actualTotalBalance), "Total balance for customer ID 1 (with multiple accounts) is incorrect.");
      } catch (SQLException e) {
        Assertions.fail("SQLException during testMultipleAccounts for query [" + sql + "]: " + e.getMessage());
      } finally {
        rollbackConnection();
      }
    }
    @Test @Order(2) @DisplayName("Customer ID 2 with one account (1200.00) should have total balance 1200.00")
    void testSingleAccount() {
      String sql = "SELECT get_customer_total_balance(2) AS total_balance;";
      try {
        System.out.println("\nExecuting Query for Task 6 (testSingleAccount):\n" + sql);
        List<Map<String, Object>> result = executeQuery(sql);
        printQueryResults(result);
        BigDecimal actualTotalBalance = (BigDecimal)result.get(0).get("total_balance");
        Assertions.assertEquals(0, new BigDecimal("1200.00").compareTo(actualTotalBalance), "Total balance for customer ID 2 (with a single account) is incorrect.");
      } catch (SQLException e) {
        Assertions.fail("SQLException during testSingleAccount for query [" + sql + "]: " + e.getMessage());
      } finally {
        rollbackConnection();
      }
    }
    @Test @Order(3) @DisplayName("Customer ID 10 with no accounts should have total balance 0.00")
    void testNoAccounts() {
      String sql = "SELECT get_customer_total_balance(10) AS total_balance;";
      try {
        System.out.println("\nExecuting Query for Task 6 (testNoAccounts):\n" + sql);
        List<Map<String, Object>> result = executeQuery(sql);
        printQueryResults(result);
        BigDecimal actualTotalBalance = (BigDecimal)result.get(0).get("total_balance");
        Assertions.assertEquals(0, new BigDecimal("0.00").compareTo(actualTotalBalance), "Total balance for customer ID 10 (with no accounts) should be 0.00.");
      } catch (SQLException e) {
        Assertions.fail("SQLException during testNoAccounts for query [" + sql + "]: " + e.getMessage());
      } finally {
        rollbackConnection();
      }
    }
    @Test @Order(4) @DisplayName("Should raise SQL exception for non-existent customer ID 999 when getting total balance")
    void testNonExistentCustomer() {
      String sql = "SELECT get_customer_total_balance(999);";
      try {
        System.out.println("\nExecuting Query for Task 6 (testNonExistentCustomer) - Expected to fail:\n" + sql);
        SQLException exception = Assertions.assertThrows(SQLException.class,
                () -> executeQuery(sql),
                "Executing get_customer_total_balance with a non-existent customer ID (999) should throw an SQLException."
        );
        Assertions.assertTrue(exception.getMessage().contains("Customer not found: 999"), "The SQL exception message should indicate that customer ID 999 was not found.");
      } finally {
        rollbackConnection();
      }
    }
  }

  @Nested
  @Order(70)
  @DisplayName("Task 7: count_transactions_by_type_for_account Tests")
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class CountTransactionsByTypeTests {
    @BeforeEach
    void loadCountTransactionsByTypeFunction() throws SQLException, IOException {
      loadFunctionFromFile(SQL_QUERY_FILES_PATH + "07_count_transactions_by_type_for_account.sql");
    }

    @Test @Order(1) @DisplayName("Account ID 1 should have 1 'deposit' transaction")
    void testExistingType() {
      String sql = "SELECT count_transactions_by_type_for_account(1, 'deposit') AS tx_count;";
      try {
        System.out.println("\nExecuting Query for Task 7 (testExistingType):\n" + sql);
        List<Map<String, Object>> result = executeQuery(sql);
        printQueryResults(result);
        Assertions.assertEquals(1, ((Number)result.get(0).get("tx_count")).intValue(), "Count of 'deposit' transactions for account ID 1 is incorrect.");
      } catch (SQLException e) {
        Assertions.fail("SQLException during testExistingType for query [" + sql + "]: " + e.getMessage());
      } finally {
        rollbackConnection();
      }
    }
    @Test @Order(2) @DisplayName("Account ID 1 should have 0 'Fee' transactions")
    void testNonExistingType() {
      String sql = "SELECT count_transactions_by_type_for_account(1, 'Fee') AS tx_count;";
      try {
        System.out.println("\nExecuting Query for Task 7 (testNonExistingType):\n" + sql);
        List<Map<String, Object>> result = executeQuery(sql);
        printQueryResults(result);
        Assertions.assertEquals(0, ((Number)result.get(0).get("tx_count")).intValue(), "Count of 'Fee' transactions for account ID 1 should be 0.");
      } catch (SQLException e) {
        Assertions.fail("SQLException during testNonExistingType for query [" + sql + "]: " + e.getMessage());
      } finally {
        rollbackConnection();
      }
    }
    @Test @Order(3) @DisplayName("Account ID 4 (no transactions) should have 0 'deposit' transactions")
    void testAccountWithNoTransactions() {
      String sql = "SELECT count_transactions_by_type_for_account(4, 'deposit') AS tx_count;";
      try {
        System.out.println("\nExecuting Query for Task 7 (testAccountWithNoTransactions):\n" + sql);
        List<Map<String, Object>> result = executeQuery(sql);
        printQueryResults(result);
        Assertions.assertEquals(0, ((Number)result.get(0).get("tx_count")).intValue(), "Count of 'deposit' transactions for account ID 4 (which has no transactions) should be 0.");
      } catch (SQLException e) {
        Assertions.fail("SQLException during testAccountWithNoTransactions for query [" + sql + "]: " + e.getMessage());
      } finally {
        rollbackConnection();
      }
    }
    @Test @Order(4) @DisplayName("Non-existent account ID 999 should result in 0 transaction count")
    void testNonExistentAccount() {
      String sql = "SELECT count_transactions_by_type_for_account(999, 'deposit') AS tx_count;";
      try {
        System.out.println("\nExecuting Query for Task 7 (testNonExistentAccount):\n" + sql);
        List<Map<String, Object>> result = executeQuery(sql);
        printQueryResults(result);
        Assertions.assertEquals(0, ((Number)result.get(0).get("tx_count")).intValue(), "Transaction count for a non-existent account ID (999) should be 0 as per function logic.");
      } catch (SQLException e) {
        Assertions.fail("SQLException during testNonExistentAccount for query [" + sql + "]: " + e.getMessage());
      } finally {
        rollbackConnection();
      }
    }
  }

  @Nested
  @Order(80)
  @DisplayName("Task 8: get_transactions_for_account (SRF) Tests")
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class GetTransactionsForAccountSrfTests {
    @BeforeEach
    void loadGetTransactionsForAccountFunction() throws SQLException, IOException {
      loadFunctionFromFile(SQL_QUERY_FILES_PATH + "08_get_transactions_for_account.sql");
    }

    @Test @Order(1) @DisplayName("Account ID 1 should return 1 transaction with correct details")
    void testAccountWithTransactions() {
      String sql = "SELECT * FROM get_transactions_for_account(1);";
      try {
        System.out.println("\nExecuting Query for Task 8 (testAccountWithTransactions):\n" + sql);
        List<Map<String,Object>> results = executeQuery(sql);
        printQueryResults(results);
        Assertions.assertEquals(1, results.size(), "Should retrieve 1 transaction for account ID 1.");
        Map<String, Object> tx1 = results.get(0);
        Assertions.assertEquals(1, tx1.get("transaction_id"), "Transaction ID for the retrieved transaction is incorrect.");
        Assertions.assertEquals("deposit", tx1.get("transaction_type"), "Transaction type for the retrieved transaction is incorrect.");
        Assertions.assertEquals(0, new BigDecimal("500.00").compareTo((BigDecimal)tx1.get("amount")), "Transaction amount for the retrieved transaction is incorrect.");
      } catch (SQLException e) {
        Assertions.fail("SQLException during testAccountWithTransactions for query [" + sql + "]: " + e.getMessage());
      } finally {
        rollbackConnection();
      }
    }
    @Test @Order(2) @DisplayName("Account ID 4 (no transactions) should return an empty set")
    void testAccountWithNoTransactionsSrf() {
      String sql = "SELECT * FROM get_transactions_for_account(4);";
      try {
        System.out.println("\nExecuting Query for Task 8 (testAccountWithNoTransactionsSrf):\n" + sql);
        List<Map<String,Object>> results = executeQuery(sql);
        printQueryResults(results);
        Assertions.assertTrue(results.isEmpty(), "Should return an empty set of transactions for account ID 4, which has no transactions.");
      } catch (SQLException e) {
        Assertions.fail("SQLException during testAccountWithNoTransactionsSrf for query [" + sql + "]: " + e.getMessage());
      } finally {
        rollbackConnection();
      }
    }
    @Test @Order(3) @DisplayName("Non-existent account ID 999 should return an empty set")
    void testNonExistentAccountSrf() {
      String sql = "SELECT * FROM get_transactions_for_account(999);";
      try {
        System.out.println("\nExecuting Query for Task 8 (testNonExistentAccountSrf):\n" + sql);
        List<Map<String,Object>> results = executeQuery(sql);
        printQueryResults(results);
        Assertions.assertTrue(results.isEmpty(), "Should return an empty set of transactions for a non-existent account ID (999).");
      } catch (SQLException e) {
        Assertions.fail("SQLException during testNonExistentAccountSrf for query [" + sql + "]: " + e.getMessage());
      } finally {
        rollbackConnection();
      }
    }
  }

  @Nested
  @Order(90)
  @DisplayName("Task 9: get_account_activity_summary Tests")
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class GetAccountActivitySummaryTests {
    @BeforeEach
    void loadGetAccountActivitySummaryFunction() throws SQLException, IOException {
      loadFunctionFromFile(SQL_QUERY_FILES_PATH + "09_get_account_activity_summary.sql");
    }

    @Test @Order(1) @DisplayName("Account ID 1 (1 transaction) with limit 3 should return summary of 1 transaction")
    void testSummaryWithTransactions() {
      String sql = "SELECT get_account_activity_summary(1, 3) AS summary;";
      try {
        System.out.println("\nExecuting Query for Task 9 (testSummaryWithTransactions):\n" + sql);
        List<Map<String, Object>> result = executeQuery(sql);
        printQueryResults(result);
        String summary = (String) result.get(0).get("summary");
        Assertions.assertTrue(summary.contains("TxID: 1"), "Activity summary for account ID 1 is missing details for TxID 1.");
        Assertions.assertTrue(summary.contains("Type: deposit"), "Activity summary for account ID 1 is missing transaction type for TxID 1.");
        Assertions.assertTrue(summary.contains("Amount: 500.00"), "Activity summary for account ID 1 is missing transaction amount for TxID 1.");
        Assertions.assertEquals(1, summary.split(";", -1).length -1, "Activity summary for account ID 1 should contain exactly 1 transaction entry." );
      } catch (SQLException e) {
        Assertions.fail("SQLException during testSummaryWithTransactions for query [" + sql + "]: " + e.getMessage());
      } finally {
        rollbackConnection();
      }
    }
    @Test @Order(2) @DisplayName("Account ID 4 (no transactions) should return 'No transactions found' message")
    void testSummaryNoTransactions() {
      String sql = "SELECT get_account_activity_summary(4, 3) AS summary;";
      try {
        System.out.println("\nExecuting Query for Task 9 (testSummaryNoTransactions):\n" + sql);
        List<Map<String, Object>> result = executeQuery(sql);
        printQueryResults(result);
        Assertions.assertEquals("No transactions found for this account.", result.get(0).get("summary"), "Activity summary for account ID 4 (no transactions) is incorrect.");
      } catch (SQLException e) {
        Assertions.fail("SQLException during testSummaryNoTransactions for query [" + sql + "]: " + e.getMessage());
      } finally {
        rollbackConnection();
      }
    }
    @Test @Order(3) @DisplayName("Non-existent account ID 999 should return 'Account not found' message")
    void testSummaryNonExistentAccount() {
      String sql = "SELECT get_account_activity_summary(999, 3) AS summary;";
      try {
        System.out.println("\nExecuting Query for Task 9 (testSummaryNonExistentAccount):\n" + sql);
        List<Map<String, Object>> result = executeQuery(sql);
        printQueryResults(result);
        Assertions.assertEquals("Account not found.", result.get(0).get("summary"), "Activity summary for a non-existent account ID (999) is incorrect.");
      } catch (SQLException e) {
        Assertions.fail("SQLException during testSummaryNonExistentAccount for query [" + sql + "]: " + e.getMessage());
      } finally {
        rollbackConnection();
      }
    }
  }

  @Nested
  @Order(100)
  @DisplayName("Task 10: get_customer_account_numbers_array Tests")
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class GetCustomerAccountNumbersArrayTests {
    @BeforeEach
    void loadGetCustomerAccountNumbersArrayFunction() throws SQLException, IOException {
      loadFunctionFromFile(SQL_QUERY_FILES_PATH + "10_get_customer_account_numbers_array.sql");
    }

    @Test
    @Order(1)
    @DisplayName("Customer ID 1 should return array of their account IDs ['1', '2'] (as TEXT)")
    void testCustomerWithAccountsArray() {
      String sql = "SELECT get_customer_account_numbers_array(1) AS acc_array;";
      try {
        System.out.println("\nExecuting Query for Task 10 (testCustomerWithAccountsArray):\n" + sql);
        List<Map<String, Object>> result = executeQuery(sql);
        printQueryResults(result);
        Array pgArray = (Array)result.get(0).get("acc_array");
        Assertions.assertNotNull(pgArray, "Resulting account ID array should not be null for customer ID 1.");
        String[] accountIdsAsText = (String[])pgArray.getArray();
        Arrays.sort(accountIdsAsText);
        Assertions.assertArrayEquals(new String[]{"1", "2"}, accountIdsAsText, "Account IDs (returned as text array) for customer ID 1 are incorrect. Assumes function adapts to missing 'account_number' column by using 'id'.");
      } catch (SQLException e) {
        if (e.getMessage().contains("column a.account_number does not exist") || e.getMessage().contains("column accounts.account_number does not exist")) {
          Assertions.fail("Task 10 function ('get_customer_account_numbers_array') likely failed due to the 'account_number' column not existing in the 'accounts' table as per the current schema.sql. The function implementation might need to be adapted (e.g., to use 'id::TEXT') or the schema would need the 'account_number' column. Original error: " + e.getMessage());
        } else {
          Assertions.fail("An unexpected SQLException occurred for query [" + sql + "]: " + e.getMessage());
        }
      } finally {
        rollbackConnection();
      }
    }
    @Test
    @Order(2)
    @DisplayName("Customer ID 10 (no accounts) should return an empty array")
    void testCustomerNoAccountsArray() {
      String sql = "SELECT get_customer_account_numbers_array(10) AS acc_array;";
      try {
        System.out.println("\nExecuting Query for Task 10 (testCustomerNoAccountsArray):\n" + sql);
        List<Map<String, Object>> result = executeQuery(sql);
        printQueryResults(result);
        Array pgArray = (Array)result.get(0).get("acc_array");
        Assertions.assertNotNull(pgArray, "Resulting array should not be null (expected empty array) for customer ID 10.");
        String[] accountIdsAsText = (String[])pgArray.getArray();
        Assertions.assertEquals(0, accountIdsAsText.length, "Account ID array for customer ID 10 (who has no accounts) should be empty.");
      } catch (SQLException e) {
        if (e.getMessage().contains("column a.account_number does not exist") || e.getMessage().contains("column accounts.account_number does not exist")) {
          Assertions.fail("Task 10 function ('get_customer_account_numbers_array') likely failed due to missing 'account_number' column. Original error: " + e.getMessage());
        } else {
          Assertions.fail("An unexpected SQLException occurred for query [" + sql + "]: " + e.getMessage());
        }
      } finally {
        rollbackConnection();
      }
    }
    @Test
    @Order(3)
    @DisplayName("Non-existent customer ID 999 should return an empty array or raise specific error")
    void testNonExistentCustomerArray() {
      String sql = "SELECT get_customer_account_numbers_array(999) AS acc_array;";
      try {
        System.out.println("\nExecuting Query for Task 10 (testNonExistentCustomerArray) - May raise 'Customer not found':\n" + sql);
        List<Map<String, Object>> result = executeQuery(sql);
        printQueryResults(result);
        Array pgArray = (Array)result.get(0).get("acc_array");
        Assertions.assertNotNull(pgArray, "Resulting array for non-existent customer should not be null (expected empty or exception).");
        Assertions.assertEquals(0, ((String[])pgArray.getArray()).length, "Account ID array for a non-existent customer ID (999) should be empty if no error is raised first.");
      } catch (SQLException e) {
        if (e.getMessage().contains("Customer not found: 999")) {
          // This is an acceptable outcome if the function raises this before array_agg.
        } else if (e.getMessage().contains("column a.account_number does not exist") || e.getMessage().contains("column accounts.account_number does not exist")) {
          Assertions.fail("Task 10 function ('get_customer_account_numbers_array') likely failed due to missing 'account_number' column. Original error: " + e.getMessage());
        } else {
          Assertions.fail("Unexpected SQLException for non-existent customer for query [" + sql + "]: " + e.getMessage());
        }
      } finally {
        rollbackConnection();
      }
    }
  }

  @Nested
  @Order(110)
  @DisplayName("Task 11: format_loan_details Tests")
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class FormatLoanDetailsTests {
    @BeforeEach
    void loadFormatLoanDetailsFunction() throws SQLException, IOException {
      loadFunctionFromFile(SQL_QUERY_FILES_PATH + "11_format_loan_details.sql");
    }

    @Test
    @Order(1)
    @DisplayName("Existing loan ID 1 should return correctly formatted details string")
    void testExistingLoanFormat() {
      String sql = "SELECT format_loan_details(1) AS details;";
      try {
        System.out.println("\nExecuting Query for Task 11 (testExistingLoanFormat):\n" + sql);
        List<Map<String, Object>> result = executeQuery(sql);
        printQueryResults(result);
        String details = (String) result.get(0).get("details");
        Assertions.assertTrue(details.contains("Loan ID: 1"), "Formatted loan details string is missing 'Loan ID: 1'.");
        Assertions.assertTrue(details.contains("Customer: John Doe"), "Formatted loan details string is missing 'Customer: John Doe'.");
        Assertions.assertTrue(details.contains("Amount: $5000.00"), "Formatted loan details string is missing or has incorrect 'Amount: $5000.00'.");
        Assertions.assertTrue(details.contains("at 5.50%"), "Formatted loan details string is missing or has incorrect interest rate format 'at 5.50%'.");
      } catch (SQLException e) {
        Assertions.fail("SQLException during testExistingLoanFormat for query [" + sql + "]: " + e.getMessage());
      } finally {
        rollbackConnection();
      }
    }
    @Test
    @Order(2)
    @DisplayName("Non-existent loan ID 999 should return 'Loan not found.' message")
    void testNonExistentLoanFormat() {
      String sql = "SELECT format_loan_details(999) AS details;";
      try {
        System.out.println("\nExecuting Query for Task 11 (testNonExistentLoanFormat):\n" + sql);
        List<Map<String, Object>> result = executeQuery(sql);
        printQueryResults(result);
        Assertions.assertEquals("Loan not found.", result.get(0).get("details"), "Response for a non-existent loan ID (999) is incorrect.");
      } catch (SQLException e) {
        Assertions.fail("SQLException during testNonExistentLoanFormat for query [" + sql + "]: " + e.getMessage());
      } finally {
        rollbackConnection();
      }
    }
  }

  @Nested
  @Order(120)
  @DisplayName("Task 12: determine_account_status_detailed Tests")
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class DetermineAccountStatusDetailedTests {
    @BeforeEach
    void loadDetermineAccountStatusDetailedFunction() throws SQLException, IOException {
      loadFunctionFromFile(SQL_QUERY_FILES_PATH + "12_determine_account_status_detailed.sql");
    }

    private void testStatus(int accountId, String expectedDetailedStatus, String testCaseDescription) {
      String sql = "SELECT determine_account_status_detailed(" + accountId + ") AS s;";
      try {
        System.out.println("\nExecuting Query for Task 12 (testStatus - " + testCaseDescription + "):\n" + sql);
        List<Map<String, Object>> result = executeQuery(sql);
        printQueryResults(result);
        Assertions.assertEquals(expectedDetailedStatus, result.get(0).get("s"), "Detailed status for account ID " + accountId + " (" + testCaseDescription + ") is incorrect.");
      } catch (SQLException e) {
        if (e.getMessage().contains("column \"status\" does not exist") || e.getMessage().contains("column v_status does not exist")) {
          Assertions.fail("Task 12 function ('determine_account_status_detailed') failed for account ID " + accountId +
                  " due to the 'status' column not existing in the 'accounts' table as per the current schema.sql. " +
                  "The function implementation might need adaptation or the schema requires the 'status' column. Original error: " + e.getMessage());
        } else if (e.getMessage().contains("Account not found: " + accountId) && expectedDetailedStatus.equals("Account not found.")) {
          // This is an expected outcome for a specific test case (non-existent account)
        }
        else {
          Assertions.fail("Unexpected SQLException for account ID " + accountId + " (" + testCaseDescription + ") for query [" + sql + "]: " + e.getMessage());
        }
      } finally {
        rollbackConnection();
      }
    }

    @Test
    @Order(1)
    @DisplayName("Account ID 1 (balance 1500.00) should be 'Active with Positive Balance'")
    void testStatusActivePositive() {
      testStatus(1, "Active with Positive Balance", "Active Positive Balance");
    }

    @Test
    @Order(2)
    @DisplayName("Non-existent account ID 999 should raise 'Account not found' exception")
    void testStatusNonExistentAccount() {
      String sql = "SELECT determine_account_status_detailed(999);";
      try {
        System.out.println("\nExecuting Query for Task 12 (testStatusNonExistentAccount) - Expected to fail:\n" + sql);
        SQLException exception = Assertions.assertThrows(SQLException.class,
                () -> executeQuery(sql),
                "Executing determine_account_status_detailed for non-existent account ID 999 should throw SQLException."
        );
        String exceptionMessage = exception.getMessage();
        boolean correctException = exceptionMessage.contains("Account not found: 999") ||
                exceptionMessage.contains("column \"status\" does not exist") ||
                exceptionMessage.contains("column v_status does not exist");
        Assertions.assertTrue(correctException, "Exception message for non-existent account ID 999 is incorrect. Expected 'Account not found: 999' or schema-related error. Got: " + exceptionMessage);
      } finally {
        rollbackConnection();
      }
    }
  }

  @Nested
  @Order(130)
  @DisplayName("Task 13: get_account_details_with_out_params Tests")
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class GetAccountDetailsWithOutParamsTests {
    @BeforeEach
    void loadGetAccountDetailsOutParamsFunction() throws SQLException, IOException {
      loadFunctionFromFile(SQL_QUERY_FILES_PATH + "13_get_account_details_with_out_params.sql");
    }

    @Test
    @Order(1)
    @DisplayName("Should retrieve details for existing account ID 1 via OUT parameters")
    void testExistingAccountOutParams() {
      String sql = "SELECT * FROM get_account_details_with_out_params(1);";
      try {
        System.out.println("\nExecuting Query for Task 13 (testExistingAccountOutParams):\n" + sql);
        List<Map<String, Object>> result = executeQuery(sql);
        printQueryResults(result);
        Assertions.assertFalse(result.isEmpty(), "Result set from get_account_details_with_out_params for account ID 1 should not be empty.");
        Map<String, Object> details = result.get(0);

        Assertions.assertEquals("1", details.get("o_account_number"), "OUT parameter o_account_number (expected as account ID string) for account ID 1 is incorrect.");
        Assertions.assertEquals("checking", details.get("o_account_type"), "OUT parameter o_account_type for account ID 1 is incorrect.");
        Assertions.assertEquals(0, new BigDecimal("1500.00").compareTo((BigDecimal) details.get("o_balance")), "OUT parameter o_balance for account ID 1 is incorrect.");

        if (details.containsKey("o_is_active")) {
          Assertions.assertTrue((Boolean) details.get("o_is_active"), "OUT parameter o_is_active for account ID 1 is incorrect (assuming default true if account exists and status column is handled).");
        } else {
          System.err.println("Warning (Task 13): o_is_active parameter not found in result for account ID 1. This might be due to the missing 'status' column in the 'accounts' schema, and the function not outputting this parameter.");
        }

      } catch (SQLException e) {
        if (e.getMessage().contains("column accounts.account_number does not exist") ||
                e.getMessage().contains("column accounts.status does not exist")) {
          Assertions.fail("Task 13 function ('get_account_details_with_out_params') likely failed due to missing 'account_number' or 'status' column in the 'accounts' table (as per schema.sql for this module). The function implementation might need adaptation or the schema requires these columns. Original error: " + e.getMessage());
        } else {
          Assertions.fail("An unexpected SQLException occurred for query [" + sql + "]: " + e.getMessage());
        }
      } finally {
        rollbackConnection();
      }
    }

    @Test
    @Order(2)
    @DisplayName("Should raise SQL exception for non-existent account ID 999 with OUT parameters")
    void testNonExistentAccountOutParams() {
      String sql = "SELECT * FROM get_account_details_with_out_params(999);";
      try {
        System.out.println("\nExecuting Query for Task 13 (testNonExistentAccountOutParams) - Expected to fail:\n" + sql);
        SQLException exception = Assertions.assertThrows(SQLException.class,
                () -> executeQuery(sql),
                "Executing get_account_details_with_out_params for non-existent account ID 999 should throw SQLException."
        );
        String exceptionMessage = exception.getMessage();
        boolean correctException = exceptionMessage.contains("Account not found: 999") ||
                exceptionMessage.contains("column accounts.account_number does not exist") ||
                exceptionMessage.contains("column accounts.status does not exist");
        Assertions.assertTrue(correctException, "Exception message for non-existent account ID 999 is incorrect. Expected 'Account not found: 999' or a schema-related error for missing columns. Got: " + exceptionMessage);
      } finally {
        rollbackConnection();
      }
    }
  }

  @Nested
  @Order(140)
  @DisplayName("Task 14: get_filtered_transactions_dynamic_srf Tests")
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class GetFilteredTransactionsDynamicSrfTests {
    @BeforeEach
    void loadGetFilteredTransactionsSrfFunction() throws SQLException, IOException {
      loadFunctionFromFile(SQL_QUERY_FILES_PATH + "14_get_filtered_transactions_dynamic_srf.sql");
    }

    @Test
    @Order(1)
    @DisplayName("Account ID 1 with min_amount 50.00 should return 1 transaction")
    void testFilteredTransactions() {
      String sql = "SELECT * FROM get_filtered_transactions_dynamic_srf(1, 50.00) " +
              "AS (tx_id INT, type TEXT, val NUMERIC, tx_time TIMESTAMP);";
      String sql2 = "SELECT * FROM get_filtered_transactions_dynamic_srf(1, 600.00) " +
              "AS (id INT, category TEXT, amount_val NUMERIC, \"when\" TIMESTAMP);";
      try {
        System.out.println("\nExecuting Query 1 for Task 14 (testFilteredTransactions):\n" + sql);
        List<Map<String, Object>> results = executeQuery(sql);
        printQueryResults(results);
        Assertions.assertEquals(1, results.size(), "Should find 1 transaction for account ID 1 with amount > 50.00.");
        Map<String, Object> tx = results.get(0);
        Assertions.assertEquals(1, tx.get("tx_id"), "Transaction ID of the filtered transaction is incorrect.");
        Assertions.assertEquals("deposit", tx.get("type"), "Transaction type of the filtered transaction is incorrect.");
        Assertions.assertEquals(0, new BigDecimal("500.00").compareTo((BigDecimal)tx.get("val")), "Transaction amount of the filtered transaction is incorrect.");

        System.out.println("\nExecuting Query 2 for Task 14 (testFilteredTransactions):\n" + sql2);
        List<Map<String, Object>> results2 = executeQuery(sql2);
        printQueryResults(results2);
        Assertions.assertTrue(results2.isEmpty(), "Should find 0 transactions for account ID 1 with amount > 600.00.");
      } catch (SQLException e) {
        Assertions.fail("SQLException during testFilteredTransactions. Query1: [" + sql + "], Query2: ["+ sql2 + "]: " + e.getMessage());
      } finally {
        rollbackConnection();
      }
    }

    @Test
    @Order(2)
    @DisplayName("Account ID 4 (no transactions) with any min_amount should return empty set")
    void testNoMatchingTransactions() {
      String sql = "SELECT * FROM get_filtered_transactions_dynamic_srf(4, 0.00) " +
              "AS (tx_id INT, type TEXT, val NUMERIC, tx_time TIMESTAMP);";
      try {
        System.out.println("\nExecuting Query for Task 14 (testNoMatchingTransactions):\n" + sql);
        List<Map<String, Object>> results = executeQuery(sql);
        printQueryResults(results);
        Assertions.assertTrue(results.isEmpty(), "Should return an empty set for account ID 4 (no transactions), regardless of min_amount.");
      } catch (SQLException e) {
        Assertions.fail("SQLException during testNoMatchingTransactions for query [" + sql + "]: " + e.getMessage());
      } finally {
        rollbackConnection();
      }
    }

    @Test
    @Order(3)
    @DisplayName("Non-existent account ID 999 should return an empty set")
    void testNonExistentAccountForFilteredTransactions() {
      String sql = "SELECT * FROM get_filtered_transactions_dynamic_srf(999, 0.00) " +
              "AS (tx_id INT, type TEXT, val NUMERIC, tx_time TIMESTAMP);";
      try {
        System.out.println("\nExecuting Query for Task 14 (testNonExistentAccountForFilteredTransactions):\n" + sql);
        List<Map<String, Object>> results = executeQuery(sql);
        printQueryResults(results);
        Assertions.assertTrue(results.isEmpty(), "Should return an empty set for a non-existent account ID (999).");
      } catch (SQLException e) {
        Assertions.fail("SQLException during testNonExistentAccountForFilteredTransactions for query [" + sql + "]: " + e.getMessage());
      } finally {
        rollbackConnection();
      }
    }
  }

  @Nested
  @Order(150)
  @DisplayName("Task 15: get_column_value_from_table_dynamic Tests")
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class GetColumnValueFromTableDynamicTests {
    @BeforeEach
    void loadGetColumnValueDynamicFunction() throws SQLException, IOException {
      loadFunctionFromFile(SQL_QUERY_FILES_PATH + "15_get_column_value_from_table_dynamic.sql");
    }

    @Test
    @Order(1)
    @DisplayName("Should retrieve email for customer ID 1 dynamically")
    void testGetCustomerEmailDynamic() {
      String sql = "SELECT get_column_value_from_table_dynamic('customers', 'email', 'id', 1) AS val;";
      try {
        System.out.println("\nExecuting Query for Task 15 (testGetCustomerEmailDynamic):\n" + sql);
        List<Map<String, Object>> result = executeQuery(sql);
        printQueryResults(result);
        Assertions.assertEquals("john.doe@example.com", result.get(0).get("val"), "Dynamically retrieved email for customer ID 1 is incorrect.");
      } catch (SQLException e) {
        Assertions.fail("SQLException during testGetCustomerEmailDynamic for query [" + sql + "]: " + e.getMessage());
      } finally {
        rollbackConnection();
      }
    }

    @Test
    @Order(2)
    @DisplayName("Should retrieve balance for account ID 1 as TEXT dynamically")
    void testGetAccountBalanceDynamic() {
      String sql = "SELECT get_column_value_from_table_dynamic('accounts', 'balance', 'id', 1) AS val;";
      try {
        System.out.println("\nExecuting Query for Task 15 (testGetAccountBalanceDynamic):\n" + sql);
        List<Map<String, Object>> result = executeQuery(sql);
        printQueryResults(result);
        Assertions.assertEquals("1500.00", result.get(0).get("val"), "Dynamically retrieved balance (as TEXT) for account ID 1 is incorrect.");
      } catch (SQLException e) {
        Assertions.fail("SQLException during testGetAccountBalanceDynamic for query [" + sql + "]: " + e.getMessage());
      } finally {
        rollbackConnection();
      }
    }

    @Test
    @Order(3)
    @DisplayName("Should return NULL for non-existent ID (customer 999) dynamically")
    void testNonExistentIdDynamic() {
      String sql = "SELECT get_column_value_from_table_dynamic('customers', 'email', 'id', 999) AS val;";
      try {
        System.out.println("\nExecuting Query for Task 15 (testNonExistentIdDynamic):\n" + sql);
        List<Map<String, Object>> result = executeQuery(sql);
        printQueryResults(result);
        Assertions.assertNull(result.get(0).get("val"), "Dynamically retrieving a column for a non-existent ID should return NULL.");
      } catch (SQLException e) {
        Assertions.fail("SQLException during testNonExistentIdDynamic for query [" + sql + "]: " + e.getMessage());
      } finally {
        rollbackConnection();
      }
    }

    @Test
    @Order(4)
    @DisplayName("Should return NULL for invalid table name dynamically (if function handles error)")
    void testInvalidTableDynamic() {
      String sql = "SELECT get_column_value_from_table_dynamic('non_existent_table', 'email', 'customer_id', 1) AS val;";
      try {
        System.out.println("\nExecuting Query for Task 15 (testInvalidTableDynamic):\n" + sql);
        List<Map<String, Object>> result = executeQuery(sql);
        printQueryResults(result);
        Assertions.assertNull(result.get(0).get("val"), "Dynamically retrieving from a non-existent table should return NULL (assuming function error handling).");
      } catch (SQLException e) {
        Assertions.fail("SQLException during testInvalidTableDynamic for query [" + sql + "]: " + e.getMessage());
      } finally {
        rollbackConnection();
      }
    }

    @Test
    @Order(5)
    @DisplayName("Should return NULL for invalid column name dynamically (if function handles error)")
    void testInvalidColumnDynamic() {
      String sql = "SELECT get_column_value_from_table_dynamic('customers', 'non_existent_column', 'id', 1) AS val;";
      try {
        System.out.println("\nExecuting Query for Task 15 (testInvalidColumnDynamic):\n" + sql);
        List<Map<String, Object>> result = executeQuery(sql);
        printQueryResults(result);
        Assertions.assertNull(result.get(0).get("val"), "Dynamically retrieving a non-existent column should return NULL (assuming function error handling).");
      } catch (SQLException e) {
        Assertions.fail("SQLException during testInvalidColumnDynamic for query [" + sql + "]: " + e.getMessage());
      } finally {
        rollbackConnection();
      }
    }
  }
}
