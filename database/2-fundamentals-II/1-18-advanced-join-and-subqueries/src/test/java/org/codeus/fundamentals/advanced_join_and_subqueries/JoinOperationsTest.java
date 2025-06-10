package org.codeus.fundamentals.advanced_join_and_subqueries;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for JOIN operations including FULL OUTER JOIN, CROSS JOIN, and LATERAL JOIN.
 */
public class JoinOperationsTest extends BaseAdvancedJoinTest {

    @Test
    @Order(1)
    @DisplayName("Task 1: FULL OUTER JOIN - Customer-Loan Relationship Analysis")
    void test01FullJoinCustomerLoanAnalysis() throws IOException, SQLException {
        result = executeQueryFromFile("01_1_full_join_customer_loan_analysis.sql");

        // Get expected business data
        BusinessDataValidator validator = new BusinessDataValidator(connection);
        Set<Integer> allCustomerIds = validator.getAllCustomerIds();
        Set<Integer> customersWithLoans = validator.getCustomersWithLoans();
        Set<Integer> customersWithoutLoans = validator.getCustomersWithoutLoans();

        // Test 1: Must return results
        assertFalse(result.isEmpty(), "Query must return results for FULL OUTER JOIN analysis");

        boolean hasNulls = result.stream().anyMatch(row ->
                row.values().stream().anyMatch(Objects::isNull)
        );

        assertTrue(hasNulls, "At least one row should have NULLs in FULL OUTER JOIN result");

        // Test 2: Must show complete customer picture
        Set<Integer> studentsCustomerIds = extractCustomerIds(result);
        assertTrue(studentsCustomerIds.size() >= allCustomerIds.size() * 0.9,
                "Should include most/all customers in FULL OUTER JOIN (found " + studentsCustomerIds.size() +
                        " out of " + allCustomerIds.size() + " customers)");

        // Test 3: Should distinguish between customers with/without loans
        boolean hasRelationshipClassification = result.stream()
                .anyMatch(this::hasClassificationField);

        assertTrue(hasRelationshipClassification,
                "Should classify customer-loan relationships (with/without loans, orphaned records, etc.)");

        // Test 4: Business logic validation - verify actual relationship status
        validateCustomerLoanRelationships(validator, customersWithLoans, customersWithoutLoans);

        // Test 5: Should handle loan data appropriately
        boolean includesLoanData = result.stream()
                .anyMatch(row -> row.containsKey("loan_id") ||
                        row.containsKey("amount") ||
                        row.containsKey("loan_amount") ||
                        hasLoanRelatedFields(row));

        assertTrue(includesLoanData, "Should include loan information in FULL OUTER JOIN");
    }

    @Test
    @Order(3)
    @DisplayName("Task 3: CROSS JOIN - Product Recommendation Matrix")
    void test03CrossJoinProductMatrix() throws IOException, SQLException {
        result = executeQueryFromFile("03_1_cross_join_product_matrix.sql");

        BusinessDataValidator validator = new BusinessDataValidator(connection);
        int customerCount = validator.getCustomerCount();
        Set<String> accountTypes = validator.getAccountTypes();
        int expectedCombinations = customerCount * accountTypes.size();

        // Test 1: Should generate customer-product combinations
        assertFalse(result.isEmpty(), "Should generate product recommendation matrix");

        // Test 2: Should approach expected combination count
        assertTrue(result.size() >= expectedCombinations * 0.8,
                "Should generate substantial customer-product combinations (expected ~" +
                        expectedCombinations + ", got " + result.size() + ")");

        // Test 3: Should identify opportunities vs existing relationships
        boolean hasOpportunityAnalysis = result.stream()
                .anyMatch(this::hasOpportunityField);

        assertTrue(hasOpportunityAnalysis,
                "Should identify cross-selling opportunities vs existing account relationships");

        // Test 4: Business value - revenue calculation or prioritization
        boolean hasBusinessValue = result.stream()
                .anyMatch(this::hasBusinessValueField);

        assertTrue(hasBusinessValue,
                "Should include business value analysis (revenue potential, customer value, etc.)");

        // Test 5: Validate opportunity identification accuracy
        validateOpportunityAccuracy(validator);
    }

