package org.codeus.fundamentals.advanced_join_and_subqueries;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for performance comparison between EXISTS, IN, and JOIN operations.
 */
public class PerformanceComparisonTest extends BaseAdvancedJoinTest {

    @Test
    @Order(9)
    @DisplayName("Task 6: EXISTS vs IN vs JOIN - Performance Comparison with Timing")
    void test06PerformanceComparison() throws IOException, SQLException {
        BusinessDataValidator validator = new BusinessDataValidator(connection);
        Set<Integer> expectedCustomers = validator.getCustomersWithHighValueTransactions(500);

        System.out.println("=== PERFORMANCE COMPARISON WITH TIMING ANALYSIS ===");
        System.out.println("Expected customers with transactions >= 500: " + expectedCustomers);

        // Performance metrics storage
        Map<String, Long> executionTimes = new HashMap<>();
        Map<String, List<Map<String, Object>>> results = new HashMap<>();

        // Test 1: EXISTS Method with timing
        System.out.println("\n--- Testing EXISTS Method with Performance Timing ---");
        long startTime = System.nanoTime();
        List<Map<String, Object>> existsResults = executeQueryFromFile("06_1_performance_comparison_analysis.sql");
        long existsTime = System.nanoTime() - startTime;

        executionTimes.put("EXISTS", existsTime);
        results.put("EXISTS", existsResults);
        Set<Integer> existsCustomers = extractCustomerIds(existsResults);

        System.out.println("✅ EXISTS found customers: " + existsCustomers);
        System.out.println("⏱️  EXISTS execution time: " + formatTime(existsTime));

        // Test 2: IN Method with timing
        System.out.println("\n--- Testing IN Method with Performance Timing ---");
        startTime = System.nanoTime();
        List<Map<String, Object>> inResults = executeQueryFromFile("06_2_performance_comparison_in.sql");
        long inTime = System.nanoTime() - startTime;

        executionTimes.put("IN", inTime);
        results.put("IN", inResults);
        Set<Integer> inCustomers = extractCustomerIds(inResults);

        System.out.println("✅ IN found customers: " + inCustomers);
        System.out.println("⏱️  IN execution time: " + formatTime(inTime));

        // Test 3: JOIN Method with timing
        System.out.println("\n--- Testing JOIN Method with Performance Timing ---");
        startTime = System.nanoTime();
        List<Map<String, Object>> joinResults = executeQueryFromFile("06_3_performance_comparison_join.sql");
        long joinTime = System.nanoTime() - startTime;

        executionTimes.put("JOIN", joinTime);
        results.put("JOIN", joinResults);
        Set<Integer> joinCustomers = extractCustomerIds(joinResults);

        System.out.println("✅ JOIN found customers: " + joinCustomers);
        System.out.println("⏱️  JOIN execution time: " + formatTime(joinTime));

        // Test 4: Performance Analysis with EXPLAIN
        System.out.println("\n--- EXPLAIN Query Plans Analysis ---");
        analyzeQueryPlans();

        // Test 5: Multiple Runs for More Accurate Timing
        System.out.println("\n--- Multiple Runs Performance Test (5 runs each) ---");
        Map<String, Double> averageTimes = performMultipleRuns();

        // Test 6: Performance Comparison and Ranking
        performanceRanking(executionTimes);

        // Test 7: Performance Insights
        performanceInsights(executionTimes);

        // Test 8: Data Size Impact Analysis
        dataSizeImpactAnalysis(validator);

        // Test 9: Standard Test Validations
        result = existsResults; // Set for compatibility

        assertFalse(existsResults.isEmpty(), "EXISTS method should return results");
        assertFalse(inResults.isEmpty(), "IN method should return results");
        assertFalse(joinResults.isEmpty(), "JOIN method should return results");

        // Validate business logic
        for (Integer customerId : existsCustomers) {
            assertTrue(validator.customerHasHighValueTransactions(customerId, 500),
                    "Customer " + customerId + " should have transactions >= 500");
        }

        // Test 10: Result Structure Validation
        validateResultStructure(existsResults, "EXISTS");
        validateResultStructure(inResults, "IN");
        validateResultStructure(joinResults, "JOIN");

        performanceSummary(executionTimes);
    }

    /**
     * Formats execution time in appropriate units.
     */
    private String formatTime(long nanoSeconds) {
        if (nanoSeconds < 1_000) {
            return nanoSeconds + " ns";
        } else if (nanoSeconds < 1_000_000) {
            return String.format("%.2f μs", nanoSeconds / 1_000.0);
        } else if (nanoSeconds < 1_000_000_000) {
            return String.format("%.2f ms", nanoSeconds / 1_000_000.0);
        } else {
            return String.format("%.2f s", nanoSeconds / 1_000_000_000.0);
        }
    }

