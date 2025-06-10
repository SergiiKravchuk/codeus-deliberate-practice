package org.codeus.fundamentals.advanced_join_and_subqueries;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for SET operations including UNION, UNION ALL, EXCEPT, and INTERSECT.
 */
public class SetOperationsTest extends BaseAdvancedJoinTest {

    @Test
    @Order(2)
    @DisplayName("Task 2.1: UNION - Contact Information Consolidation")
    void test02_1UnionContactConsolidationDeduplicated() throws IOException, SQLException {
        result = executeQueryFromFile("02_1_union_contact_consolidation.sql");

        BusinessDataValidator validator = new BusinessDataValidator(connection);
        int totalCustomers = validator.getCustomerCount();
        int totalEmployees = validator.getEmployeeCount();

        // Test 1: Should combine multiple data sources
        assertFalse(result.isEmpty(), "Should return combined contact list (deduplicated)");

        // Test 2: Should represent both customer and employee sources
        boolean hasContactTypeDistinction = result.stream()
                .anyMatch(this::hasContactTypeField);
        assertTrue(hasContactTypeDistinction,
                "Should distinguish between customer and employee contacts");

        // Test 3: Should have reasonable data volume (at most total customers + employees, but duplicates removed)
        assertTrue(result.size() <= (totalCustomers + totalEmployees),
                "Combined contact list (deduplicated) should have reasonable size relative to sources");

        // Test 4: Email format validation
        boolean hasValidEmails = result.stream()
                .anyMatch(this::hasValidEmailField);
        assertTrue(hasValidEmails, "Should include valid email addresses");

        // Test 5: UNION behavior check - no duplicate emails
        Set<String> emails = extractEmails(result);
        assertEquals(emails.size(), result.size(),
                "UNION should remove duplicate emails - no duplicates in result");
    }

    @Test
    @Order(3)
    @DisplayName("Task 2.2: UNION ALL - Contact Information Audit (including duplicates)")
    void test02_2UnionContactConsolidationAudit() throws IOException, SQLException {
        result = executeQueryFromFile("02_2_union_contact_consolidation_deduplicated.sql");

        BusinessDataValidator validator = new BusinessDataValidator(connection);
        int totalCustomers = validator.getCustomerCount();
        int totalEmployees = validator.getEmployeeCount();

        // Test 1: Should combine multiple data sources
        assertFalse(result.isEmpty(), "Should return full contact list (including duplicates)");

        // Test 2: Should represent both customer and employee sources
        boolean hasContactTypeDistinction = result.stream()
                .anyMatch(this::hasContactTypeField);
        assertTrue(hasContactTypeDistinction,
                "Should distinguish between customer and employee contacts");

        // Test 3: Should have reasonable data volume (including duplicates)
        assertTrue(result.size() >= (totalCustomers + totalEmployees),
                "Contact audit should include all records (including duplicates)");

        // Test 4: Email format validation
        boolean hasValidEmails = result.stream()
                .anyMatch(this::hasValidEmailField);
        assertTrue(hasValidEmails, "Should include valid email addresses");

        // Test 5: UNION ALL behavior check - duplicates allowed
        Set<String> emails = extractEmails(result);
        assertTrue(emails.size() < result.size(),
                "UNION ALL allows duplicate emails - result size may exceed unique emails");
    }

