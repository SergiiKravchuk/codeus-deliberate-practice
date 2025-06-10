package org.codeus.fundamentals.advanced_join_and_subqueries;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Business logic validator for database queries.
 * Provides methods to validate business rules and data integrity.
 */
public class BusinessDataValidator {
    private final Connection connection;

    public BusinessDataValidator(Connection connection) {
        this.connection = connection;
    }

    public Set<Integer> getAllCustomerIds() throws SQLException {
        return executeQuery("SELECT id FROM customers")
                .stream()
                .map(row -> (Integer) row.get("id"))
                .collect(Collectors.toSet());
    }

    public int getCustomerCount() throws SQLException {
        List<Map<String, Object>> result = executeQuery("SELECT COUNT(*) as count FROM customers");
        return ((Number) result.get(0).get("count")).intValue();
    }

    public Set<Integer> getCustomersWithAccounts() throws SQLException {
        return executeQuery("SELECT DISTINCT customer_id FROM accounts")
                .stream()
                .map(row -> (Integer) row.get("customer_id"))
                .collect(Collectors.toSet());
    }

    public boolean customerHasAccounts(Integer customerId) throws SQLException {
        List<Map<String, Object>> result = executeQuery(
                "SELECT COUNT(*) as count FROM accounts WHERE customer_id = " + customerId);
        return ((Number) result.get(0).get("count")).intValue() > 0;
    }

    public int getAccountCountForCustomer(Integer customerId) throws SQLException {
        String query = "SELECT COUNT(*) as count FROM accounts WHERE customer_id = " + customerId;
        List<Map<String, Object>> result = executeQuery(query);
        return ((Number) result.get(0).get("count")).intValue();
    }

    public double getTotalAccountBalanceForCustomer(Integer customerId) throws SQLException {
        String query = "SELECT COALESCE(SUM(balance), 0) as total FROM accounts WHERE customer_id = " + customerId;
        List<Map<String, Object>> result = executeQuery(query);
        return ((Number) result.get(0).get("total")).doubleValue();
    }

    public Set<Integer> getCustomersWithHighBalance(double balance) throws SQLException {
        return executeQuery("SELECT DISTINCT customer_id FROM accounts WHERE balance > " + balance)
                .stream()
                .map(row -> (Integer) row.get("customer_id"))
                .collect(Collectors.toSet());
    }

    public boolean customerHasHighBalance(Integer customerId, double balance) throws SQLException {
        List<Map<String, Object>> result = executeQuery(
                "SELECT COUNT(*) as count FROM accounts WHERE customer_id = " + customerId +
                        " AND balance > " + balance);
        return ((Number) result.get(0).get("count")).intValue() > 0;
    }

    public Set<Integer> getCustomersWithLoans() throws SQLException {
        return executeQuery("SELECT DISTINCT customer_id FROM loans WHERE customer_id IS NOT NULL")
                .stream()
                .map(row -> (Integer) row.get("customer_id"))
                .collect(Collectors.toSet());
    }

    public Set<Integer> getCustomersWithoutLoans() throws SQLException {
        Set<Integer> allCustomers = getAllCustomerIds();
        Set<Integer> customersWithLoans = getCustomersWithLoans();
        allCustomers.removeAll(customersWithLoans);
        return allCustomers;
    }

    public Set<Integer> getCustomersWithActiveLoans() throws SQLException {
        return executeQuery("SELECT DISTINCT customer_id FROM loans WHERE status = 'active'")
                .stream()
                .map(row -> (Integer) row.get("customer_id"))
                .collect(Collectors.toSet());
    }

    public boolean customerHasActiveLoans(Integer customerId) throws SQLException {
        List<Map<String, Object>> result = executeQuery(
                "SELECT COUNT(*) as count FROM loans WHERE customer_id = " + customerId +
                        " AND status = 'active'");
        return ((Number) result.get(0).get("count")).intValue() > 0;
    }

    public Set<Integer> getCustomersWithHighValueActiveLoans(double amount) throws SQLException {
        return executeQuery(
                "SELECT DISTINCT customer_id FROM loans " +
                        "WHERE amount > " + amount + " AND status = 'active'")
                .stream()
                .map(row -> (Integer) row.get("customer_id"))
                .collect(Collectors.toSet());
    }

    public boolean customerHasHighValueActiveLoans(Integer customerId, double amount) throws SQLException {
        List<Map<String, Object>> result = executeQuery(
                "SELECT COUNT(*) as count FROM loans WHERE customer_id = " + customerId +
                        " AND amount > " + amount + " AND status = 'active'");
        return ((Number) result.get(0).get("count")).intValue() > 0;
    }

    public double getTotalActiveLoansForCustomer(Integer customerId) throws SQLException {
        String query = "SELECT COALESCE(SUM(amount), 0) as total FROM loans WHERE customer_id = " + customerId +
                " AND status = 'active'";
        List<Map<String, Object>> result = executeQuery(query);
        return ((Number) result.get(0).get("total")).doubleValue();
    }

    public int getActiveLoanCountForCustomer(Integer customerId) throws SQLException {
        String query =
                "SELECT COUNT(*) as count FROM loans WHERE customer_id = " + customerId + " AND status = 'active'";
        List<Map<String, Object>> result = executeQuery(query);
        return ((Number) result.get(0).get("count")).intValue();
    }

