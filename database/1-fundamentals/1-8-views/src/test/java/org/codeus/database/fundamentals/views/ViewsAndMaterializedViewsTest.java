package org.codeus.database.fundamentals.views;

import org.codeus.database.common.EmbeddedPostgreSqlSetup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ViewsAndMaterializedViewsTest extends EmbeddedPostgreSqlSetup {

    private static final String QUERIES_DIR = "views_and_materialized_views/";

    @Test
    @Order(1)
    @DisplayName("Task 1: Create a view for active loans with outstanding balances")
    void test01CreateViewActiveLoans() throws IOException, SQLException {
        // Execute the query file
        String filename = "01_1_create_view_active_loans.sql";
        executeQueriesFromFile(QUERIES_DIR + filename);

        // Check if the view exists
        boolean viewExists = checkViewExists("active_loans_view");

        List<Map<String, Object>> views = executeQuery(
                "SELECT viewname FROM pg_views WHERE viewname = 'active_loans_view'"
        );
        System.out.println("Views found: " + views);

        assertTrue(viewExists, "The active_loans_view view should exist");

        // Check the view structure and data
        List<Map<String, Object>> viewData = executeQuery("SELECT * FROM active_loans_view ORDER BY monthly_payment DESC");

        // The view should contain data
        assertFalse(viewData.isEmpty(), "The view should contain data");

        // Check if we have the expected columns
        Map<String, Object> firstRow = viewData.get(0);
        assertTrue(firstRow.containsKey("first_name"), "The view should contain the first_name column");
        assertTrue(firstRow.containsKey("last_name"), "The view should contain the last_name column");
        assertTrue(firstRow.containsKey("email"), "The view should contain the email column");
        assertTrue(firstRow.containsKey("amount"), "The view should contain the amount column");
        assertTrue(firstRow.containsKey("interest_rate"), "The view should contain the interest_rate column");
        assertTrue(firstRow.containsKey("term_months"), "The view should contain the term_months column");
        assertTrue(firstRow.containsKey("status"), "The view should contain the status column");
        assertTrue(firstRow.containsKey("monthly_payment"), "The view should contain the monthly_payment column");

        // Check that only active loans are in the view
        for (Map<String, Object> row : viewData) {
            assertEquals("active", row.get("status"), "Only active loans should be in the view");
        }

        // Check that monthly_payment calculation is correct for at least one record
        boolean paymentCalculationCorrect = false;
        for (Map<String, Object> row : viewData) {
            double amount = ((Number) row.get("amount")).doubleValue();
            double interestRate = ((Number) row.get("interest_rate")).doubleValue();
            int termMonths = ((Number) row.get("term_months")).intValue();
            double expectedMonthlyPayment = amount * (1 + interestRate / 100) / termMonths;
            double actualMonthlyPayment = ((Number) row.get("monthly_payment")).doubleValue();

            // Check with a small delta for floating point precision
            if (Math.abs(expectedMonthlyPayment - actualMonthlyPayment) < 0.01) {
                paymentCalculationCorrect = true;
                break;
            }
        }

        assertTrue(paymentCalculationCorrect, "Monthly payment calculation should be correct");
    }

    @Test
    @Order(2)
    @DisplayName("Task 2: Create a materialized view for daily transaction summaries")
    void test02CreateMaterializedViewTransactionSummary() throws IOException, SQLException {
        // Execute the query file
        String filename = "02_1_create_materialized_view_transaction_summary.sql";
        executeQueriesFromFile(QUERIES_DIR + filename);

        // Check if the materialized view exists
        boolean viewExists = checkMaterializedViewExists("transaction_daily_summary");
        assertTrue(viewExists, "The transaction_daily_summary materialized view should exist");

        // Check the materialized view structure and data
        List<Map<String, Object>> viewData = executeQuery("SELECT * FROM transaction_daily_summary ORDER BY transaction_date, transaction_type");

        // The materialized view should contain data
        assertFalse(viewData.isEmpty(), "The materialized view should contain data");

        // Check if we have the expected columns
        Map<String, Object> firstRow = viewData.get(0);
        assertTrue(firstRow.containsKey("transaction_date"), "The view should contain the transaction_date column");
        assertTrue(firstRow.containsKey("transaction_type"), "The view should contain the transaction_type column");
        assertTrue(firstRow.containsKey("transaction_count"), "The view should contain the transaction_count column");
        assertTrue(firstRow.containsKey("total_amount"), "The view should contain the total_amount column");
        assertTrue(firstRow.containsKey("average_amount"), "The view should contain the average_amount column");

        // Verify that the sum of counts matches the number of transactions in the source table
        int totalTransactionsInView = 0;
        for (Map<String, Object> row : viewData) {
            totalTransactionsInView += ((Number) row.get("transaction_count")).intValue();
        }

        List<Map<String, Object>> transactionsCount = executeQuery("SELECT COUNT(*) as count FROM transactions");
        int actualTotalTransactions = ((Number) transactionsCount.get(0).get("count")).intValue();

        assertEquals(actualTotalTransactions, totalTransactionsInView, "Total transaction count in the materialized view should match the source table");
    }

    @Test
    @Order(3)
    @DisplayName("Task 3: Refresh and analyze performance improvements of materialized views")
    void test03RefreshAnalyzeMaterializedView() throws IOException, SQLException {

        String filename = QUERIES_DIR + "03_1_refresh_analyze_materialized_view.sql";
        String sqlText = Files.readString(Paths.get(getResourcePath(filename)));
        boolean containsRefresh = sqlText
                .toLowerCase()
                .contains("refresh materialized view transaction_daily_summary");

        assertTrue(containsRefresh,
                "Your SQL file is missing the required REFRESH MATERIALIZED VIEW transaction_daily_summary; " +
                        "Please add it to make sure the materialized view is updated with new data.");

        // Step 1: Execute student's SQL file
        executeQueriesFromFile(filename);

        // Step 2: Validate transactions were inserted today
        List<Map<String, Object>> inserted = executeQuery("""
    SELECT * FROM transactions
    WHERE transaction_date::date = CURRENT_DATE
    AND amount IN (750.00, 250.00)
""");
        assertEquals(2, inserted.size(), "Expected 2 inserted transactions");

        // Step 3: Check if the view includes the new transactions → proves refresh happened
        List<Map<String, Object>> viewAfter = executeQuery("""
    SELECT * FROM transaction_daily_summary
    WHERE transaction_date = CURRENT_DATE
""");

        boolean foundDeposit = viewAfter.stream()
                .anyMatch(row -> "deposit".equalsIgnoreCase((String) row.get("transaction_type")) &&
                        ((Number) row.get("total_amount")).doubleValue() >= 750.00);

        boolean foundWithdrawal = viewAfter.stream()
                .anyMatch(row -> "withdrawal".equalsIgnoreCase((String) row.get("transaction_type")) &&
                        ((Number) row.get("total_amount")).doubleValue() >= 250.00);

        assertTrue(foundDeposit, "Expected deposit (750.00) not found — did the student refresh?");
        assertTrue(foundWithdrawal, "Expected withdrawal (250.00) not found — did the student refresh?");
    }

    @Test
    @Order(4)
    @DisplayName("Task 4: Implement an incremental refresh strategy for transaction data marts")
    void test04IncrementalRefreshStrategy() throws IOException, SQLException {
        executeQueriesFromFile(QUERIES_DIR + "04_1_incremental_refresh_strategy.sql");
        boolean isMaterializedView = checkMaterializedViewExists("transaction_daily_summary_advanced");

        if (!isMaterializedView) {
            boolean isTable = checkTableExists("transaction_daily_summary_advanced");
            if (isTable) {
                fail("Found a TABLE named 'transaction_daily_summary_advanced'. You must create a MATERIALIZED VIEW instead.");
            } else {
                fail("Expected a MATERIALIZED VIEW named 'transaction_daily_summary_advanced', but none was found.");
            }
        }

        assertTrue(isMaterializedView, "The materialized view 'transaction_daily_summary_advanced' should exist");

        List<Map<String, Object>> viewData = executeQuery(
                "SELECT * FROM transaction_daily_summary_advanced WHERE transaction_date = CURRENT_DATE ORDER BY transaction_type"
        );

        assertFalse(viewData.isEmpty(), "Expected the materialized view to include today's transactions after refresh");

        boolean hasDeposit = viewData.stream().anyMatch(row ->
                "deposit".equals(row.get("transaction_type")) && ((Number) row.get("total_amount")).doubleValue() >= 1200);
        boolean hasWithdrawal = viewData.stream().anyMatch(row ->
                "withdrawal".equals(row.get("transaction_type")) && ((Number) row.get("total_amount")).doubleValue() >= 800);

        assertTrue(hasDeposit, "Expected a deposit transaction of at least 1200 in the materialized view");
        assertTrue(hasWithdrawal, "Expected a withdrawal transaction of at least 800 in the materialized view");
    }

    private boolean checkViewExists(String viewName) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT EXISTS (SELECT 1 FROM pg_views WHERE viewname = ?)"
        )) {
            stmt.setString(1, viewName.toLowerCase());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean(1);
                }
            }
        }
        return false;
    }

    // Helper method to check if a materialized view exists
    private boolean checkMaterializedViewExists(String viewName) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT EXISTS (SELECT 1 FROM pg_matviews WHERE matviewname = ?)"
        )) {
            stmt.setString(1, viewName.toLowerCase());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean(1);
                }
            }
        }
        return false;
    }

   private boolean checkTableExists(String tableName) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_schema = 'public' AND table_name = ?)")
        ) {
            stmt.setString(1, tableName.toLowerCase());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
        }
    }
}