    @Test
    @Order(4)
    @DisplayName("Task 4: EXCEPT - Risk Analysis and Compliance")
    void test04ExceptRiskAnalysis() throws IOException, SQLException {
        result = executeQueryFromFile("04_1_except_risk_analysis.sql");

        BusinessDataValidator validator = new BusinessDataValidator(connection);
        Set<Integer> accountHolders = validator.getCustomersWithAccounts();
        Set<Integer> activeLoanHolders = validator.getCustomersWithActiveLoans();

        // Calculate expected loan prospects
        Set<Integer> expectedProspects = new HashSet<>(accountHolders);
        expectedProspects.removeAll(activeLoanHolders);

        // Test 1: Should identify loan prospects
        assertFalse(result.isEmpty(), "Should identify loan prospects");

        // Test 2: Should not exceed expected prospect count
        Set<Integer> studentProspects = extractCustomerIds(result);
        assertTrue(studentProspects.size() <= expectedProspects.size(),
                "Should not identify more prospects than actually exist (max: " +
                        expectedProspects.size() + ", found: " + studentProspects.size() + ")");

        // Test 3: Validate each identified prospect
        for (Integer customerId : studentProspects) {
            assertTrue(validator.customerHasAccounts(customerId),
                    "Loan prospect " + customerId + " must have at least one account");
            assertFalse(validator.customerHasActiveLoans(customerId),
                    "Loan prospect " + customerId + " must not have active loans");
        }

        // Test 4: Should include relevant customer information
        boolean hasCustomerDetails = result.stream()
                .anyMatch(this::hasCustomerDetailsField);

        assertTrue(hasCustomerDetails, "Should include customer details for prospects");

        // Test 5: Risk assessment or prioritization
        boolean hasRiskAssessment = result.stream()
                .anyMatch(this::hasRiskAssessmentField);

        assertTrue(hasRiskAssessment,
                "Should include risk assessment or prospect prioritization");
    }

    @Test
    @Order(10)
    @DisplayName("Task 7: INTERSECT - VIP Customer Analysis - Enhanced Validation")
    void test07IntersectVipAnalysisEnhanced() throws IOException, SQLException {
        // Execute the participant's query
        result = executeQueryFromFile("07_1_intersect_vip_analysis.sql");

        // Create validator for business logic verification
        BusinessDataValidator validator = new BusinessDataValidator(connection);

        // Step 1: Calculate expected VIP customers using business logic
        Set<Integer> highBalanceCustomers = validator.getCustomersWithHighBalance(5000);
        Set<Integer> highLoanCustomers = validator.getCustomersWithHighValueActiveLoans(10000);

        // Find intersection (VIP customers)
        Set<Integer> expectedVipCustomers = new HashSet<>(highBalanceCustomers);
        expectedVipCustomers.retainAll(highLoanCustomers);

        System.out.println("High balance customers (>5000): " + highBalanceCustomers);
        System.out.println("High loan customers (>10000, active): " + highLoanCustomers);
        System.out.println("Expected VIP customers (intersection): " + expectedVipCustomers);

        // Step 2: Handle edge case - no VIP customers
        if (expectedVipCustomers.isEmpty()) {
            assertTrue(result.isEmpty() || hasNoVipCustomersMessage(result),
                    "Should handle case with no VIP customers gracefully (empty result or informative message)");
            System.out.println("No VIP customers found in test data - test passed for edge case");
            return;
        }

        // Step 3: Basic result validation
        assertFalse(result.isEmpty(),
                "Query should identify VIP customers when they exist");

        // Step 4: Extract customer IDs from results
        Set<Integer> actualVipCustomers = extractCustomerIds(result);
        System.out.println("Actual VIP customers from query: " + actualVipCustomers);

        // Step 5: Validate INTERSECT logic - no false positives
        assertTrue(actualVipCustomers.size() <= expectedVipCustomers.size(),
                "Should not identify more VIP customers than actually exist (Expected: " +
                        expectedVipCustomers.size() + ", Found: " + actualVipCustomers.size() + ")");

        // Step 6: Validate each identified VIP customer meets both criteria
        for (Integer customerId : actualVipCustomers) {
            assertTrue(validator.customerHasHighBalance(customerId, 5000),
                    "VIP customer " + customerId + " must have account balance > 5000");
            assertTrue(validator.customerHasHighValueActiveLoans(customerId, 10000),
                    "VIP customer " + customerId + " must have active loan > 10000");
        }

        // Step 7: Validate comprehensive profile information
        validateVipCustomerProfiles(result, validator);

        // Step 8: Validate aggregation accuracy for each customer
        validateVipAggregationAccuracy(result, validator);

        // Step 9: Validate ordering (should be by total financial value DESC)
        validateVipOrdering(result);

        // Step 10: Validate INTERSECT was used (check for proper intersection behavior)
        validateIntersectBehavior(actualVipCustomers, highBalanceCustomers, highLoanCustomers);

        // Step 11: Performance consideration check
        assertTrue(result.size() <= 50,
                "VIP customer list should be manageable size for business use");

        System.out.println("VIP Analysis Test completed successfully!");
        System.out.println("Found " + actualVipCustomers.size() + " VIP customers out of " +
                validator.getCustomerCount() + " total customers");
    }