    public int getActiveLoansCount() throws SQLException {
        List<Map<String, Object>> result = executeQuery("SELECT COUNT(*) as count FROM loans WHERE status = 'active'");
        return ((Number) result.get(0).get("count")).intValue();
    }

    public Set<Integer> getAllAccountIds() throws SQLException {
        return executeQuery("SELECT id FROM accounts")
                .stream()
                .map(row -> (Integer) row.get("id"))
                .collect(Collectors.toSet());
    }

    public int getAccountsCount() throws SQLException {
        List<Map<String, Object>> result = executeQuery("SELECT COUNT(*) as count FROM accounts");
        return ((Number) result.get(0).get("count")).intValue();
    }

    public Set<String> getAccountTypes() throws SQLException {
        return executeQuery("SELECT DISTINCT account_type FROM accounts")
                .stream()
                .map(row -> (String) row.get("account_type"))
                .collect(Collectors.toSet());
    }

    public Set<Integer> getAccountsWithTransactions() throws SQLException {
        return executeQuery("SELECT DISTINCT account_id FROM transactions")
                .stream()
                .map(row -> (Integer) row.get("account_id"))
                .collect(Collectors.toSet());
    }

    public Set<Integer> getCustomersWithHighValueTransactions(double amount) throws SQLException {
        String query = "SELECT DISTINCT a.customer_id FROM accounts a " +
                "JOIN transactions t ON a.id = t.account_id WHERE t.amount >= " + amount;

        List<Map<String, Object>> result = executeQuery(query);

        System.out.println("BusinessDataValidator query: " + query);
        System.out.println("Raw validator results: " + result);

        Set<Integer> customers = result.stream()
                .map(row -> (Integer) row.get("customer_id"))
                .collect(Collectors.toSet());

        System.out.println("Validator found customers: " + customers);
        return customers;
    }

    public boolean customerHasHighValueTransactions(Integer customerId, double amount) throws SQLException {

        String query = "SELECT COUNT(*) as count FROM accounts a " +
                "JOIN transactions t ON a.id = t.account_id " +
                "WHERE a.customer_id = " + customerId + " AND t.amount >= " + amount;

        List<Map<String, Object>> result = executeQuery(query);
        int count = ((Number) result.get(0).get("count")).intValue();

        return count > 0;
    }

    public int getTotalTransactionsForCustomer(Integer customerId) throws SQLException {
        String query = """
                    SELECT COUNT(t.id) as count
                    FROM accounts a
                    LEFT JOIN transactions t ON a.id = t.account_id
                    WHERE a.customer_id = %d
                """.formatted(customerId);

        List<Map<String, Object>> result = executeQuery(query);
        return ((Number) result.get(0).get("count")).intValue();
    }

    public Double getAverageTransactionAmountForCustomer(Integer customerId) throws SQLException {
        String query = """
                    SELECT ROUND(AVG(t.amount), 2) as avg_amount
                    FROM accounts a
                    JOIN transactions t ON a.id = t.account_id
                    WHERE a.customer_id = %d
                """.formatted(customerId);

        List<Map<String, Object>> result = executeQuery(query);

        if (result.isEmpty() || result.get(0).get("avg_amount") == null) {
            return null; // No transactions
        }

        return ((Number) result.get(0).get("avg_amount")).doubleValue();
    }

    public Date getLastTransactionDateForCustomer(Integer customerId) throws SQLException {
        String query = """
                    SELECT MAX(t.transaction_date) as last_date
                    FROM accounts a
                    JOIN transactions t ON a.id = t.account_id
                    WHERE a.customer_id = %d
                """.formatted(customerId);

        List<Map<String, Object>> result = executeQuery(query);

        if (result.isEmpty() || result.get(0).get("last_date") == null) {
            return null; // No transactions
        }

        return (Date) result.get(0).get("last_date");
    }

    public Double getTransactionFrequencyDays(Integer customerId) throws SQLException {
        String query = """
                    SELECT
                        CASE
                            WHEN COUNT(t.id) > 1 THEN
                                EXTRACT(DAY FROM (MAX(t.transaction_date) - MIN(t.transaction_date))) / COUNT(t.id)
                            ELSE NULL
                        END as frequency
                    FROM accounts a
                    JOIN transactions t ON a.id = t.account_id
                    WHERE a.customer_id = %d
                """.formatted(customerId);

        List<Map<String, Object>> result = executeQuery(query);

        if (result.isEmpty() || result.get(0).get("frequency") == null) {
            return null; // No transactions or only one transaction
        }

        return ((Number) result.get(0).get("frequency")).doubleValue();
    }

    public int getEmployeeCount() throws SQLException {
        List<Map<String, Object>> result = executeQuery("SELECT COUNT(*) as count FROM employees");
        return ((Number) result.get(0).get("count")).intValue();
    }

    /**
     * Executes a query and returns results as a list of maps.
     */
    private List<Map<String, Object>> executeQuery(String query) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnLabel(i).toLowerCase();
                    Object value = rs.getObject(i);
                    row.put(columnName, value);
                }
                result.add(row);
            }
        }
        return result;
    }
}