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
public class WarmUpSqlQueriesTest extends EmbeddedPostgreSqlSetup {

    @Test
    @Order(1)
    void testSelectAllCustomerNamesEasyWarmup() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile("warmup/1_select_all_customer_names_easy_warmup.sql");
        assertFalse(results.isEmpty(), "Query should return results");
        assertEquals(6, results.size(), "Should be 6 customers");
        // You could add more specific assertions to check for individual names if needed
    }

    @Test
    @Order(2)
    void testSelectKyivCustomersOrderedByNameEasyWarmup() throws IOException, SQLException {
        List<Map<String, Object>> results = executeQueryFromFile("warmup/2_select_kyiv_customers_ordered_by_name_easy_warmup.sql");
        assertFalse(results.isEmpty(), "Query should return results");
        assertEquals(2, results.size(), "Should be 2 customers from Kyiv");
        List<String> customerNames = results.stream()
                .map(row -> (String) row.get("customer_name"))
                .toList();
        assertTrue(customerNames.contains("Ivan Kyivsky"), "Should contain Ivan Kyivsky");
        assertTrue(customerNames.contains("Maria Kyivska"), "Should contain Maria Kyivska");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "warmup/1_select_all_customer_names_easy_warmup.sql",
            "warmup/2_select_kyiv_customers_ordered_by_name_easy_warmup.sql"
    })
    void testQuerySyntax(String queryFile) throws IOException {
        // This test just ensures the query can be executed without syntax errors
        assertDoesNotThrow(() -> executeQueryFromFile(queryFile));
    }
}