    /**
     * Performs multiple runs for more accurate timing.
     */
    private Map<String, Double> performMultipleRuns() throws IOException, SQLException {
        Map<String, Double> averageTimes = new HashMap<>();
        int runs = 5;

        String[] methods = {"EXISTS", "IN", "JOIN"};
        String[] files = {
                "06_1_performance_comparison_analysis.sql",
                "06_2_performance_comparison_in.sql",
                "06_3_performance_comparison_join.sql"
        };

        for (int i = 0; i < methods.length; i++) {
            String method = methods[i];
            String file = files[i];
            long totalTime = 0;

            System.out.println("Running " + method + " method " + runs + " times...");

            for (int run = 0; run < runs; run++) {
                long startTime = System.nanoTime();
                executeQueryFromFile(file);
                long endTime = System.nanoTime();
                totalTime += (endTime - startTime);
            }

            double averageTime = totalTime / (double) runs;
            averageTimes.put(method, averageTime);

            System.out.printf("   %s average: %s%n",
                    method, formatTime((long) averageTime));
        }

        return averageTimes;
    }

    /**
     * Analyzes query execution plans using EXPLAIN.
     */
    private void analyzeQueryPlans() {
        String[] queries = {
                "EXPLAIN (ANALYZE, BUFFERS) SELECT c.id FROM customers c WHERE EXISTS " +
                        "(SELECT 1 FROM accounts a JOIN transactions t ON a.id = t.account_id " +
                        "WHERE a.customer_id = c.id AND t.amount >= 500)",

                "EXPLAIN (ANALYZE, BUFFERS) SELECT c.id FROM customers c WHERE c.id IN " +
                        "(SELECT DISTINCT a.customer_id FROM accounts a JOIN transactions t ON a.id = t.account_id " +
                        "WHERE t.amount >= 500 AND a.customer_id IS NOT NULL)",

                "EXPLAIN (ANALYZE, BUFFERS) SELECT DISTINCT c.id FROM customers c " +
                        "JOIN accounts a ON c.id = a.customer_id JOIN transactions t ON a.id = t.account_id " +
                        "WHERE t.amount >= 500"
        };

        String[] methods = {"EXISTS", "IN", "JOIN"};

        for (int i = 0; i < queries.length; i++) {
            System.out.println("\n--- " + methods[i] + " Execution Plan ---");
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(queries[i])) {

                while (rs.next()) {
                    String plan = rs.getString(1);
                    // Extract timing information from plan
                    if (plan.contains("actual time=")) {
                        System.out.println("📊 " + plan);
                    } else if (plan.contains("Execution Time:")) {
                        System.out.println("⏱️  " + plan);
                    } else if (plan.contains("Planning Time:")) {
                        System.out.println("🧠 " + plan);
                    }
                }

            } catch (SQLException e) {
                System.out.println("⚠️  Could not analyze " + methods[i] + " plan: " + e.getMessage());
            }
        }
    }

    /**
     * Displays performance ranking.
     */
    private void performanceRanking(Map<String, Long> executionTimes) {
        System.out.println("\n--- Performance Ranking ---");
        List<Map.Entry<String, Long>> sortedTimes = executionTimes.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .toList();

        for (int i = 0; i < sortedTimes.size(); i++) {
            Map.Entry<String, Long> entry = sortedTimes.get(i);
            String rank = (i == 0) ? "🥇 FASTEST" : (i == 1) ? "🥈 MEDIUM" : "🥉 SLOWEST";
            System.out.printf("%s %s: %s%n",
                    rank, entry.getKey(), formatTime(entry.getValue()));
        }
    }

    /**
     * Provides performance insights.
     */
    private void performanceInsights(Map<String, Long> executionTimes) {
        System.out.println("\n--- Performance Insights ---");

        List<Map.Entry<String, Long>> sortedTimes = executionTimes.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .toList();

        String fastest = sortedTimes.get(0).getKey();
        String slowest = sortedTimes.get(sortedTimes.size() - 1).getKey();
        long fastestTime = sortedTimes.get(0).getValue();
        long slowestTime = sortedTimes.get(sortedTimes.size() - 1).getValue();

        double speedup = (double) slowestTime / fastestTime;
        System.out.printf(" %s is %.2fx faster than %s%n",
                fastest, speedup, slowest);
    }

    /**
     * Analyzes data size impact on performance.
     */
    private void dataSizeImpactAnalysis(BusinessDataValidator validator) throws SQLException {
        System.out.println("\n--- Data Size Impact ---");
        System.out.println("📊 Test data size:");
        System.out.println("   • Customers: " + validator.getCustomerCount());
        System.out.println("   • Accounts: " + validator.getAccountsCount());
        System.out.println("   • Transactions: " + validator.getAccountsWithTransactions().size());
        System.out.println("💡 Performance differences will be more pronounced with larger datasets");
    }

    /**
     * Validates result structure for different methods.
     */
    private void validateResultStructure(List<Map<String, Object>> results, String methodName) {
        if (!results.isEmpty()) {
            Map<String, Object> firstRow = results.get(0);

            boolean hasCustomerId = firstRow.containsKey("customer_id") || firstRow.containsKey("id");
            boolean hasCustomerName = firstRow.containsKey("customer_name") ||
                    firstRow.containsKey("name") ||
                    (firstRow.containsKey("first_name") && firstRow.containsKey("last_name"));
            boolean hasEmail = firstRow.containsKey("email");

            assertTrue(hasCustomerId, methodName + " should include customer ID");
            assertTrue(hasCustomerName, methodName + " should include customer name");
            assertTrue(hasEmail, methodName + " should include email");

            System.out.println(methodName + " structure validated");
        }
    }

    /**
     * Displays performance summary.
     */
    private void performanceSummary(Map<String, Long> executionTimes) {
        System.out.println("\n=== PERFORMANCE SUMMARY ===");
        System.out.println("Key Performance:");

        for (Map.Entry<String, Long> entry : executionTimes.entrySet()) {
            String method = entry.getKey();
            String time = formatTime(entry.getValue());
            String description = getMethodDescription(method);
            System.out.println("   • " + method + ": " + time + " - " + description);
        }

        System.out.println("💡 Remember: Performance differences depend on:");
        System.out.println("   • Data size (more data = bigger differences)");
        System.out.println("   • Indexes (proper indexing improves performance)");
        System.out.println("   • Query complexity (simpler is often faster)");
        System.out.println("   • Database optimizer (chooses execution plan)");
        System.out.println(" Performance comparison completed!");
    }

    /**
     * Gets description for each method.
     */
    private String getMethodDescription(String method) {
        return switch (method) {
            case "EXISTS" -> "Best for existence checks";
            case "IN" -> "Good for subqueries, watch for NULLs";
            case "JOIN" -> "Best when you need related data";
            default -> "Unknown method";
        };
    }

    /**
     * Executes performance analysis using EXPLAIN statements.
     */
    private void executePerformanceAnalysis() throws IOException, SQLException {
        try {
            // This file contains EXPLAIN statements for performance comparison
            String fullPath = getResourcePath("06_4_performance_comparison_explain.sql");
            String sqlContent = java.nio.file.Files.readString(java.nio.file.Paths.get(fullPath));

            // Split by EXPLAIN statements and execute each
            String[] explainQueries = sqlContent.split("EXPLAIN");

            System.out.println("📊 Performance Analysis Results:");

            try (Statement stmt = connection.createStatement()) {
                for (int i = 1; i < explainQueries.length; i++) { // Skip first empty split
                    String query = "EXPLAIN" + explainQueries[i].trim();
                    if (query.contains("SELECT")) {
                        System.out.println("\n--- Analysis " + i + " ---");
                        try (ResultSet rs = stmt.executeQuery(query)) {
                            while (rs.next()) {
                                System.out.println(rs.getString(1));
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("⚠️  Performance analysis had issues (this is normal): " + e.getMessage());
        }
    }

    /**
     * Validates that all methods return consistent results.
     */
    private void validateConsistentResults(Map<String, List<Map<String, Object>>> results) {
        Set<Integer> existsCustomers = extractCustomerIds(results.get("EXISTS"));
        Set<Integer> inCustomers = extractCustomerIds(results.get("IN"));
        Set<Integer> joinCustomers = extractCustomerIds(results.get("JOIN"));

        // All methods should find the same customers (business logic validation)
        assertEquals(existsCustomers, inCustomers,
                "EXISTS and IN methods should find the same customers");
        assertEquals(existsCustomers, joinCustomers,
                "EXISTS and JOIN methods should find the same customers");

        System.out.println("✅ All methods returned consistent results");
    }

    /**
     * Validates performance characteristics of each method.
     */
    private void validatePerformanceCharacteristics(Map<String, Long> executionTimes) {
        // Ensure all methods completed within reasonable time (10 seconds max)
        long maxTime = 10_000_000_000L; // 10 seconds in nanoseconds

        for (Map.Entry<String, Long> entry : executionTimes.entrySet()) {
            assertTrue(entry.getValue() < maxTime,
                    entry.getKey() + " method took too long: " + formatTime(entry.getValue()));
        }

        System.out.println("✅ All methods completed within acceptable time limits");
    }
}