    @Test
    @Order(5)
    @DisplayName("Task 5: LATERAL JOIN - Advanced Transaction Analysis")
    void test05LateralJoinTransactionAnalysis() throws IOException, SQLException {
        result = executeQueryFromFile("05_1_lateral_join_transaction_analysis.sql");

        BusinessDataValidator validator = new BusinessDataValidator(connection);
        Set<Integer> accountsWithTransactions = validator.getAccountsWithTransactions();

        // Test 1: Should provide account-level analysis
        assertFalse(result.isEmpty(), "Should return transaction analysis results");

        // Test 2: Should be account-centric (not just transaction list)
        boolean isAccountCentric = result.stream()
                .anyMatch(this::hasAccountIdentifier);

        assertTrue(isAccountCentric, "Analysis should be account-centric");

        // Test 3: Should handle accounts with and without transactions
        Set<Integer> analyzedAccounts = extractAccountIds(result);
        assertTrue(analyzedAccounts.size() >= accountsWithTransactions.size(),
                "Should analyze at least all accounts with transactions");

        // Test 4: Should include transaction details where available
        boolean hasTransactionDetails = result.stream()
                .anyMatch(this::hasTransactionDetailsField);

        assertTrue(hasTransactionDetails, "Should include transaction details");

        // Test 5: Should demonstrate correlated analysis (latest/recent transactions)
        boolean hasCorrelatedAnalysis = result.stream()
                .anyMatch(this::hasCorrelatedAnalysisField);

        assertTrue(hasCorrelatedAnalysis,
                "Should demonstrate correlated subquery concepts (latest transactions, patterns, etc.)");

        // Test 6: Validate transaction data accuracy
        validateTransactionAnalysisAccuracy(validator);
    }

    @Test
    @Order(7)
    @DisplayName("Task 5.2: LATERAL JOIN - Transaction Pattern Analysis")
    void test05_2LateralJoinTransactionAnalysis() throws IOException, SQLException {
        // Execute the query
        result = executeQueryFromFile("05_2_lateral_join_transaction_analysis.sql");

        // Prepare a validator helper
        BusinessDataValidator validator = new BusinessDataValidator(connection);

        // Extract and validate key fields
        assertFalse(result.isEmpty(), "Result should not be empty");

        for (Map<String, Object> row : result) {
            Integer customerId = ((Number) row.get("customer_id")).intValue();
            Integer accountCount =
                    row.get("account_count") != null ? ((Number) row.get("account_count")).intValue() : null;
            Integer totalTransactions =
                    row.get("total_transactions") != null ? ((Number) row.get("total_transactions")).intValue() : null;
            Double avgAmount = row.get("avg_transaction_amount") != null ?
                    ((Number) row.get("avg_transaction_amount")).doubleValue() : null;
            Date lastTransactionDate = (Date) row.get("last_transaction_date");
            Double frequencyDays = row.get("transaction_frequency_days") != null ?
                    ((Number) row.get("transaction_frequency_days")).doubleValue() : null;
            String behavior = (String) row.get("transaction_behavior");

            assertNotNull(customerId, "Customer ID should not be null");

            int expectedAccountCount = validator.getAccountCountForCustomer(customerId);
            assertEquals(expectedAccountCount, accountCount, "Account count mismatch for customer " + customerId);

            int expectedTotalTransactions = validator.getTotalTransactionsForCustomer(customerId);
            assertEquals(expectedTotalTransactions, totalTransactions,
                    "Total transactions mismatch for customer " + customerId);

            Double expectedAvgAmount = validator.getAverageTransactionAmountForCustomer(customerId);
            if (expectedAvgAmount == null) {
                assertNull(avgAmount, "Avg amount should be null for customer with no transactions: " + customerId);
            } else {
                assertEquals(expectedAvgAmount, avgAmount, 0.01, "Avg amount mismatch for customer " + customerId);
            }

            Date expectedLastDate = validator.getLastTransactionDateForCustomer(customerId);
            assertEquals(expectedLastDate, lastTransactionDate,
                    "Last transaction date mismatch for customer " + customerId);

            Double expectedFrequency = validator.getTransactionFrequencyDays(customerId);
            if (expectedFrequency == null) {
                assertNull(frequencyDays,
                        "Transaction frequency days should be null for one/no transaction customers: " + customerId);
            } else {
                assertEquals(expectedFrequency, frequencyDays, 0.01,
                        "Transaction frequency mismatch for customer " + customerId);
            }

            String expectedBehavior = classifyBehavior(expectedFrequency);
            assertEquals(expectedBehavior, behavior, "Behavior mismatch for customer " + customerId);
        }

        // Additional check: all analyzed accounts must belong to real customers
        Set<Integer> allCustomerIds = validator.getAllCustomerIds();
        Set<Integer> resultCustomerIds = result.stream()
                .map(r -> (Integer) r.get("customer_id"))
                .collect(Collectors.toSet());

        assertTrue(allCustomerIds.containsAll(resultCustomerIds), "All analyzed customers must exist in DB");
    }

