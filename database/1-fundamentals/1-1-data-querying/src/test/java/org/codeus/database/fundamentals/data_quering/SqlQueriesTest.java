package org.codeus.database.fundamentals.data_quering;

import org.codeus.database.common.EmbeddedPostgreSqlSetup;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SqlQueriesTest extends EmbeddedPostgreSqlSetup {

    @Order(3)
    @Test
    void testSelectCustomersKyivLviv() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile("main/3_select_customers_kyiv_lviv.sql");
        assertFalse(results.isEmpty(), "Query should return results");
        assertEquals(3, results.size(), "Should be 3 customers from Kyiv or Lviv");

        List<String> customerNames = results.stream()
                .map(row -> (String) row.get("customer_name"))
                .toList();
        assertTrue(customerNames.contains("Ivan Kyivsky"), "Should contain Ivan Kyivsky");
        assertTrue(customerNames.contains("Petro Lvivsky"), "Should contain Petro Lvivsky");
        assertTrue(customerNames.contains("Maria Kyivska"), "Should contain Maria Kyivska");
    }

    @Order(4)
    @Test
    void testSelectUniqueCustomerCitiesSorted() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile("main/4_select_unique_customer_cities_sorted.sql");
        assertFalse(results.isEmpty(), "Query should return results");
        assertEquals(4, results.size(), "Should be 4 unique cities");
        List<String> cities = results.stream()
                .map(row -> (String) row.get("city"))
                .toList();
        assertEquals(List.of("Dnipro", "Kharkiv", "Kyiv", "Lviv"), cities, "Cities should be sorted alphabetically");
    }

    @Order(5)
    @Test
    void testSelectSavingsAccountsBalanceBetween() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile("main/5_select_savings_accounts_balance_between.sql");
        assertFalse(results.isEmpty(), "Query should return results");
        assertEquals(2, results.size(), "Should be 2 savings accounts with balance between 10000 and 50000");

        List<String> accountNumbers = results.stream()
                .map(row -> (String) row.get("account_number"))
                .toList();
        assertTrue(accountNumbers.contains("SA002"), "Should contain SA002");
        assertTrue(accountNumbers.contains("SA003"), "Should contain SA003");
    }

    @Order(6)
    @Test
    void testSelectCurrentAccountsBalanceOutOfRange() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile("main/6_select_current_accounts_balance_out_of_range.sql");
        assertFalse(results.isEmpty(), "Query should return results");
        assertEquals(2, results.size(), "Should be 2 current accounts with balance out of range");

        List<String> accountNumbers = results.stream()
                .map(row -> (String) row.get("account_number"))
                .toList();
        assertTrue(accountNumbers.contains("CA001"), "Should contain CA001");
        assertTrue(accountNumbers.contains("CA004"), "Should contain CA004");
    }

    @Order(7)
    @Test
    void testListCustomersWithCheckingAccountsBase() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile("main/7_list_customers_with_checking_accounts_base.sql");
        assertFalse(results.isEmpty(), "Query should return results");
        assertEquals(3, results.size(), "Should be 3 customers with checking accounts");
        List<String> customerNames = results.stream()
                .map(row -> (String) row.get("customer_name"))
                .toList();
        assertTrue(customerNames.contains("Ivan Kyivsky"), "Should contain Ivan Kyivsky");
        assertTrue(customerNames.contains("Petro Lvivsky"), "Should contain Petro Lvivsky");
        assertTrue(customerNames.contains("Olena Dniprovska"), "Should contain Olena Dniprovska");
    }

    @Order(8)
    @Test
    void testFindTransactionsAbove10000SortedByDateBase() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile("main/8_find_transactions_above_10000_sorted_by_date_base.sql");
        assertFalse(results.isEmpty(), "Query should return results");
        assertEquals(1, results.size(), "Should be 1 transaction above 10000");
        assertEquals("2023-10-15", results.get(0).get("transaction_date").toString(), "Transaction date should be 2023-10-15");
    }

    @Order(9)
    @Test
    void testSelectCustomersMAndA() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile("main/9_select_customers_m_and_a.sql");
        assertFalse(results.isEmpty(), "Query should return results");
        assertEquals(2, results.size(), "Should be 2 customers names start with 'M' and contain 'а'");

        List<String> customerNames = results.stream()
                .map(row -> (String) row.get("customer_name"))
                .toList();
        assertTrue(customerNames.contains("Maria Kyivska"), "Should contain Maria Kyivska");
        assertTrue(customerNames.contains("Maksym Kharkivsky"), "Should contain Maksym Kharkivsky");
    }

    @Order(10)
    @Test
    void testSelectCustomersUkraineNotKharkiv() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile("main/10_select_customers_ukraine_not_kharkiv.sql");
        assertFalse(results.isEmpty(), "Query should return results");
        assertEquals(4, results.size(), "Should be 4 customers from Ukraine not Kharkiv");
        List<String> customerNames = results.stream()
                .map(row -> (String) row.get("customer_name"))
                .toList();
        assertTrue(customerNames.contains("Ivan Kyivsky"), "Should contain Ivan Kyivsky");
        assertTrue(customerNames.contains("Petro Lvivsky"), "Should contain Petro Lvivsky");
        assertTrue(customerNames.contains("Maria Kyivska"), "Should contain Maria Kyivska");
        assertTrue(customerNames.contains("Olena Dniprovska"), "Should contain Olena Dniprovska");
    }

    @Order(11)
    @Test
    void testSelectAccountsSortedByTypeAndBalance() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile("main/11_select_accounts_sorted_by_type_and_balance.sql");
        assertFalse(results.isEmpty(), "Query should return results");
        assertEquals(6, results.size(), "Should be 6 accounts");

        List<String> accountNumbers = results.stream().map(row -> (String) row.get("account_number")).toList();
        List<String> expectedOrder = List.of("SA003", "SA002", "SA001", "CA004", "CA002", "CA001");
        assertEquals(expectedOrder, accountNumbers, "Accounts should be sorted by type and balance DESC");
    }

    @Order(12)
    @Test
    void testSearchTransactionDescriptionsLikeBase() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile("main/12_search_transaction_descriptions_like_base.sql");
        assertFalse(results.isEmpty(), "Query should return results");
        assertEquals(3, results.size(), "Should be 3 transactions with type starting with 'deposit' (case-sensitive)");
        List<String> transactionTypes = results.stream()
                .map(row -> (String) row.get("transaction_type"))
                .toList();
        assertTrue(transactionTypes.contains("deposit"), "Should contain 'deposit'");
        assertTrue(transactionTypes.contains("deposit"), "Should contain 'deposit' (again)");
        assertTrue(transactionTypes.contains("deposit"), "Should contain 'deposit' (a third time)");
    }

    @Order(13)
    @Test
    void testSearchTransactionDescriptionsIlikeBase() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile("main/13_search_transaction_descriptions_ilike_base.sql");
        assertFalse(results.isEmpty(), "Query should return results");
        assertEquals(3, results.size(), "Should be 3 transactions with type containing 'deposit' (case-insensitive)");
        List<String> transactionTypes = results.stream()
                .map(row -> (String) row.get("transaction_type"))
                .toList();
        assertTrue(transactionTypes.contains("deposit"), "Should contain 'deposit'");
        assertTrue(transactionTypes.contains("deposit"), "Should contain 'deposit' (again)");
        assertTrue(transactionTypes.contains("deposit"), "Should contain 'deposit' (a third time)");
    }

    @Order(14)
    @Test
    void testSearchTransactionDepositIlikeBase() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile("main/14_search_transaction_deposit_iliike_base.sql");
        assertFalse(results.isEmpty(), "Query should return results");
        assertEquals(3, results.size(), "Should be 3 transactions with type starting with 'deposit' (case-insensitive)");
        List<String> transactionTypes = results.stream()
                .map(row -> (String) row.get("transaction_type"))
                .toList();
        assertTrue(transactionTypes.contains("deposit"), "Should contain 'deposit'");
        assertTrue(transactionTypes.contains("deposit"), "Should contain 'deposit' (again)");
        assertTrue(transactionTypes.contains("deposit"), "Should contain 'deposit' (a third time)");
    }

    @Order(15)
    @Test
    void testSearchTransactionDrawLikeBase() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile("main/15_search_transaction_draw_like_base.sql");
        assertFalse(results.isEmpty(), "Query should return results");
        assertEquals(2, results.size(), "Should be 2 transactions with type containing 'draw' (case-sensitive)");
        List<String> transactionTypes = results.stream()
                .map(row -> (String) row.get("transaction_type"))
                .toList();
        assertTrue(transactionTypes.contains("withdrawal"), "Should contain 'withdrawal'");
        assertTrue(transactionTypes.contains("withdrawal"), "Should contain 'withdrawal' (again)");
    }

    @Order(16)
    @Test
    void testCustomersWithSavingsAccountsKyivLvivBase() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile("main/16_customers_with_savings_accounts_kyiv_lviv_base.sql");
        assertFalse(results.isEmpty(), "Query should return results");
        assertEquals(3, results.size(), "Should be 3 customers with savings accounts in Kyiv or Lviv");
        List<String> customerNames = results.stream()
                .map(row -> (String) row.get("customer_name"))
                .toList();
        assertTrue(customerNames.contains("Ivan Kyivsky"), "Should contain Ivan Kyivsky");
        assertTrue(customerNames.contains("Petro Lvivsky"), "Should contain Petro Lvivsky");
        assertTrue(customerNames.contains("Maria Kyivska"), "Should contain Maria Kyivska");
    }

    @Order(17)
    @Test
    void testHighValueTransactionsSeptember2023Base() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile("main/17_high_value_transactions_september_2023_base.sql");
        assertFalse(results.isEmpty(), "Query should return results");
        assertEquals(1, results.size(), "Should be 1 high value transaction in September 2023");
        assertEquals("10000.00", results.get(0).get("amount").toString(), "Amount should be 10000.00");
        assertEquals("2023-09-10", results.get(0).get("transaction_date").toString(), "Transaction date should be 2023-09-10");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "main/3_select_customers_kyiv_lviv.sql",
            "main/4_select_unique_customer_cities_sorted.sql",
            "main/5_select_savings_accounts_balance_between.sql",
            "main/6_select_current_accounts_balance_out_of_range.sql",
            "main/7_list_customers_with_checking_accounts_base.sql",
            "main/8_find_transactions_above_10000_sorted_by_date_base.sql",
            "main/9_select_customers_m_and_a.sql",
            "main/10_select_customers_ukraine_not_kharkiv.sql",
            "main/11_select_accounts_sorted_by_type_and_balance.sql",
            "main/12_search_transaction_descriptions_like_base.sql",
            "main/13_search_transaction_descriptions_ilike_base.sql",
            "main/14_search_transaction_deposit_iliike_base.sql",
            "main/15_search_transaction_draw_like_base.sql",
            "main/16_customers_with_savings_accounts_kyiv_lviv_base.sql",
            "main/17_high_value_transactions_september_2023_base.sql"
    })
    void testQuerySyntax(String queryFile) throws IOException {
        // This test just ensures the query can be executed without syntax errors
        assertDoesNotThrow(() -> executeQueryFromFile(queryFile));
    }
}