    @Test
    @Order(11)
    @DisplayName("Task 7: VIP Analysis - Data Consistency Validation")
    void test07VipAnalysisDataConsistency() throws SQLException {
        BusinessDataValidator validator = new BusinessDataValidator(connection);

        // Test data setup validation
        assertTrue(validator.getCustomerCount() > 0, "Should have customers in test data");
        assertTrue(validator.getAccountsCount() > 0, "Should have accounts in test data");
        assertTrue(validator.getActiveLoansCount() > 0, "Should have loans in test data");

        // Log counts for debugging
        System.out.println("Test data counts:");
        System.out.println("Total customers: " + validator.getCustomerCount());
        System.out.println("Total accounts: " + validator.getAccountsCount());
        System.out.println("Total loans: " + validator.getActiveLoansCount());
        System.out.println("Active loans: " + validator.getActiveLoansCount());

        // Validate test data has potential VIP customers
        Set<Integer> highBalanceCustomers = validator.getCustomersWithHighBalance(5000);
        Set<Integer> highLoanCustomers = validator.getCustomersWithHighValueActiveLoans(10000);

        System.out.println("VIP criteria analysis:");
        System.out.println("Customers with high balance (>5000): " + highBalanceCustomers.size());
        System.out.println("Customers with high loans (>10000, active): " + highLoanCustomers.size());

        // Log specific test data for debugging
        if (!highBalanceCustomers.isEmpty()) {
            System.out.println("High balance customers: " + highBalanceCustomers);
        }
        if (!highLoanCustomers.isEmpty()) {
            System.out.println("High loan customers: " + highLoanCustomers);
        }

        Set<Integer> intersection = new HashSet<>(highBalanceCustomers);
        intersection.retainAll(highLoanCustomers);
        System.out.println("Expected VIP customers: " + intersection);

        assertTrue(true, "Data consistency check completed - see logs for details");
    }

    /**
     * Checks if the result row contains contact type field.
     */
    private boolean hasContactTypeField(Map<String, Object> row) {
        return row.keySet().stream().anyMatch(key ->
                key.toLowerCase().contains("type") ||
                        key.toLowerCase().contains("source") ||
                        key.toLowerCase().contains("contact")) ||
                row.values().stream().anyMatch(val ->
                        val != null && (val.toString().toUpperCase().contains("CUSTOMER") ||
                                val.toString().toUpperCase().contains("EMPLOYEE")));
    }

    /**
     * Checks if the result row contains risk assessment field.
     */
    private boolean hasRiskAssessmentField(Map<String, Object> row) {
        return row.keySet().stream().anyMatch(key ->
                key.toLowerCase().contains("balance") ||
                        key.toLowerCase().contains("total") ||
                        key.toLowerCase().contains("value") ||
                        key.toLowerCase().contains("category") ||
                        key.toLowerCase().contains("tier"));
    }

    /**
     * Checks if the result has a "no VIP customers" message.
     */
    private boolean hasNoVipCustomersMessage(List<Map<String, Object>> result) {
        return result.stream().anyMatch(row ->
                row.values().stream().anyMatch(val ->
                        val != null && val.toString().toLowerCase().contains("no vip")));
    }

