package org.codeus.database.fundamentals.indexing_and_query_execution_planning;

import org.codeus.database.common.EmbeddedPostgreSqlSetup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SqlQueriesTest extends EmbeddedPostgreSqlSetup {

    // File paths relative to src/test/resources
    private static final String QUERIES_DIR = "queries/";
    private static final String B_TREE_INDEX_DIR = "b-tree-index/";
    private static final String TRANSACTIONS_TABLE_NAME = "transactions";
    private static final String ACCOUNTS_TABLE_NAME = "accounts";

    @Nested
    @DisplayName("B-Tree index tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class BTreeIndexTests {
        @Test
        @Order(1)
        void O1_testBasicBTreeIndexOnTransactions() throws IOException, SQLException {
            String filename = "01_basic_b_tree_index.sql";
            executeQueriesFromFile(QUERIES_DIR + B_TREE_INDEX_DIR + filename);

            String expectedIndexName = "transaction_amount_idx";
            String expectedIndexDefinition = "CREATE INDEX transaction_amount_idx ON public.transactions USING btree (amount)";


            List<Map<String, Object>> createdIndex = fetchIndexDetails(TRANSACTIONS_TABLE_NAME, expectedIndexName);

            assertIndexExists(createdIndex, expectedIndexName);
            assertIndexDefinition(createdIndex, expectedIndexDefinition);
        }

        @Test
        @Order(5)
        void O2_testCompositeBTreeIndexOnTransactions() throws IOException, SQLException {
            String filename = "02_composite_b_tree_index_on_transactions.sql";
            executeQueriesFromFile(QUERIES_DIR + B_TREE_INDEX_DIR + filename);

            String expectedIndexName = "transaction_amount_and_date_idx";
            String expectedIndexDefinition = "CREATE INDEX transaction_amount_and_date_idx ON public.transactions USING btree (amount, transaction_date)";


            List<Map<String, Object>> createdIndex = fetchIndexDetails(TRANSACTIONS_TABLE_NAME, expectedIndexName);

            assertIndexExists(createdIndex, expectedIndexName);
            assertIndexDefinition(createdIndex, expectedIndexDefinition);
        }

        @Test
        @Order(10)
        void O3_testBTreeIndexWithSortingOnTransactions() throws IOException, SQLException {
            String filename = "03_b_tree_index_with_sorting.sql";
            executeQueriesFromFile(QUERIES_DIR + B_TREE_INDEX_DIR + filename);

            String expectedIndexName = "transaction_account_id_and_date_idx";
            String expectedIndexDefinition = "CREATE INDEX transaction_account_id_and_date_idx ON public.transactions USING btree (account_id, transaction_date)";


            List<Map<String, Object>> createdIndex = fetchIndexDetails(TRANSACTIONS_TABLE_NAME, expectedIndexName);

            assertIndexExists(createdIndex, expectedIndexName);
            assertIndexDefinition(createdIndex, expectedIndexDefinition);
        }

        @Test
        @Order(15)
        void O4_testBTreePartialIndexOnTransactions() throws IOException, SQLException {
            String filename = "04_b_tree_partial_index.sql";
            executeQueriesFromFile(QUERIES_DIR + B_TREE_INDEX_DIR + filename);

            String expectedIndexName = "account_type_idx";
            String expectedIndexDefinition = "CREATE INDEX account_type_idx ON public.accounts USING btree (account_type) WHERE ((account_type)::text = 'savings'::text)";


            List<Map<String, Object>> createdIndex = fetchIndexDetails(ACCOUNTS_TABLE_NAME, expectedIndexName);

            assertIndexExists(createdIndex, expectedIndexName);
            assertIndexDefinition(createdIndex, expectedIndexDefinition);
        }

        @Test
        @Order(20)
        void O5_testBTreeCoveringIndexOnTransactions() throws IOException, SQLException {
            String filename = "05_b_tree_covering_index.sql";
            executeQueriesFromFile(QUERIES_DIR + B_TREE_INDEX_DIR + filename);

            String expectedIndexName = "transaction_amount_covering_idx";
            String expectedIndexDefinition = "CREATE INDEX transaction_amount_covering_idx ON public.transactions USING btree (amount) INCLUDE (account_id, transaction_type)";


            List<Map<String, Object>> createdIndex = fetchIndexDetails(TRANSACTIONS_TABLE_NAME, expectedIndexName);

            assertIndexExists(createdIndex, expectedIndexName);
            assertIndexDefinition(createdIndex, expectedIndexDefinition);
        }
    }


    private List<Map<String, Object>> fetchIndexDetails(String tableName, String indexName) throws SQLException {
        String indexQuery = String.format(
                "SELECT indexname, indexdef FROM pg_indexes WHERE tablename = '%s' AND indexname = '%s';",
                tableName,
                indexName
        );
        return executeQuery(indexQuery);
    }

    private void assertIndexExists(List<Map<String, Object>> indexResults, String indexName) {
        assertEquals(1, indexResults.size(), "Index with name {%s} is not created.".formatted(indexName));
    }

    private void assertIndexDefinition(List<Map<String, Object>> indexResults, String expectedDefinition) {
        String actualDefinition = (String) indexResults.get(0).get("indexdef");
        boolean isEqual = expectedDefinition.equals(actualDefinition);
        assertTrue(isEqual, "Index definition is not correct");
    }
}