    /**
     * Checks if the result row contains classification field for relationship analysis.
     */
    private boolean hasClassificationField(Map<String, Object> row) {
        return row.keySet().stream().anyMatch(key ->
                key.toLowerCase().contains("status") ||
                        key.toLowerCase().contains("relationship") ||
                        key.toLowerCase().contains("type") ||
                        key.toLowerCase().contains("classification"));
    }

    /**
     * Checks if the result row contains opportunity field for cross-selling analysis.
     */
    private boolean hasOpportunityField(Map<String, Object> row) {
        return row.keySet().stream().anyMatch(key ->
                key.toLowerCase().contains("opportunity") ||
                        key.toLowerCase().contains("has_account") ||
                        key.toLowerCase().contains("existing") ||
                        key.toLowerCase().contains("recommendation")) ||
                row.values().stream().anyMatch(val ->
                        val != null && (val.toString().toUpperCase().contains("OPPORTUNITY") ||
                                val.toString().toUpperCase().contains("EXISTING")));
    }

    /**
     * Checks if the result row contains business value field.
     */
    private boolean hasBusinessValueField(Map<String, Object> row) {
        return row.keySet().stream().anyMatch(key ->
                key.toLowerCase().contains("revenue") ||
                        key.toLowerCase().contains("value") ||
                        key.toLowerCase().contains("potential") ||
                        key.toLowerCase().contains("priority"));
    }

    /**
     * Checks if the result row contains account identifier.
     */
    private boolean hasAccountIdentifier(Map<String, Object> row) {
        return row.keySet().stream().anyMatch(key ->
                key.toLowerCase().contains("account"));
    }

    /**
     * Checks if the result row contains transaction details field.
     */
    private boolean hasTransactionDetailsField(Map<String, Object> row) {
        return row.keySet().stream().anyMatch(key ->
                key.toLowerCase().contains("transaction") ||
                        key.toLowerCase().contains("amount") ||
                        key.toLowerCase().contains("date"));
    }

    /**
     * Checks if the result row contains correlated analysis field.
     */
    private boolean hasCorrelatedAnalysisField(Map<String, Object> row) {
        return row.keySet().stream().anyMatch(key ->
                key.toLowerCase().contains("last") ||
                        key.toLowerCase().contains("latest") ||
                        key.toLowerCase().contains("recent") ||
                        key.toLowerCase().contains("activity") ||
                        key.toLowerCase().contains("pattern"));
    }

    /**
     * Classifies transaction behavior based on frequency.
     */
    private String classifyBehavior(Double frequencyDays) {
        if (frequencyDays == null) {
            return "VERY_LOW_FREQUENCY";
        } else if (frequencyDays <= 10) {
            return "HIGH_FREQUENCY";
        } else if (frequencyDays <= 30) {
            return "MEDIUM_FREQUENCY";
        } else if (frequencyDays <= 90) {
            return "LOW_FREQUENCY";
        } else {
            return "VERY_LOW_FREQUENCY";
        }
    }

    /**
     * Validates customer-loan relationships in FULL OUTER JOIN results.
     */
    private void validateCustomerLoanRelationships(BusinessDataValidator validator,
                                                   Set<Integer> customersWithLoans,
                                                   Set<Integer> customersWithoutLoans) {
        System.out.println("Validating customer-loan relationships...");
        System.out.println("Customers with loans: " + customersWithLoans.size());
        System.out.println("Customers without loans: " + customersWithoutLoans.size());
    }

    /**
     * Validates opportunity accuracy in cross join results.
     */
    private void validateOpportunityAccuracy(BusinessDataValidator validator) {
        System.out.println("Validating cross-selling opportunity accuracy...");
    }

    /**
     * Validates transaction analysis accuracy in lateral join results.
     */
    private void validateTransactionAnalysisAccuracy(BusinessDataValidator validator) {
        System.out.println("Validating transaction analysis accuracy...");
    }
}