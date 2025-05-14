package org.codeus.fundamentals.json_and_full_text_search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.codeus.fundamentals.json_and_full_text_search.config.EmbeddedPostgreSqlFullTextSearchSetup;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.TestMethodOrder;

@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class SqlQueriesTest extends EmbeddedPostgreSqlFullTextSearchSetup {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class FullTextSearchTest {

    @Order(1)
    @DisplayName("Basic tsvector and tsquery usage")
    @Test
    void testTsvectorWithTsquery() throws Exception {
      String taskFile = "queries/01_01_search_for_imagination.sql";
      setupSchema("schema.sql", "test-data.sql");
      final String sql = getSqlRequestFromFilePath(taskFile);
      if (!sql.contains("to_tsvector") && !sql.contains("to_tsquery") && !sql.contains("@@")) {
        fail("Please use match command with to_tsvector and to_tsquery");
      }
      final List<Map<String, Object>> resultSet = executeQuery(sql);
      printQueryResults(resultSet);

      final Map<String, Object> firstResult = resultSet.get(0);
      assertEquals(2, resultSet.size());
      assertNotNull(firstResult.get("person"));
      assertNotNull(firstResult.get("quote"));
    }

    @Order(2)
    @DisplayName("Basic tsquery with plainto usage")
    @Test
    void testPlaintoTsquery() throws Exception {
      String taskFile = "queries/01_02_plainto_with_tsquery.sql";
      setupSchema("schema.sql", "test-data.sql");
      final String sql = getSqlRequestFromFilePath(taskFile);
      if (!sql.contains("to_tsvector") && !sql.contains("plainto_tsquery") && !sql.contains("@@")) {
        fail("Please use match command with to_tsvector and plainto_tsquery");
      }
      final List<Map<String, Object>> resultSet = executeQuery(sql);
      printQueryResults(resultSet);

      final Map<String, Object> firstResult = resultSet.get(0);
      assertEquals(1, resultSet.size());
      assertNotNull(firstResult.get("person"));
      assertNotNull(firstResult.get("quote"));
    }

    @Order(3)
    @DisplayName("Search with prefix")
    @Test
    void searchWithPrefix() throws Exception {
      String taskFile = "queries/01_03_search_using_prefix.sql";
      setupSchema("schema.sql", "test-data.sql");
      final String sql = getSqlRequestFromFilePath(taskFile);
      if (!sql.contains("to_tsvector") && !sql.contains("plainto_tsquery") && !sql.contains("@@")) {
        fail("Please use match command with to_tsvector and plainto_tsquery");
      }
      final List<Map<String, Object>> resultSet = executeQuery(sql);
      printQueryResults(resultSet);

      final Map<String, Object> firstResult = resultSet.get(0);
      assertEquals(2, resultSet.size());
      assertNotNull(firstResult.get("person"));
      assertNotNull(firstResult.get("quote"));
    }

    @Order(4)
    @DisplayName("Search multiple word with AND")
    @Test
    void multipleSearchWithAnd() throws Exception {
      String taskFile = "queries/01_04_multiple_word_search_and.sql";
      setupSchema("schema.sql", "test-data.sql");
      final String sql = getSqlRequestFromFilePath(taskFile);
      if (!sql.contains("to_tsvector") && !sql.contains("plainto_tsquery") && !sql.contains("@@")) {
        fail("Please use match command with to_tsvector and plainto_tsquery");
      }
      final List<Map<String, Object>> resultSet = executeQuery(sql);
      printQueryResults(resultSet);

      final Map<String, Object> firstResult = resultSet.get(0);
      assertEquals(1, resultSet.size());
      assertNotNull(firstResult.get("person"));
      assertNotNull(firstResult.get("quote"));
      assertEquals("Alan Turing", firstResult.get("person"));
    }

    @Order(5)
    @DisplayName("Search multiple word with OR")
    @Test
    void multipleSearchWithOr() throws Exception {
      String taskFile = "queries/01_05_multiple_word_search_or.sql";
      setupSchema("schema.sql", "test-data.sql");
      final String sql = getSqlRequestFromFilePath(taskFile);
      if (!sql.contains("to_tsvector") && !sql.contains("plainto_tsquery") && !sql.contains("@@")) {
        fail("Please use match command with to_tsvector and plainto_tsquery");
      }
      final List<Map<String, Object>> resultSet = executeQuery(sql);
      printQueryResults(resultSet);

      final Set<String> names = resultSet
          .stream()
          .map(r -> String.valueOf(r.get("person")))
          .collect(Collectors.toSet());

      final Map<String, Object> firstResult = resultSet.get(0);
      assertEquals(2, resultSet.size());
      assertEquals(2, names.size());
      assertNotNull(firstResult.get("person"));
      assertNotNull(firstResult.get("quote"));
      assertTrue(names.contains("Alan Turing"));
      assertTrue(names.contains("Albert Einstein"));
    }

    @Order(6)
    @DisplayName("Create Gin index")
    @Test
    void createGinIndex() throws Exception {
      String taskFile = "queries/01_06_create_gin_index.sql";
      setupSchema("schema.sql", "test-data.sql");
      final String sql = getSqlRequestFromFilePath(taskFile);
      if (!sql.contains("WHERE body @@ to_tsquery")) {
        fail("Please use body with to_tsquery lookup");
      }
      final String[] commands = sql.split(";");
      int n = commands.length;
      final String select = commands[n - 1].concat(";");
      final String createIndex = commands[n - 2].concat(";");
      executeModificationQuery(createIndex);
      final List<Map<String, Object>> resultSet = executeQuery(select);
      printQueryResults(resultSet);

      final Map<String, Object> firstResult = resultSet.get(0);
      assertEquals(2, resultSet.size());
      assertNotNull(firstResult.get("body"));
      assertNotNull(firstResult.get("id"));
    }

  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class JsonTest {

    @Order(1)
    @DisplayName("Key-value request")
    @Test
    void testKeyValueRequest() throws Exception {
      String taskFile = "queries/02_01_search_for_key_value.sql";
      setupSchema("schema.sql", "test-data.sql");
      final String sql = getSqlRequestFromFilePath(taskFile);
      if (!sql.contains("@>")) {
        fail("Please use @> operator in query");
      }
      final List<Map<String, Object>> resultSet = executeQuery(sql);
      printQueryResults(resultSet);

      final Map<String, Object> firstResult = resultSet.get(0);
      assertEquals(1, resultSet.size());
      assertEquals("Moon Landing", firstResult.get("title"));
    }

    @Order(2)
    @DisplayName("Search by value")
    @Test
    void testSearchByValue() throws Exception {
      String taskFile = "queries/02_02_search_by_value.sql";
      setupSchema("schema.sql", "test-data.sql");
      final String sql = getSqlRequestFromFilePath(taskFile);
      if (!sql.contains("->>")) {
        fail("Please use ->> operator in query");
      }
      final List<Map<String, Object>> resultSet = executeQuery(sql);
      printQueryResults(resultSet);

      final Map<String, Object> firstResult = resultSet.get(0);
      assertEquals(1, resultSet.size());
      assertEquals("French Revolution", firstResult.get("title"));
    }

    @Order(3)
    @DisplayName("Check if key tags exists")
    @Test
    void checkTagsKeyExistence() throws Exception {
      String taskFile = "queries/02_03_check_key_existence.sql";
      setupSchema("schema.sql", "test-data.sql");
      final String sql = getSqlRequestFromFilePath(taskFile);
      if (!sql.contains("?")) {
        fail("Please use ? operator in query");
      }
      final List<Map<String, Object>> resultSet = executeQuery(sql);
      printQueryResults(resultSet);

      assertEquals(5, resultSet.size());
    }

    @Order(4)
    @DisplayName("Filter by year")
    @Test
    void filterByYear() throws Exception {
      String taskFile = "queries/02_04_filter_by_date.sql";
      setupSchema("schema.sql", "test-data.sql");
      final String sql = getSqlRequestFromFilePath(taskFile);

      final List<Map<String, Object>> resultSet = executeQuery(sql);
      printQueryResults(resultSet);

      final Set<String> titles = resultSet
          .stream()
          .map(r -> String.valueOf(r.get("title")))
          .collect(Collectors.toSet());

      assertEquals(2, resultSet.size());
      assertEquals(2, titles.size());
      assertTrue(titles.contains("Printing Press Invented"));
      assertTrue(titles.contains("French Revolution"));
    }

    @Order(5)
    @DisplayName("Filter by array value")
    @Test
    void filterByArrayValue() throws Exception {
      String taskFile = "queries/02_05_filter_by_array_value.sql";
      setupSchema("schema.sql", "test-data.sql");
      final String sql = getSqlRequestFromFilePath(taskFile);

      final List<Map<String, Object>> resultSet = executeQuery(sql);
      printQueryResults(resultSet);

      final Set<String> titles = resultSet
          .stream()
          .map(r -> String.valueOf(r.get("title")))
          .collect(Collectors.toSet());

      assertEquals(2, resultSet.size());
      assertEquals(2, titles.size());
      assertTrue(titles.contains("Fall of Berlin Wall"));
      assertTrue(titles.contains("French Revolution"));
    }



  }

  @AfterEach
  void tearDown() throws SQLException {
    clearDatabase();
  }


  private void setupSchema(String schemaFileName) throws SQLException, IOException {
    setupSchema(schemaFileName, null);
  }

  @Override
  protected void setupSchema(String schemaFileName, String dataFileName) throws SQLException, IOException {
    // Start a transaction that will be rolled back after each test
    connection.setAutoCommit(false);

    // Clear any existing data
    clearDatabase();

    // Initialize database schema
    System.out.println("Setting up database schema from: " + schemaFileName);
    executeSqlFile(getResourcePath(schemaFileName));

    if (dataFileName != null && !dataFileName.isBlank()) {
      // Load test data
      System.out.println("Setting up test data from: " + dataFileName);
      executeSqlFile(getResourcePath(dataFileName));
    }


    System.out.println("Setup complete");
  }
}