    /**
     * Validates VIP customer profile information.
     */
    private void validateVipCustomerProfiles(List<Map<String, Object>> results, BusinessDataValidator validator) {
        for (Map<String, Object> row : results) {

            assertNotNull(row.get("customer_id"), "customer_id should not be null");
            assertNotNull(row.get("customer_name"), "customer_name should not be null");
            assertNotNull(row.get("email"), "email should not be null");

            Number totalBalance = (Number) row.get("total_account_balance");
            Number totalLoans = (Number) row.get("total_active_loans");
            Number accountCount = (Number) row.get("number_of_accounts");
            Number loanCount = (Number) row.get("number_of_active_loans");

            assertNotNull(totalBalance, "total_account_balance should not be null");
            assertNotNull(totalLoans, "total_active_loans should not be null");
            assertNotNull(accountCount, "number_of_accounts should not be null");
            assertNotNull(loanCount, "number_of_active_loans should not be null");

            assertTrue(totalBalance.doubleValue() > 5000,
                    "VIP customer total balance should be > 5000, found: " + totalBalance);
            assertTrue(totalLoans.doubleValue() > 10000,
                    "VIP customer total active loans should be > 10000, found: " + totalLoans);
            assertTrue(accountCount.intValue() > 0,
                    "VIP customer should have at least one account");
            assertTrue(loanCount.intValue() > 0,
                    "VIP customer should have at least one active loan");
        }
    }

    /**
     * Validates VIP aggregation accuracy for each customer.
     */
    private void validateVipAggregationAccuracy(List<Map<String, Object>> results, BusinessDataValidator validator)
            throws SQLException {
        for (Map<String, Object> row : results) {
            Integer customerId = ((Number) row.get("customer_id")).intValue();

            double expectedTotalBalance = validator.getTotalAccountBalanceForCustomer(customerId);
            double actualTotalBalance = ((Number) row.get("total_account_balance")).doubleValue();
            assertEquals(expectedTotalBalance, actualTotalBalance, 0.01,
                    "Total account balance mismatch for customer " + customerId);

            double expectedTotalLoans = validator.getTotalActiveLoansForCustomer(customerId);
            double actualTotalLoans = ((Number) row.get("total_active_loans")).doubleValue();
            assertEquals(expectedTotalLoans, actualTotalLoans, 0.01,
                    "Total active loans mismatch for customer " + customerId);

            int expectedAccountCount = validator.getAccountCountForCustomer(customerId);
            int actualAccountCount = ((Number) row.get("number_of_accounts")).intValue();
            assertEquals(expectedAccountCount, actualAccountCount,
                    "Account count mismatch for customer " + customerId);

            int expectedLoanCount = validator.getActiveLoanCountForCustomer(customerId);
            int actualLoanCount = ((Number) row.get("number_of_active_loans")).intValue();
            assertEquals(expectedLoanCount, actualLoanCount,
                    "Active loan count mismatch for customer " + customerId);
        }
    }

    /**
     * Validates VIP ordering by total financial value.
     */
    private void validateVipOrdering(List<Map<String, Object>> results) {
        List<Double> totalValues = results.stream()
                .map(row -> {
                    double balance = ((Number) row.get("total_account_balance")).doubleValue();
                    double loans = ((Number) row.get("total_active_loans")).doubleValue();
                    return balance + loans;
                })
                .toList();

        // Check if properly ordered in descending order
        for (int i = 1; i < totalValues.size(); i++) {
            assertTrue(totalValues.get(i - 1) >= totalValues.get(i),
                    "Results should be ordered by total financial value (balance + loans) in descending order");
        }
    }

    /**
     * Validates INTERSECT behavior.
     */
    private void validateIntersectBehavior(Set<Integer> actualVipCustomers,
                                           Set<Integer> highBalanceCustomers,
                                           Set<Integer> highLoanCustomers) {
        // Ensure no customer appears who doesn't meet both criteria
        for (Integer customerId : actualVipCustomers) {
            assertTrue(highBalanceCustomers.contains(customerId),
                    "Customer " + customerId + " should be in high balance customer set");
            assertTrue(highLoanCustomers.contains(customerId),
                    "Customer " + customerId + " should be in high loan customer set");
        }
    }
}