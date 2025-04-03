package org.codeus.database.fundamentals.indexing_and_query_execution_planning;

import org.codeus.database.common.EmbeddedPostgreSqlSetup;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SqlQueriesTest extends EmbeddedPostgreSqlSetup {

    // File paths relative to src/test/resources
    private static final String QUERIES_DIR = "queries/";

    @Test
    @Order(1)
    void test_01_BasicBTreeIndexOnCustomersEmail() throws IOException, SQLException {
        String filename = "01_basic_b_tree_index_on_customers_email.sql";
        executeQueriesFromFile(QUERIES_DIR + filename);

        String indexQuery = "SELECT indexname, indexdef FROM pg_indexes " +
                "WHERE tablename = 'customers' AND indexname = 'idx_customers_email';";

        List<Map<String, Object>> createdIndex = executeQuery(indexQuery);

        printQueryResults(createdIndex);

        assertEquals(1, createdIndex.size(), "Index with name {%s} is not created.".formatted("idx_customers_email"));
        Map<String, Object> indexRow = createdIndex.get(0);
        boolean indexCorrect = "CREATE INDEX idx_customers_email ON public.customers USING btree (email)".equals(indexRow.get("indexdef"));
        assertTrue(indexCorrect, "Index definition is not correct");
    }

//    @ParameterizedTest
//    @ValueSource(strings = {
//            MANDATORY_COUNT_01, MANDATORY_COUNT_02, MANDATORY_COUNT_03, MANDATORY_SUM_04, MANDATORY_SUM_05,
//            MANDATORY_SUM_06, MANDATORY_AVG_07, MANDATORY_AVG_08, MANDATORY_AVG_09, MANDATORY_MIN_MAX_10,
//            MANDATORY_MIN_MAX_11, MANDATORY_MIN_MAX_12, MANDATORY_GROUPING_HAVING_13, MANDATORY_GROUPING_HAVING_14,
//            MANDATORY_GROUPING_HAVING_15, OPTIONAL_COUNT_16, OPTIONAL_COUNT_17, OPTIONAL_COUNT_18, OPTIONAL_COUNT_19,
//            OPTIONAL_COUNT_20, OPTIONAL_COUNT_21, OPTIONAL_SUM_22, OPTIONAL_SUM_23, OPTIONAL_SUM_24, OPTIONAL_SUM_25,
//            OPTIONAL_SUM_26, OPTIONAL_AVG_27, OPTIONAL_AVG_28, OPTIONAL_AVG_29, OPTIONAL_AVG_30, OPTIONAL_MIN_MAX_31,
//            OPTIONAL_MIN_MAX_32, OPTIONAL_MIN_MAX_33, OPTIONAL_MIN_MAX_34, OPTIONAL_GROUPING_HAVING_35,
//            OPTIONAL_GROUPING_HAVING_36, OPTIONAL_GROUPING_HAVING_37, OPTIONAL_GROUPING_HAVING_38,
//            OPTIONAL_GROUPING_HAVING_39, OPTIONAL_GROUPING_HAVING_40, OPTIONAL_GROUPING_HAVING_41,
//            OPTIONAL_GROUPING_HAVING_42, OPTIONAL_GROUPING_HAVING_43, OPTIONAL_GROUPING_HAVING_44,
//            OPTIONAL_GROUPING_HAVING_45
//    })
//    void testQuerySyntax(String queryFile) throws IOException {
//        // This test just ensures the query can be executed without syntax errors
//        assertDoesNotThrow(() -> executeQueryFromFile(queryFile));
//    }
}