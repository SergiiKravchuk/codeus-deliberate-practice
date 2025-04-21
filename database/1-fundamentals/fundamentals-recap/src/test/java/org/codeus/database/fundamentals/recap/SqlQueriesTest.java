package org.codeus.database.fundamentals.recap;

import org.codeus.database.fundamentals.recap.setup.EmbeddedPostgreSqlSetupHelper;
import org.codeus.database.fundamentals.recap.setup.ExternalPostgreSqlSetupHelper;
import org.codeus.database.fundamentals.recap.setup.ProblemSolutionAnalyser;
import org.codeus.database.fundamentals.recap.setup.ProblemSolutionAnalyser.Report;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class SqlQueriesTest {

  @Nested
  @Order(1)
  @DisplayName("Block 1")
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class Block_1 extends EmbeddedPostgreSqlSetupHelper {

    private static final String SCHEMA_FILE = "queries/block_1/block_setup/schema.sql";
    private static final String TEST_DATA_FILE = "queries/block_1/block_setup/test-data.sql";

    /**
     * Controls how much time one transaction waits for another to finish its work.
     * Increase value if you're facing unusual exceptions
     * @return number of millis
     */
    protected int getThreadMaxSleepMillis() {
      return super.getThreadMaxSleepMillis();
    }

    @Override
    protected Optional<String> getSchemaSetupFile() {
      return Optional.of(SCHEMA_FILE);
    }

    @Override
    protected Optional<String> getTestDataSetupFile() {
      return Optional.of(TEST_DATA_FILE);
    }

    @Test
    @Order(1)
    void problem_1() throws IOException, SQLException, InterruptedException, ExecutionException {
      String readTransaction = readSolutionFile(PROBLEM_1_DIR, "problem_1_read_transaction");
      String updateTransaction = readSolutionFile(PROBLEM_1_DIR, "problem_1_update_transaction");

      executeConcurrently(readTransaction, updateTransaction);
      List<Map<String, Object>> results = executeQuery(
        "SELECT first_read, second_read FROM isolation_level_test ORDER BY test_id DESC");

      assertFalse(results.isEmpty(), "Results of your solution cannot be found. Check your solution");

      var rowResult = results.get(0);
      var firstReadBalance = convertToDouble(rowResult.get("first_read"));
      var secondReadBalance = convertToDouble(rowResult.get("second_read"));
      assertEquals(firstReadBalance, secondReadBalance,
        "Row values (balance) should be the same for consequent read operations within one transaction");
    }

    @Test
    @Order(5)
    void problem_2() throws IOException, SQLException, InterruptedException, ExecutionException {
      String readTransaction = readSolutionFile(PROBLEM_2_DIR, "problem_2_read_transaction");
      String updateTransaction = readSolutionFile(PROBLEM_2_DIR, "problem_2_update_transaction");

      executeConcurrently(readTransaction, updateTransaction);
      List<Map<String, Object>> results = executeQuery(
        "SELECT * FROM isolation_level_test ORDER BY test_id");

      assertFalse(results.isEmpty(), "Results of your solution cannot be found. Check your solution");

      var assertFailures = new ArrayList<String>();
      for (Map<String, Object> rowResult : results) {
        var firstReadBalance = convertToDouble(rowResult.get("first_read"));
        var secondReadBalance = convertToDouble(rowResult.get("second_read"));
        if (firstReadBalance != secondReadBalance) {
          var failureMessage = "Balance for account id='%s' changed between read consequent read operations: %.2f -> %.2f"
            .formatted(rowResult.get("account_id"), firstReadBalance, secondReadBalance);
          assertFailures.add(failureMessage);
        }
      }
      var failureReasonMessage = assertFailures.stream().collect(Collectors.joining("\n", "\n", "\n"));
      assertTrue(assertFailures.isEmpty(),
        "Row values should be the same for consequent read operations within one transaction. Failure reason: " + failureReasonMessage);
    }

    @Test
    @Order(10)
    void problem_3() throws IOException, SQLException, InterruptedException, ExecutionException {
      String readTransaction = readSolutionFile(PROBLEM_3_DIR, "problem_3_read_transaction");
      String updateTransaction = readSolutionFile(PROBLEM_3_DIR, "problem_3_update_transaction");

      executeConcurrently(readTransaction, updateTransaction);
      List<Map<String, Object>> results = executeQuery(
        "SELECT * FROM isolation_level_test ORDER BY test_id");

      assertFalse(results.isEmpty(), "Results of your solution cannot be found. Check your solution");

      var rowResult = results.get(0);
      var firstReadRowCount = convertToDouble(rowResult.get("first_read"));
      var secondReadRowCount = convertToDouble(rowResult.get("second_read"));
      assertEquals(firstReadRowCount, secondReadRowCount,
        "Row count should be the same for consequent read operations within one transaction");
    }

  }

  @Nested
  @Order(2)
  @DisplayName("Block 2")
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class Block_2 extends ExternalPostgreSqlSetupHelper {

    private final ProblemSolutionAnalyser analyser = new ProblemSolutionAnalyser(connection);

    @AfterEach
    public void rollbackProblemChanges() throws SQLException {
      connection.rollback();
    }

    @Test
    @Order(1)
    void problem_1() throws SQLException, InterruptedException {
      var problemIndex = 1;
      var problemMetadata = getProblemMetadata(problemIndex);
      printCurrentProblemHeader(problemIndex);

      Report report = analyser.analyze(problemMetadata, this::executeQueriesFromFileRuntime);
      analyser.printFullReport(report);

      assertTrue(report.isOptimizationAchieved());
    }

    @Test
    @Order(5)
    void problem_2() throws SQLException {
      var problemIndex = 2;
      var problemMetadata = getProblemMetadata(problemIndex);
      printCurrentProblemHeader(problemIndex);

      Report report = analyser.analyze(problemMetadata, this::executeQueriesFromFileRuntime);

      analyser.printOptimizationMechanismResult(report);
      assertTrue(report.isOptimizationMechanismIntended());
    }

    @Test
    @Order(10)
    void problem_3() throws SQLException {
      var problemIndex = 3;
      var problemMetadata = getProblemMetadata(problemIndex);
      printCurrentProblemHeader(problemIndex);

      Report report = analyser.analyze(problemMetadata, this::executeQueriesFromFileRuntime);

      analyser.printFullReport(report);
      assertTrue(report.isOptimizationAchieved());
    }

    @Test
    @Order(15)
    void problem_4() throws SQLException {
      var problemIndex = 4;
      var problemMetadata = getProblemMetadata(problemIndex);
      printCurrentProblemHeader(problemIndex);

      Report report = analyser.analyze(problemMetadata, this::executeQueriesFromFileRuntime);

      analyser.printOptimizationMechanismResult(report);

      assertTrue(report.isOptimizationMechanismIntended());
    }

    @Test
    @Order(20)
    void problem_5() throws SQLException {
      var problemIndex = 5;
      var problemMetadata = getProblemMetadata(problemIndex);
      printCurrentProblemHeader(problemIndex);

      Report report = analyser.analyze(problemMetadata, this::executeQueriesFromFileRuntime);

      analyser.printFullReport(report);
      assertTrue(report.isOptimizationAchieved());
    }

    @Test
    @Order(25)
    void problem_6() throws SQLException {
      var problemIndex = 6;
      var problemMetadata = getProblemMetadata(problemIndex);
      printCurrentProblemHeader(problemIndex);

      Report report = analyser.analyze(problemMetadata, this::executeQueriesFromFileRuntime);

      analyser.printFullReport(report);
      assertTrue(report.isOptimizationAchieved());
    }

    @Test
    @Order(30)
    void problem_7() throws SQLException {
      var problemIndex = 7;
      var problemMetadata = getProblemMetadata(problemIndex);
      printCurrentProblemHeader(problemIndex);

      Report report = analyser.analyze(problemMetadata, this::executeQueriesFromFileRuntime);

      analyser.printFullReport(report);
      assertTrue(report.isOptimizationAchieved());
    }

    @Test
    @Order(35)
    void problem_8() throws SQLException {
      var problemIndex = 8;
      var problemMetadata = getProblemMetadata(problemIndex);
      printCurrentProblemHeader(problemIndex);

      Report report = analyser.analyze(problemMetadata, this::executeQueriesFromFileRuntime);

      analyser.printFullReport(report);
      assertTrue(report.isOptimizationAchieved());
    }

    private void executeQueriesFromFileRuntime(String filename) {
      try {
        executeQueriesFromFile(filename);
      } catch (IOException | SQLException e) {
        throw new RuntimeException(e);
      }
    }

    private void printCurrentProblemHeader(int problemIndex) {
      System.out.println("=".repeat(120));
      System.out.printf("- PROBLEM %d%n", problemIndex);
      System.out.println("=".repeat(120));
    }

    private void printIndexes(ProblemMetadata metadata) throws SQLException {
      if (metadata.indexes().isEmpty()) return;

      var tablesNames = metadata.tables().stream().map(name -> "'" + name + "'").collect(Collectors.joining(","));
      String query = """
      SELECT tablename, indexname, indexdef 
      FROM pg_catalog.pg_indexes 
      WHERE tablename IN (%s);
      """.formatted(tablesNames);

      executeQueryAndPrintResult(query);
    }

    @SuppressWarnings({"unused"})
    private void printViews(ProblemMetadata metadata) throws SQLException {
      if (metadata.views().isEmpty()) return;

      String query = """
      SELECT viewname, definition
      FROM pg_catalog.pg_views
      WHERE schemaname NOT IN ('pg_catalog', 'information_schema')
      UNION
      SELECT matviewname as viewname, definition
      FROM pg_catalog.pg_matviews
      WHERE schemaname NOT IN ('pg_catalog', 'information_schema')
      """;
      executeQueryAndPrintResult(query);
    }

    private void printMainQueryResults(ProblemMetadata metadata) throws SQLException {
      executeQueryAndPrintResult(metadata.query());
    }
  }

  @Nested
  @Order(3)
  @DisplayName("Block 3")
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class Block_3 extends EmbeddedPostgreSqlSetupHelper {

    private static final String TABLES_CHECK_PROCEDURE = "queries/block_3/block_setup/tables_check_procedure.sql";

    @Override
    protected Optional<String> getFunctionSetupFile() {
      return Optional.of(TABLES_CHECK_PROCEDURE);
    }

    @Test
    @Order(1)
    void checkTablesExistAndHaveData() throws SQLException, IOException {
      String createDatabaseSql = Files.readString(Paths.get(getResourcePath("queries/block_3/database_design.sql")));

      execute(createDatabaseSql);
      boolean areTablesExist = executeFunction("SELECT has_three_non_empty_tables()");

      if (areTablesExist) {
        System.out.println("✅ At least 3 non-empty tables exist.");
      } else {
        System.out.println("❌ Fewer than 3 non-empty tables found.");
      }
      assertTrue(areTablesExist, "At least 3 non-empty tables should exist.");
    }
  }

}