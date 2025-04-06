package org.codeus.database.fundamentals.data_analytics; // Or your preferred package

import org.codeus.database.common.EmbeddedPostgreSqlSetup;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SqlWindowFunctionTests extends EmbeddedPostgreSqlSetup {

    private static final String QUERIES_DIR = "queries/";

    private static final String TASK_01_QUERY = QUERIES_DIR + "01_ranking/01_row_number_account_1001.sql";
    private static final String TASK_02_QUERY = QUERIES_DIR + "01_ranking/02_rank_customers_by_accounts.sql";
    private static final String TASK_03_QUERY = QUERIES_DIR + "01_ranking/03_dense_rank_customers_by_accounts.sql";
    private static final String TASK_04_QUERY = QUERIES_DIR + "01_ranking/04_rank_accounts_by_balance_in_branch.sql";
    private static final String TASK_05_QUERY = QUERIES_DIR + "01_ranking/05_top3_transactions_per_account.sql";
    private static final String TASK_06_QUERY = QUERIES_DIR + "02_offset/06_lead_next_transaction_amount.sql";
    private static final String TASK_07_QUERY = QUERIES_DIR + "02_offset/07_lag_previous_transaction_amount.sql";
    private static final String TASK_08_QUERY = QUERIES_DIR + "02_offset/08_lag_days_between_transactions.sql";
    private static final String TASK_09_QUERY = QUERIES_DIR + "02_offset/09_lag_fraud_detection_double_amount.sql";
    private static final String TASK_10_QUERY = QUERIES_DIR + "02_offset/10_lead_next_account_open_date.sql";
    private static final String TASK_11_QUERY = QUERIES_DIR + "03_aggregate_frames/11_sum_running_total_balance.sql";
    private static final String TASK_12_QUERY = QUERIES_DIR + "03_aggregate_frames/12_sum_cumulative_deposits.sql";
    private static final String TASK_13_QUERY = QUERIES_DIR + "03_aggregate_frames/13_avg_moving_average_3txn.sql";
    private static final String TASK_14_QUERY = QUERIES_DIR + "03_aggregate_frames/14_sum_total_daily_amount_per_account.sql";
    private static final String TASK_15_QUERY = QUERIES_DIR + "03_aggregate_frames/15_avg_difference_from_account_avg.sql";
    private static final String TASK_16_QUERY = QUERIES_DIR + "04_advanced/16_ntile_account_balance_quartiles.sql";
    private static final String TASK_17_QUERY = QUERIES_DIR + "04_advanced/17_first_value_highest_balance_in_branch.sql";
    private static final String TASK_18_QUERY = QUERIES_DIR + "04_advanced/18_lag_amount_2_txns_ago.sql";
    private static final String TASK_19_QUERY = QUERIES_DIR + "04_advanced/19_rank_customers_by_total_balance_in_state.sql";
    private static final String TASK_20_QUERY = QUERIES_DIR + "04_advanced/20_lag_lead_fraud_detection_deposit_withdrawal_time.sql";


    @Test
    @Order(1)
    @DisplayName("Task 1: ROW_NUMBER() on account 1001")
    void testTask1RowNumber() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile(TASK_01_QUERY);
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(10, results.size());
        assertTrue(results.get(0).containsKey("rn"));

        // Verify transaction IDs are in expected order first (sanity check)
        int[] expectedTxnIds = {1, 2, 3, 4, 5, 6, 7, 8, 27, 9};
        for (int i = 0; i < results.size(); i++) {
            assertEquals(expectedTxnIds[i], results.get(i).get("transaction_id"), "Transaction ID mismatch at index " + i);
        }

        // Verify all row numbers are sequential
        for (int i = 0; i < results.size(); i++) {
            assertEquals(i + 1, (Long) results.get(i).get("rn"), "Row number mismatch at index " + i);
        }
    }

    @Test
    @Order(2)
    @DisplayName("Task 2: Rank customers by accounts")
    void testTask2RankCustomers() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile(TASK_02_QUERY);
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(6, results.size());
        assertTrue(results.get(0).containsKey("customer_rank"));
        assertTrue(results.get(0).containsKey("account_count"));

        // Sort results by rank, then customer_id for consistent checking of ties
        results.sort(Comparator.<Map<String, Object>, Long>comparing(m -> (Long) m.get("customer_rank"))
                .thenComparingInt(m -> (Integer) m.get("customer_id")));

        // Rank 1 (2 accounts) - Cust 101, 103, 105
        assertEquals(1L, results.get(0).get("customer_rank"));
        assertEquals(2L, results.get(0).get("account_count"));
        assertEquals(101, results.get(0).get("customer_id"));
        assertEquals(1L, results.get(1).get("customer_rank"));
        assertEquals(2L, results.get(1).get("account_count"));
        assertEquals(103, results.get(1).get("customer_id"));
        assertEquals(1L, results.get(2).get("customer_rank"));
        assertEquals(2L, results.get(2).get("account_count"));
        assertEquals(105, results.get(2).get("customer_id"));

        // Rank 4 (1 account) - Cust 102, 104, 106
        assertEquals(4L, results.get(3).get("customer_rank"));
        assertEquals(1L, results.get(3).get("account_count"));
        assertEquals(102, results.get(3).get("customer_id"));
        assertEquals(4L, results.get(4).get("customer_rank"));
        assertEquals(1L, results.get(4).get("account_count"));
        assertEquals(104, results.get(4).get("customer_id"));
        assertEquals(4L, results.get(5).get("customer_rank"));
        assertEquals(1L, results.get(5).get("account_count"));
        assertEquals(106, results.get(5).get("customer_id"));
    }

    @Test
    @Order(3)
    @DisplayName("Task 3: Dense Rank customers by accounts")
    void testTask3DenseRankCustomers() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile(TASK_03_QUERY);
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(6, results.size());
        assertTrue(results.get(0).containsKey("customer_dense_rank"));
        assertTrue(results.get(0).containsKey("account_count"));

        // Sort results by rank, then customer_id for consistent checking of ties
        results.sort(Comparator.<Map<String, Object>, Long>comparing(m -> (Long) m.get("customer_dense_rank"))
                .thenComparingInt(m -> (Integer) m.get("customer_id")));

        // Rank 1 (2 accounts) - Cust 101, 103, 105
        assertEquals(1L, results.get(0).get("customer_dense_rank"));
        assertEquals(2L, results.get(0).get("account_count"));
        assertEquals(101, results.get(0).get("customer_id"));
        assertEquals(1L, results.get(1).get("customer_dense_rank"));
        assertEquals(2L, results.get(1).get("account_count"));
        assertEquals(103, results.get(1).get("customer_id"));
        assertEquals(1L, results.get(2).get("customer_dense_rank"));
        assertEquals(2L, results.get(2).get("account_count"));
        assertEquals(105, results.get(2).get("customer_id"));

        // Rank 2 (1 account) - Cust 102, 104, 106
        assertEquals(2L, results.get(3).get("customer_dense_rank"));
        assertEquals(1L, results.get(3).get("account_count"));
        assertEquals(102, results.get(3).get("customer_id"));
        assertEquals(2L, results.get(4).get("customer_dense_rank"));
        assertEquals(1L, results.get(4).get("account_count"));
        assertEquals(104, results.get(4).get("customer_id"));
        assertEquals(2L, results.get(5).get("customer_dense_rank"));
        assertEquals(1L, results.get(5).get("account_count"));
        assertEquals(106, results.get(5).get("customer_id"));
    }

    @Test
    @Order(4)
    @DisplayName("Task 4: Rank accounts by balance in branch")
    void testTask4RankAccountsInBranch() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile(TASK_04_QUERY);
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(9, results.size());
        assertTrue(results.get(0).containsKey("rank_in_branch"));
        Map<Integer, List<Map<String, Object>>> resultsByBranch = results.stream().collect(Collectors.groupingBy(m -> (Integer) m.get("branch_id")));
        // Branch 1: 1002 (15k) R1, 1004 (8k) R2, 1001 (5k) R3
        List<Map<String, Object>> branch1 = resultsByBranch.get(1);
        assertNotNull(branch1);
        branch1.sort(Comparator.comparingLong(m -> (Long) m.get("rank_in_branch")));
        assertEquals(3, branch1.size());
        assertEquals(1002, branch1.get(0).get("account_id"));
        assertEquals(1L, branch1.get(0).get("rank_in_branch"));
        assertEquals(0, new BigDecimal("15000.00").compareTo((BigDecimal) branch1.get(0).get("balance")));
        assertEquals(1004, branch1.get(1).get("account_id"));
        assertEquals(2L, branch1.get(1).get("rank_in_branch"));
        assertEquals(0, new BigDecimal("8000.00").compareTo((BigDecimal) branch1.get(1).get("balance")));
        assertEquals(1001, branch1.get(2).get("account_id"));
        assertEquals(3L, branch1.get(2).get("rank_in_branch"));
        assertEquals(0, new BigDecimal("5000.00").compareTo((BigDecimal) branch1.get(2).get("balance")));
        // Branch 2: 1003 (2.5k) R1, 1006 (1.2k) R2
        List<Map<String, Object>> branch2 = resultsByBranch.get(2);
        assertNotNull(branch2);
        branch2.sort(Comparator.comparingLong(m -> (Long) m.get("rank_in_branch")));
        assertEquals(2, branch2.size());
        assertEquals(1003, branch2.get(0).get("account_id"));
        assertEquals(1L, branch2.get(0).get("rank_in_branch"));
        assertEquals(0, new BigDecimal("2500.00").compareTo((BigDecimal) branch2.get(0).get("balance")));
        assertEquals(1006, branch2.get(1).get("account_id"));
        assertEquals(2L, branch2.get(1).get("rank_in_branch"));
        assertEquals(0, new BigDecimal("1200.00").compareTo((BigDecimal) branch2.get(1).get("balance")));
        // Branch 3: 1005 (25k) R1, 1009 (4.5k) R2
        List<Map<String, Object>> branch3 = resultsByBranch.get(3);
        assertNotNull(branch3);
        branch3.sort(Comparator.comparingLong(m -> (Long) m.get("rank_in_branch")));
        assertEquals(2, branch3.size());
        assertEquals(1005, branch3.get(0).get("account_id"));
        assertEquals(1L, branch3.get(0).get("rank_in_branch"));
        assertEquals(0, new BigDecimal("25000.00").compareTo((BigDecimal) branch3.get(0).get("balance")));
        assertEquals(1009, branch3.get(1).get("account_id"));
        assertEquals(2L, branch3.get(1).get("rank_in_branch"));
        assertEquals(0, new BigDecimal("4500.00").compareTo((BigDecimal) branch3.get(1).get("balance")));
        // Branch 4: 1008 (7k) R1, 1007 (3k) R2
        List<Map<String, Object>> branch4 = resultsByBranch.get(4);
        assertNotNull(branch4);
        branch4.sort(Comparator.comparingLong(m -> (Long) m.get("rank_in_branch")));
        assertEquals(2, branch4.size());
        assertEquals(1008, branch4.get(0).get("account_id"));
        assertEquals(1L, branch4.get(0).get("rank_in_branch"));
        assertEquals(0, new BigDecimal("7000.00").compareTo((BigDecimal) branch4.get(0).get("balance")));
        assertEquals(1007, branch4.get(1).get("account_id"));
        assertEquals(2L, branch4.get(1).get("rank_in_branch"));
        assertEquals(0, new BigDecimal("3000.00").compareTo((BigDecimal) branch4.get(1).get("balance")));
    }

    @Test
    @Order(5)
    @DisplayName("Task 5: Top 3 highest transactions per account")
    void testTask5Top3Transactions() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile(TASK_05_QUERY);
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertTrue(results.get(0).containsKey("transaction_rank"));
        Map<Integer, List<Map<String, Object>>> resultsByAccount = results.stream().collect(Collectors.groupingBy(m -> (Integer) m.get("account_id")));

        // Account 1001: Expecting Txns 1, 4, 7 (all rank 1, amount 1000)
        List<Map<String, Object>> acc1001 = resultsByAccount.get(1001);
        assertNotNull(acc1001);
        assertEquals(3, acc1001.size());
        assertTrue(acc1001.stream().allMatch(m -> m.get("transaction_rank").equals(1L) && ((BigDecimal) m.get("amount")).compareTo(new BigDecimal("1000.00")) == 0));
        assertTrue(acc1001.stream().map(m -> (Integer) m.get("transaction_id")).collect(Collectors.toSet()).containsAll(List.of(1, 4, 7)));

        // Account 1003: Expecting Txns 17 (R1, 1750), 13 (R2, 1500), 15 (R2, 1500)
        List<Map<String, Object>> acc1003 = resultsByAccount.get(1003);
        assertNotNull(acc1003);
        assertEquals(3, acc1003.size());
        assertTrue(acc1003.stream().map(m -> (Integer) m.get("transaction_id")).collect(Collectors.toSet()).containsAll(List.of(17, 13, 15)));
        assertTrue(acc1003.stream().anyMatch(m -> m.get("transaction_id").equals(17) && m.get("transaction_rank").equals(1L) && ((BigDecimal) m.get("amount")).compareTo(new BigDecimal("1750.00")) == 0));
        assertTrue(acc1003.stream().anyMatch(m -> m.get("transaction_id").equals(13) && m.get("transaction_rank").equals(2L) && ((BigDecimal) m.get("amount")).compareTo(new BigDecimal("1500.00")) == 0));
        assertTrue(acc1003.stream().anyMatch(m -> m.get("transaction_id").equals(15) && m.get("transaction_rank").equals(2L) && ((BigDecimal) m.get("amount")).compareTo(new BigDecimal("1500.00")) == 0));

        // Account 1005: Expecting Txns 26 (R1, 1500), 25 (R2, 1000)
        List<Map<String, Object>> acc1005 = resultsByAccount.get(1005);
        assertNotNull(acc1005);
        assertEquals(2, acc1005.size());
        assertTrue(acc1005.stream().anyMatch(m -> m.get("transaction_id").equals(26) && m.get("transaction_rank").equals(1L) && ((BigDecimal) m.get("amount")).compareTo(new BigDecimal("1500.00")) == 0));
        assertTrue(acc1005.stream().anyMatch(m -> m.get("transaction_id").equals(25) && m.get("transaction_rank").equals(2L) && ((BigDecimal) m.get("amount")).compareTo(new BigDecimal("1000.00")) == 0));

        // Account 1009: Txn 36 (R1, 1300), Txn 38 (R2, 1250), Txn 34 (R3, 1200)
        List<Map<String, Object>> acc1009 = resultsByAccount.get(1009);
        assertNotNull(acc1009);
        assertEquals(3, acc1009.size());
        assertTrue(acc1009.stream().anyMatch(m -> m.get("transaction_id").equals(36) && m.get("transaction_rank").equals(1L) && ((BigDecimal) m.get("amount")).compareTo(new BigDecimal("1300.00")) == 0));
        assertTrue(acc1009.stream().anyMatch(m -> m.get("transaction_id").equals(38) && m.get("transaction_rank").equals(2L) && ((BigDecimal) m.get("amount")).compareTo(new BigDecimal("1250.00")) == 0));
        assertTrue(acc1009.stream().anyMatch(m -> m.get("transaction_id").equals(34) && m.get("transaction_rank").equals(3L) && ((BigDecimal) m.get("amount")).compareTo(new BigDecimal("1200.00")) == 0));
    }

    @Test
    @Order(6)
    @DisplayName("Task 6: LEAD next transaction amount")
    void testTask6LeadAmount() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile(TASK_06_QUERY);
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(10, results.size());
        assertTrue(results.get(0).containsKey("next_transaction_amount"));

        // Expected values for next_transaction_amount in order
        BigDecimal[] expectedNextAmounts = {
                new BigDecimal("-200.00"), new BigDecimal("-50.00"), new BigDecimal("1000.00"), new BigDecimal("-300.00"),
                new BigDecimal("-150.00"), new BigDecimal("1000.00"), new BigDecimal("-250.00"), new BigDecimal("-250.00"),
                new BigDecimal("-75.00"), null
        };
        int[] expectedTxnIds = {1, 2, 3, 4, 5, 6, 7, 8, 27, 9}; // Verify order

        assertEquals(expectedTxnIds.length, results.size()); // Double check size

        for (int i = 0; i < results.size(); i++) {
            assertEquals(expectedTxnIds[i], results.get(i).get("transaction_id"), "Txn ID mismatch at index " + i);
            BigDecimal expected = expectedNextAmounts[i];
            Object actualObject = results.get(i).get("next_transaction_amount");
            if (expected == null) {
                assertNull(actualObject, "Expected null next_amount at index " + i);
            } else {
                assertNotNull(actualObject, "Expected non-null next_amount at index " + i);
                assertTrue(actualObject instanceof BigDecimal, "Expected BigDecimal type at index " + i);
                assertEquals(0, expected.compareTo((BigDecimal) actualObject), "Next amount mismatch at index " + i);
            }
        }
    }


    @Test
    @Order(7)
    @DisplayName("Task 7: LAG() on account 1001 with default")
    void testTask7LagWithDefault() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile(TASK_07_QUERY);
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(10, results.size());
        assertTrue(results.get(0).containsKey("previous_transaction_amount"));

        // Expected values for previous_transaction_amount in order
        BigDecimal[] expectedPrevAmounts = {
                new BigDecimal("0.00"), // Default for txn 1
                new BigDecimal("1000.00"),// for txn 2
                new BigDecimal("-200.00"),// for txn 3
                new BigDecimal("-50.00"), // for txn 4
                new BigDecimal("1000.00"),// for txn 5
                new BigDecimal("-300.00"),// for txn 6
                new BigDecimal("-150.00"),// for txn 7
                new BigDecimal("1000.00"),// for txn 8
                new BigDecimal("-250.00"),// for txn 27
                new BigDecimal("-250.00") // for txn 9
        };
        int[] expectedTxnIds = {1, 2, 3, 4, 5, 6, 7, 8, 27, 9}; // Verify order

        assertEquals(expectedTxnIds.length, results.size()); // Double check size

        for (int i = 0; i < results.size(); i++) {
            assertEquals(expectedTxnIds[i], results.get(i).get("transaction_id"), "Txn ID mismatch at index " + i);
            BigDecimal expected = expectedPrevAmounts[i];
            Object actualObject = results.get(i).get("previous_transaction_amount");
            assertNotNull(actualObject, "Expected non-null prev_amount at index " + i); // LAG has default
            assertTrue(actualObject instanceof BigDecimal, "Expected BigDecimal type at index " + i);
            assertEquals(0, expected.compareTo((BigDecimal) actualObject), "Prev amount mismatch at index " + i);
        }
    }

    @Test
    @Order(8)
    @DisplayName("Task 8: LAG days between transactions")
    void testTask8LagDaysBetween() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile(TASK_08_QUERY); // Account 1003
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(6, results.size());
        assertTrue(results.get(0).containsKey("days_since_last_transaction"));

        // Expected values for days_since_last_transaction in order
        // Corrected value at index 4 from 11 to 12
        Integer[] expectedDays = {null, 17, 14, 17, 12, 17}; // Null for first, then calculated differences
        int[] expectedTxnIds = {13, 14, 15, 16, 17, 18}; // Verify order

        assertEquals(expectedTxnIds.length, results.size()); // Double check size

        for (int i = 0; i < results.size(); i++) {
            assertEquals(expectedTxnIds[i], results.get(i).get("transaction_id"), "Txn ID mismatch at index " + i);
            Integer expected = expectedDays[i];
            Object actualObject = results.get(i).get("days_since_last_transaction");
            if (expected == null) {
                assertNull(actualObject, "Expected null days_since_last at index " + i);
            } else {
                assertNotNull(actualObject, "Expected non-null days_since_last at index " + i);
                // PostgreSQL date difference returns Integer
                assertTrue(actualObject instanceof Integer, "Expected Integer type at index " + i);
                assertEquals(expected, (Integer) actualObject, "Days since last mismatch at index " + i);
            }
        }
    }

    @Test
    @Order(9)
    @DisplayName("Task 9: LAG fraud detection double amount")
    void testTask9FraudDoubleAmount() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile(TASK_09_QUERY);
        assertNotNull(results);
        assertTrue(results.isEmpty(), "Expected 0 rows for fraud detection based on current seed data and condition > prev*2");
    }

    @Test
    @Order(10)
    @DisplayName("Task 10: LEAD next account open date")
    void testTask10LeadOpenDate() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile(TASK_10_QUERY);
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(9, results.size());
        assertTrue(results.get(0).containsKey("next_account_open_date"));

        // Verify specific cases more thoroughly
        // Use HashMap as Map.of() doesn't allow null values
        Map<Integer, Object> expectedNextDate = new HashMap<>();
        expectedNextDate.put(1001, Date.valueOf("2022-01-15")); // -> 1002
        expectedNextDate.put(1002, null);                      // Last for Cust 101
        expectedNextDate.put(1003, null);                      // Only one for Cust 102
        expectedNextDate.put(1004, Date.valueOf("2022-07-01")); // -> 1005
        expectedNextDate.put(1005, null);                      // Last for Cust 103
        expectedNextDate.put(1006, null);                      // Only one for Cust 104
        expectedNextDate.put(1007, Date.valueOf("2023-03-15")); // -> 1008
        expectedNextDate.put(1008, null);                      // Last for Cust 105
        expectedNextDate.put(1009, null);                       // Only one for Cust 106


        for (Map<String, Object> row : results) {
            int accId = (Integer) row.get("account_id");
            assertEquals(expectedNextDate.get(accId), row.get("next_account_open_date"), "Next open date mismatch for account " + accId);
        }
    }


    @Test
    @Order(11)
    @DisplayName("Task 11: Running Total for account 1001")
    void testTask11RunningTotal() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile(TASK_11_QUERY);
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(10, results.size());
        assertTrue(results.get(0).containsKey("running_total_balance"));

        // Expected running totals in order
        BigDecimal[] expectedTotals = {
                new BigDecimal("1000.00"), new BigDecimal("800.00"), new BigDecimal("750.00"), new BigDecimal("1750.00"),
                new BigDecimal("1450.00"), new BigDecimal("1300.00"), new BigDecimal("2300.00"), new BigDecimal("2050.00"),
                new BigDecimal("1800.00"), new BigDecimal("1725.00")
        };
        int[] expectedTxnIds = {1, 2, 3, 4, 5, 6, 7, 8, 27, 9}; // Verify order

        assertEquals(expectedTxnIds.length, results.size()); // Double check size

        for (int i = 0; i < results.size(); i++) {
            assertEquals(expectedTxnIds[i], results.get(i).get("transaction_id"), "Txn ID mismatch at index " + i);
            BigDecimal expected = expectedTotals[i];
            Object actualObject = results.get(i).get("running_total_balance");
            assertNotNull(actualObject, "Expected non-null running_total at index " + i);
            assertTrue(actualObject instanceof BigDecimal, "Expected BigDecimal type at index " + i);
            assertEquals(0, expected.compareTo((BigDecimal) actualObject), "Running total mismatch at index " + i);
        }
    }

    @Test
    @Order(12)
    @DisplayName("Task 12: Cumulative Deposits per account")
    void testTask12CumulativeDeposits() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile(TASK_12_QUERY);
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(21, results.size(), "Should return results for 21 deposit transactions");
        assertTrue(results.get(0).containsKey("cumulative_deposits"));

        Map<Integer, List<Map<String, Object>>> resultsByAccount = results.stream()
                .collect(Collectors.groupingBy(m -> (Integer) m.get("account_id")));

        // Verify final cumulative deposit for each account with deposits
        Map<Integer, BigDecimal> finalCumulative = Map.of(
                1001, new BigDecimal("3000.00"),
                1002, new BigDecimal("1500.00"),
                1003, new BigDecimal("4750.00"),
                1004, new BigDecimal("6000.00"),
                1005, new BigDecimal("2500.00"),
                1006, new BigDecimal("1300.00"), // Includes the added Txn 40
                1009, new BigDecimal("3750.00")
        );

        for (Map.Entry<Integer, List<Map<String, Object>>> entry : resultsByAccount.entrySet()) {
            int accountId = entry.getKey();
            List<Map<String, Object>> accountResults = entry.getValue();
            // Find the last transaction for this account in the results
            accountResults.sort(Comparator.comparing(m -> (Timestamp) m.get("transaction_date")));
            BigDecimal actualFinalCumulative = (BigDecimal) accountResults.get(accountResults.size() - 1).get("cumulative_deposits");
            BigDecimal expectedFinalCumulative = finalCumulative.get(accountId);

            assertNotNull(expectedFinalCumulative, "No expected final cumulative defined for account " + accountId);
            assertEquals(0, expectedFinalCumulative.compareTo(actualFinalCumulative),
                    "Final cumulative deposit mismatch for account " + accountId);
        }
    }


    @Test
    @Order(13)
    @DisplayName("Task 13: 3-Transaction Moving Average for account 1001")
    void testTask13MovingAverage() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile(TASK_13_QUERY);
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(10, results.size());
        assertTrue(results.get(0).containsKey("moving_average_3txn"));

        // Expected moving averages in order
        double[] expectedAvgs = {
                1000.00, 400.00, 250.00, 250.00, 216.67, 183.33, 183.33, 200.00, 166.67, -191.67
        };
        int[] expectedTxnIds = {1, 2, 3, 4, 5, 6, 7, 8, 27, 9}; // Verify order
        double delta = 0.01;

        assertEquals(expectedTxnIds.length, results.size()); // Double check size

        for (int i = 0; i < results.size(); i++) {
            assertEquals(expectedTxnIds[i], results.get(i).get("transaction_id"), "Txn ID mismatch at index " + i);
            double expected = expectedAvgs[i];
            Object actualObject = results.get(i).get("moving_average_3txn");
            assertNotNull(actualObject, "Expected non-null moving_avg at index " + i);
            assertTrue(actualObject instanceof Number, "Expected Number type at index " + i);
            assertEquals(expected, ((Number) actualObject).doubleValue(), delta, "Moving avg mismatch at index " + i);
        }
    }

    @Test
    @Order(14)
    @DisplayName("Task 14: Sum total daily amount per account")
    void testTask14SumDailyAmount() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile(TASK_14_QUERY);
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(41, results.size(), "Should return results for 41 transactions");
        assertTrue(results.get(0).containsKey("total_daily_amount_for_account"));

        // Verify specific daily totals
        Map<String, Object> txn8 = results.stream().filter(m -> m.get("transaction_id").equals(8)).findFirst().orElseThrow();
        assertEquals(0, new BigDecimal("-500.00").compareTo((BigDecimal) txn8.get("total_daily_amount_for_account")), "Daily total for txn 8");
        Map<String, Object> txn27 = results.stream().filter(m -> m.get("transaction_id").equals(27)).findFirst().orElseThrow();
        assertEquals(0, new BigDecimal("-500.00").compareTo((BigDecimal) txn27.get("total_daily_amount_for_account")), "Daily total for txn 27");
        Map<String, Object> txn1 = results.stream().filter(m -> m.get("transaction_id").equals(1)).findFirst().orElseThrow();
        assertEquals(0, new BigDecimal("1000.00").compareTo((BigDecimal) txn1.get("total_daily_amount_for_account")), "Daily total for txn 1");
        Map<String, Object> txn40 = results.stream().filter(m -> m.get("transaction_id").equals(40)).findFirst().orElseThrow();
        assertEquals(0, new BigDecimal("50.00").compareTo((BigDecimal) txn40.get("total_daily_amount_for_account")), "Daily total for txn 40");
        Map<String, Object> txn41 = results.stream().filter(m -> m.get("transaction_id").equals(41)).findFirst().orElseThrow();
        assertEquals(0, new BigDecimal("50.00").compareTo((BigDecimal) txn41.get("total_daily_amount_for_account")), "Daily total for txn 41");
        // Verify another single transaction day
        Map<String, Object> txn35 = results.stream().filter(m -> m.get("transaction_id").equals(35)).findFirst().orElseThrow(); // Acc 1009, 2024-01-20
        assertEquals(0, new BigDecimal("-300.00").compareTo((BigDecimal) txn35.get("total_daily_amount_for_account")), "Daily total for txn 35");
    }

    @Test
    @Order(15)
    @DisplayName("Task 15: Difference from account average")
    void testTask15DiffFromAvg() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile(TASK_15_QUERY);
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(41, results.size(), "Should return results for 41 transactions");
        assertTrue(results.get(0).containsKey("avg_account_transaction_amount"));
        assertTrue(results.get(0).containsKey("difference_from_avg"));
        double delta = 0.01;

        // Verify averages and differences for a few representative transactions per account
        // Account 1001 (Avg: 172.50)
        BigDecimal avg1001 = new BigDecimal("172.50");
        Map<String, Object> txn1 = results.stream().filter(m -> m.get("transaction_id").equals(1)).findFirst().orElseThrow();
        assertEquals(avg1001.doubleValue(), ((Number) txn1.get("avg_account_transaction_amount")).doubleValue(), delta);
        assertEquals(827.50, ((Number) txn1.get("difference_from_avg")).doubleValue(), delta);
        Map<String, Object> txn9 = results.stream().filter(m -> m.get("transaction_id").equals(9)).findFirst().orElseThrow(); // Last txn for 1001
        assertEquals(avg1001.doubleValue(), ((Number) txn9.get("avg_account_transaction_amount")).doubleValue(), delta);
        assertEquals(-247.50, ((Number) txn9.get("difference_from_avg")).doubleValue(), delta); // -75 - 172.50

        // Account 1003 (Avg: 591.67)
        BigDecimal avg1003 = new BigDecimal("591.6666666666666667"); // Use exact value if possible
        Map<String, Object> txn13 = results.stream().filter(m -> m.get("transaction_id").equals(13)).findFirst().orElseThrow();
        assertEquals(avg1003.doubleValue(), ((Number) txn13.get("avg_account_transaction_amount")).doubleValue(), delta);
        assertEquals(908.33, ((Number) txn13.get("difference_from_avg")).doubleValue(), delta); // 1500 - 591.67
        Map<String, Object> txn14 = results.stream().filter(m -> m.get("transaction_id").equals(14)).findFirst().orElseThrow();
        assertEquals(avg1003.doubleValue(), ((Number) txn14.get("avg_account_transaction_amount")).doubleValue(), delta);
        assertEquals(-991.67, ((Number) txn14.get("difference_from_avg")).doubleValue(), delta); // -400 - 591.67

        // Account 1006 (Avg: 115.00)
        BigDecimal avg1006 = new BigDecimal("115.00");
        Map<String, Object> txn40 = results.stream().filter(m -> m.get("transaction_id").equals(40)).findFirst().orElseThrow();
        assertEquals(avg1006.doubleValue(), ((Number) txn40.get("avg_account_transaction_amount")).doubleValue(), delta);
        assertEquals(85.00, ((Number) txn40.get("difference_from_avg")).doubleValue(), delta); // 200 - 115
        Map<String, Object> txn41 = results.stream().filter(m -> m.get("transaction_id").equals(41)).findFirst().orElseThrow();
        assertEquals(avg1006.doubleValue(), ((Number) txn41.get("avg_account_transaction_amount")).doubleValue(), delta);
        assertEquals(-265.00, ((Number) txn41.get("difference_from_avg")).doubleValue(), delta); // -150 - 115
    }

    @Test
    @Order(16)
    @DisplayName("Task 16: NTILE account balance quartiles")
    void testTask16NtileQuartiles() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile(TASK_16_QUERY);
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(9, results.size());
        assertTrue(results.get(0).containsKey("balance_quartile"));
        Map<Integer, Integer> expectedQuartiles = Map.of(1006, 1, 1003, 1, 1007, 1, 1009, 2, 1001, 2, 1008, 3, 1004, 3, 1002, 4, 1005, 4);
        for (Map<String, Object> row : results) {
            int accId = (Integer) row.get("account_id");
            assertEquals(expectedQuartiles.get(accId), row.get("balance_quartile"));
        }
    }

    @Test
    @Order(17)
    @DisplayName("Task 17: FIRST_VALUE highest balance in branch")
    void testTask17FirstValueHighestBalance() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile(TASK_17_QUERY);
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(4, results.size());
        assertTrue(results.get(0).containsKey("account_id_highest_balance"));
        assertTrue(results.get(0).containsKey("highest_balance"));
        results.sort(Comparator.comparingInt(m -> (Integer) m.get("branch_id")));
        assertEquals(1, results.get(0).get("branch_id"));
        assertEquals(1002, results.get(0).get("account_id_highest_balance"));
        assertEquals(0, new BigDecimal("15000.00").compareTo((BigDecimal) results.get(0).get("highest_balance")));
        assertEquals(2, results.get(1).get("branch_id"));
        assertEquals(1003, results.get(1).get("account_id_highest_balance"));
        assertEquals(0, new BigDecimal("2500.00").compareTo((BigDecimal) results.get(1).get("highest_balance")));
        assertEquals(3, results.get(2).get("branch_id"));
        assertEquals(1005, results.get(2).get("account_id_highest_balance"));
        assertEquals(0, new BigDecimal("25000.00").compareTo((BigDecimal) results.get(2).get("highest_balance")));
        assertEquals(4, results.get(3).get("branch_id"));
        assertEquals(1008, results.get(3).get("account_id_highest_balance"));
        assertEquals(0, new BigDecimal("7000.00").compareTo((BigDecimal) results.get(3).get("highest_balance")));
    }

    @Test
    @Order(18)
    @DisplayName("Task 18: LAG amount 2 txns ago")
    void testTask18Lag2TxnsAgo() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile(TASK_18_QUERY); // Account 1001
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(10, results.size());
        assertTrue(results.get(0).containsKey("amount_2_txns_ago"));

        // Expected values for amount_2_txns_ago in order
        BigDecimal[] expectedLag2Amounts = {
                null, null, new BigDecimal("1000.00"), new BigDecimal("-200.00"), new BigDecimal("-50.00"),
                new BigDecimal("1000.00"), new BigDecimal("-300.00"), new BigDecimal("-150.00"),
                new BigDecimal("1000.00"), new BigDecimal("-250.00")
        };
        int[] expectedTxnIds = {1, 2, 3, 4, 5, 6, 7, 8, 27, 9}; // Verify order

        assertEquals(expectedTxnIds.length, results.size()); // Double check size

        for (int i = 0; i < results.size(); i++) {
            assertEquals(expectedTxnIds[i], results.get(i).get("transaction_id"), "Txn ID mismatch at index " + i);
            BigDecimal expected = expectedLag2Amounts[i];
            Object actualObject = results.get(i).get("amount_2_txns_ago");
            if (expected == null) {
                assertNull(actualObject, "Expected null lag(2) amount at index " + i);
            } else {
                assertNotNull(actualObject, "Expected non-null lag(2) amount at index " + i);
                assertTrue(actualObject instanceof BigDecimal, "Expected BigDecimal type at index " + i);
                assertEquals(0, expected.compareTo((BigDecimal) actualObject), "Lag(2) amount mismatch at index " + i);
            }
        }
    }

    @Test
    @Order(19)
    @DisplayName("Task 19: Rank customers by total balance in state")
    void testTask19RankCustomersInState() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile(TASK_19_QUERY);

        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(7, results.size(), "Should return 7 rows (Cust 103 appears in 2 states)");
        assertTrue(results.get(0).containsKey("state"));
        assertTrue(results.get(0).containsKey("customer_id"));
        assertTrue(results.get(0).containsKey("total_balance"));
        assertTrue(results.get(0).containsKey("rank_in_state"));

        Map<String, List<Map<String, Object>>> resultsByState = results.stream().collect(Collectors.groupingBy(m -> (String) m.get("state")));

        // NY: 103 (33k) R1, 101 (20k) R2, 102 (2.5k) R3, 104 (1.2k) R4
        List<Map<String, Object>> ny = resultsByState.get("NY");
        assertNotNull(ny);
        ny.sort(Comparator.comparingLong(m -> (Long) m.get("rank_in_state")));
        assertEquals(4, ny.size());
        assertEquals(103, ny.get(0).get("customer_id"));
        assertEquals(1L, ny.get(0).get("rank_in_state"));
        assertEquals(0, new BigDecimal("33000.00").compareTo((BigDecimal) ny.get(0).get("total_balance")));
        assertEquals(101, ny.get(1).get("customer_id"));
        assertEquals(2L, ny.get(1).get("rank_in_state"));
        assertEquals(0, new BigDecimal("20000.00").compareTo((BigDecimal) ny.get(1).get("total_balance")));
        assertEquals(102, ny.get(2).get("customer_id"));
        assertEquals(3L, ny.get(2).get("rank_in_state"));
        assertEquals(0, new BigDecimal("2500.00").compareTo((BigDecimal) ny.get(2).get("total_balance")));
        assertEquals(104, ny.get(3).get("customer_id"));
        assertEquals(4L, ny.get(3).get("rank_in_state"));
        assertEquals(0, new BigDecimal("1200.00").compareTo((BigDecimal) ny.get(3).get("total_balance")));

        // CA: 103 (33k) R1, 106 (4.5k) R2
        List<Map<String, Object>> ca = resultsByState.get("CA");
        assertNotNull(ca);
        ca.sort(Comparator.comparingLong(m -> (Long) m.get("rank_in_state")));
        assertEquals(2, ca.size());
        assertEquals(103, ca.get(0).get("customer_id"));
        assertEquals(1L, ca.get(0).get("rank_in_state"));
        assertEquals(0, new BigDecimal("33000.00").compareTo((BigDecimal) ca.get(0).get("total_balance")));
        assertEquals(106, ca.get(1).get("customer_id"));
        assertEquals(2L, ca.get(1).get("rank_in_state"));
        assertEquals(0, new BigDecimal("4500.00").compareTo((BigDecimal) ca.get(1).get("total_balance")));

        List<Map<String, Object>> co = resultsByState.get("CO");
        assertNotNull(co);
        assertEquals(1, co.size());
        assertEquals(105, co.get(0).get("customer_id"));
        assertEquals(1L, co.get(0).get("rank_in_state"));
        assertEquals(0, new BigDecimal("10000.00").compareTo((BigDecimal) co.get(0).get("total_balance")));
    }

    @Test
    @Order(20)
    @DisplayName("Task 20: LAG/LEAD fraud detection deposit/withdrawal time")
    void testTask20FraudTime() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile(TASK_20_QUERY);
        assertNotNull(results);
        assertFalse(results.isEmpty(), "Expected 1 row based on updated seed data");
        assertEquals(1, results.size());
        Map<String, Object> result = results.get(0);
        assertTrue(result.containsKey("account_id"));
        assertTrue(result.containsKey("deposit_transaction_id"));
        assertTrue(result.containsKey("deposit_time"));
        assertTrue(result.containsKey("withdrawal_transaction_id"));
        assertTrue(result.containsKey("withdrawal_time"));
        assertEquals(1006, result.get("account_id"));
        assertEquals(40, result.get("deposit_transaction_id"));
        assertEquals(41, result.get("withdrawal_transaction_id"));
        Timestamp depositTime = (Timestamp) result.get("deposit_time");
        Timestamp withdrawalTime = (Timestamp) result.get("withdrawal_time");
        assertNotNull(depositTime);
        assertNotNull(withdrawalTime);
        assertTrue(withdrawalTime.after(depositTime));
    }

}
