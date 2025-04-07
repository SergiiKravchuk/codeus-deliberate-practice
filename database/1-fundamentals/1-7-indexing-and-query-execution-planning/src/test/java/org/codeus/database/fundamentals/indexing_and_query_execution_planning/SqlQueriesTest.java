package org.codeus.database.fundamentals.indexing_and_query_execution_planning;

import org.codeus.database.common.EmbeddedPostgreSqlSetup;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SqlQueriesTest extends EmbeddedPostgreSqlSetup {

    private static final String QUERIES_DIR = "queries/";
    private static final String B_TREE_INDEX_DIR = "b-tree-index/";
    private static final String HASH_INDEX_DIR = "hash-index/";
    private static final String TRANSACTIONS_TABLE_NAME = "transactions";
    private static final String ACCOUNTS_TABLE_NAME = "accounts";
    private static final String CUSTOMERS_TABLE_NAME = "customers";

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
        @Order(3)
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
        @Order(6)
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
        @Order(9)
        void O4_testBTreePartialIndexOnTransactions() throws IOException, SQLException {
            String filename = "04_b_tree_partial_index.sql";
            executeQueriesFromFile(QUERIES_DIR + B_TREE_INDEX_DIR + filename);

            String expectedIndexName = "account_type_idx";
            String expectedIndexDefinition = "CREATE INDEX account_type_idx ON public.accounts USING btree (account_type) WHERE ((account_type)::text = 'transfer'::text)";


            List<Map<String, Object>> createdIndex = fetchIndexDetails(ACCOUNTS_TABLE_NAME, expectedIndexName);

            assertIndexExists(createdIndex, expectedIndexName);
            assertIndexDefinition(createdIndex, expectedIndexDefinition);
        }

        @Test
        @Order(12)
        void O5_testBTreeCoveringIndexOnTransactions() throws IOException, SQLException {
            String filename = "05_b_tree_covering_index.sql";
            executeQueriesFromFile(QUERIES_DIR + B_TREE_INDEX_DIR + filename);

            String expectedIndexName = "transaction_amount_covering_idx";
            String expectedIndexDefinition = "CREATE INDEX transaction_amount_covering_idx ON public.transactions USING btree (amount) INCLUDE (account_id, transaction_type)";

            List<Map<String, Object>> createdIndex = fetchIndexDetails(TRANSACTIONS_TABLE_NAME, expectedIndexName);

            assertIndexExists(createdIndex, expectedIndexName);
            assertIndexDefinition(createdIndex, expectedIndexDefinition);
        }

        @Test
        @Order(15)
        void O6_testBTreeCoveringIndexOnTransactions() throws IOException, SQLException {
            String filename = "06_b_tree_index_on_join.sql";
            executeQueriesFromFile(QUERIES_DIR + B_TREE_INDEX_DIR + filename);

            String expectedIndexName = "accounts_customer_id_idx";
            String expectedIndexDefinition = "CREATE INDEX accounts_customer_id_idx ON public.accounts USING btree (customer_id)";

            List<Map<String, Object>> createdIndex = fetchIndexDetails(ACCOUNTS_TABLE_NAME, expectedIndexName);

            assertIndexExists(createdIndex, expectedIndexName);
            assertIndexDefinition(createdIndex, expectedIndexDefinition);
        }

        @Test
        @Order(18)
        void O7_testFunctionLowerBasedIndex() throws IOException, SQLException {
            String filename = "07_function_lower_based_index.sql";
            executeQueriesFromFile(QUERIES_DIR + B_TREE_INDEX_DIR + filename);

            String expectedIndexName = "idx_customers_lower_first_name";
            String expectedIndexDefinition = "CREATE INDEX idx_customers_lower_first_name ON public.customers USING btree (lower((email)::text))";

            List<Map<String, Object>> createdIndex = fetchIndexDetails(CUSTOMERS_TABLE_NAME, expectedIndexName);

            assertIndexExists(createdIndex, expectedIndexName);
            assertIndexDefinition(createdIndex, expectedIndexDefinition);
        }

        @Test
        @Order(20)
        void O8_testIndexWithCasting() throws IOException, SQLException {
            String filename = "08_index_with_casting.sql";
            executeQueriesFromFile(QUERIES_DIR + B_TREE_INDEX_DIR + filename);

            String expectedIndexName = "idx_transactions_date";
            String expectedIndexDefinition = "CREATE INDEX idx_transactions_date ON public.transactions USING btree (((transaction_date)::date))";

            List<Map<String, Object>> createdIndex = fetchIndexDetails(TRANSACTIONS_TABLE_NAME, expectedIndexName);

            assertIndexExists(createdIndex, expectedIndexName);
            assertIndexDefinition(createdIndex, expectedIndexDefinition);
        }

        @Test
        @Order(23)
        void O9_testIndexWithLikeOperation() throws IOException, SQLException {
            String filename = "09_b_tree_index_with_like.sql";
            executeQueriesFromFile(QUERIES_DIR + B_TREE_INDEX_DIR + filename);

            String expectedIndexName = "idx_customers_email";
            String expectedIndexDefinition = "CREATE INDEX idx_customers_email ON public.customers USING btree (email text_pattern_ops)";

            List<Map<String, Object>> createdIndex = fetchIndexDetails(CUSTOMERS_TABLE_NAME, expectedIndexName);

            assertIndexExists(createdIndex, expectedIndexName);
            assertIndexDefinition(createdIndex, expectedIndexDefinition);
        }

        @Test
        @Order(25)
        void O10_testIndexOnColumnWithLowSelectivity() throws IOException, SQLException {
            String filename = "10_index_on_column_with_low_selectivity.sql";
            executeQueriesFromFile(QUERIES_DIR + B_TREE_INDEX_DIR + filename);

            String expectedIndexName = "idx_transactions_transfer_partial";
            String expectedIndexDefinition = "CREATE INDEX idx_transactions_transfer_partial ON public.transactions USING btree (transaction_type) WHERE ((transaction_type)::text = 'transfer'::text)";
            String expectedDroppedIndex = "idx_transactions_type";

            List<Map<String, Object>> createdIndex = fetchIndexDetails(TRANSACTIONS_TABLE_NAME, expectedIndexName);
            List<Map<String, Object>> droppedIndex = fetchIndexDetails(TRANSACTIONS_TABLE_NAME, expectedDroppedIndex);

            assertIndexNotExists(droppedIndex, expectedDroppedIndex);
            assertIndexExists(createdIndex, expectedIndexName);
            assertIndexDefinition(createdIndex, expectedIndexDefinition);
        }

        @Test
        @Order(28)
        void O11_testImprovePerfomanceOfComplexQuery() throws IOException, SQLException {
            String filename = "11_improve_perfomance_of_complex_query.sql";
            executeQueriesFromFile(QUERIES_DIR + B_TREE_INDEX_DIR + filename);

            String expectedCustomersIndexName = "idx_customers_name";
            String expectedCustomersIndexDefinition = "CREATE INDEX idx_customers_name ON public.customers USING btree (first_name, last_name)";

            List<Map<String, Object>> createdCustomersIndex = fetchIndexDetails(CUSTOMERS_TABLE_NAME, expectedCustomersIndexName);

            assertIndexExists(createdCustomersIndex, expectedCustomersIndexName);
            assertIndexDefinition(createdCustomersIndex, expectedCustomersIndexDefinition);

            String expectedAccountsIndexName = "idx_accounts_customer_id";
            String expectedAccountsIndexDefinition = "CREATE INDEX idx_accounts_customer_id ON public.accounts USING btree (customer_id)";

            List<Map<String, Object>> createdAccountsIndex = fetchIndexDetails(ACCOUNTS_TABLE_NAME, expectedAccountsIndexName);

            assertIndexExists(createdAccountsIndex, expectedAccountsIndexName);
            assertIndexDefinition(createdAccountsIndex, expectedAccountsIndexDefinition);

            String expectedTransactionsIndexName = "idx_transactions_account_id";
            String expectedTransactionsIndexDefinition = "CREATE INDEX idx_transactions_account_id ON public.transactions USING btree (account_id)";

            List<Map<String, Object>> createdTransactionsIndex = fetchIndexDetails(TRANSACTIONS_TABLE_NAME, expectedTransactionsIndexName);

            assertIndexExists(createdTransactionsIndex, expectedTransactionsIndexName);
            assertIndexDefinition(createdTransactionsIndex, expectedTransactionsIndexDefinition);
        }

        @Test
        @Order(30)
        void O12_testIndexNotUsed() throws IOException, SQLException {
            String filename = "12_index_not_used.sql";
            executeQueriesFromFile(QUERIES_DIR + B_TREE_INDEX_DIR + filename);

            String expectedIndexName = "idx_transactions_date";
            String expectedIndexDefinition = "CREATE INDEX idx_transactions_date ON public.transactions USING btree (transaction_date)";
            String expectedDroppedIndex = "idx_transactions_type_date";

            List<Map<String, Object>> createdIndex = fetchIndexDetails(TRANSACTIONS_TABLE_NAME, expectedIndexName);
            List<Map<String, Object>> droppedIndex = fetchIndexDetails(TRANSACTIONS_TABLE_NAME, expectedDroppedIndex);

            assertIndexNotExists(droppedIndex, expectedDroppedIndex);
            assertIndexExists(createdIndex, expectedIndexName);
            assertIndexDefinition(createdIndex, expectedIndexDefinition);
        }
    }

    @Nested
    @DisplayName("Hash index tests")
    @Order(100)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class HashIndexTests {
        @Test
        @Order(100)
        void O1_testBasicHashIndex() throws IOException, SQLException {
            String filename = "01_basic_hash_index.sql";
            executeQueriesFromFile(QUERIES_DIR + HASH_INDEX_DIR + filename);

            String expectedIndexName = "idx_customers_phone_hash";
            String expectedIndexDefinition = "CREATE INDEX idx_customers_phone_hash ON public.customers USING hash (phone)";

            List<Map<String, Object>> createdIndex = fetchIndexDetails(CUSTOMERS_TABLE_NAME, expectedIndexName);

            assertIndexExists(createdIndex, expectedIndexName);
            assertIndexDefinition(createdIndex, expectedIndexDefinition);
        }

        @Test
        @Order(105)
        void O2_testBasicHashIndexWithRangeQuery() throws IOException, SQLException {
            String filename = "02_hash_index_with_range_query.sql";
            executeQueriesFromFile(QUERIES_DIR + HASH_INDEX_DIR + filename);

            String expectedIndexName = "idx_accounts_balance_hash";
            String expectedIndexDefinition = "CREATE INDEX idx_accounts_balance_hash ON public.accounts USING hash (balance)";

            List<Map<String, Object>> createdIndex = fetchIndexDetails(ACCOUNTS_TABLE_NAME, expectedIndexName);

            assertIndexExists(createdIndex, expectedIndexName);
            assertIndexDefinition(createdIndex, expectedIndexDefinition);
        }
    }

    private void assertIndexNotExists(List<Map<String, Object>> indexResults, String indexName) {
        assertEquals(0, indexResults.size(), "Index with name {%s} should not exist.".formatted(indexName));
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