package org.codeus.database.fundamentals.recap.setup;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

public class EmbeddedPostgreSqlSetupHelper {

  //Increase this value if tests failing as the result of transactions are out of sync.
  protected static final int THREAD_MAX_SLEEP_MILLIS = 1800;
  protected static final String PROBLEM_1_DIR = "queries/block_1/problem_1/";
  protected static final String PROBLEM_2_DIR = "queries/block_1/problem_2/";
  protected static final String PROBLEM_3_DIR = "queries/block_1/problem_3/";



  private static final String SETUP_DIR = "setup/";
  private static final String SETUP_ENDING = "-setup";
  private static final String SOLUTION_MARKER = "--SOLUTION";

  protected static EmbeddedPostgres postgres;
  protected static Connection connection;

  protected int getThreadMaxSleepMillis() {
    return THREAD_MAX_SLEEP_MILLIS;
  }

  protected Optional<String> getSchemaSetupFile() {
    return Optional.empty();
  }
  protected Optional<String> getTestDataSetupFile() {
    return Optional.empty();
  }
  protected Optional<String> getFunctionSetupFile() {
    return Optional.empty();
  }

  @BeforeAll
  static void startDatabase() throws IOException {
    System.out.println("Starting embedded PostgreSQL...");
    postgres = EmbeddedPostgres.start();
    try {
      connection = postgres.getPostgresDatabase().getConnection();
      connection.setAutoCommit(false);
      System.out.println("PostgreSQL started successfully");
    } catch (SQLException e) {
      throw new RuntimeException("Failed to get database connection", e);
    }
  }

  @AfterAll
  static void stopDatabase() throws IOException {
    System.out.println("Stopping embedded PostgreSQL...");
    if (connection != null) {
      try {
        connection.close();
      } catch (SQLException e) {
        System.err.println("Error closing connection: " + e.getMessage());
      }
    }

    if (postgres != null) {
      postgres.close();
      System.out.println("PostgreSQL stopped successfully");
    }
  }

  @BeforeEach
  void setupSchema() throws SQLException, IOException {
    System.out.println("Setting up database schema and test data...");
    // Start a transaction that will be rolled back after each test
    connection.setAutoCommit(false);

    // Clear any existing data
    clearDatabase();

    // Initialize database schema
    getSchemaSetupFile().ifPresent(EmbeddedPostgreSqlSetupHelper::executeSqlFileRuntime);

    // Load test data
    getTestDataSetupFile().ifPresent(EmbeddedPostgreSqlSetupHelper::executeSqlFileRuntime);

    // Load functions
    getFunctionSetupFile().ifPresent(EmbeddedPostgreSqlSetupHelper::executeSqlFileRuntime);

    connection.commit();
    System.out.println("Setup complete");
  }

  protected static String getResourcePath(String resourceName) {
    // In a real application, you would use a resource loader
    // Here we're simplifying by using a relative path
    return "src/test/resources/" + resourceName;
  }

  private void clearDatabase() throws SQLException {
    try (Statement statement = connection.createStatement()) {
      statement.execute("DROP SCHEMA public CASCADE; CREATE SCHEMA public;");
    }
  }

  protected double convertToDouble(Object value) {
    if (value instanceof Number) {
      return ((Number) value).doubleValue();
    }
    return Double.parseDouble(value.toString());
  }

  protected int convertToInt(Object value) {
    if (value instanceof Number) {
      return ((Number) value).intValue();
    }
    return Integer.parseInt(value.toString());
  }

  private String mergeSolutionWithSetup(String setup, String solution) {
    return setup.replace(SOLUTION_MARKER, solution);
  }

  protected String readSolutionFile(String dir, String filename) throws IOException {
      String solution = Files.readString(Paths.get(getResourcePath(dir + filename + ".sql")));
      String setup = Files.readString(Paths.get(getResourcePath(dir + SETUP_DIR + filename + SETUP_ENDING + ".sql")));

      return mergeSolutionWithSetup(setup, solution);
  }

  protected void executeConcurrently(String firstTransactionSql, String secondTransactionSql)
    throws InterruptedException, SQLException, ExecutionException {
    System.out.println("Starting concurrent transactions");

    CountDownLatch firstTransactionStarted = new CountDownLatch(1);
    CountDownLatch secondTransactionFinished = new CountDownLatch(1);

    ExecutorService executor = Executors.newFixedThreadPool(2);

    Future<?> firstTask = executor.submit(() -> {
      System.out.println("First transaction: starting");
      try {
        try (Connection conn = postgres.getPostgresDatabase().getConnection()) {
          String[] parts = firstTransactionSql.split("--WAIT_HERE");

          System.out.println("First transaction: executing SQL");
          try (Statement stmt = conn.createStatement()) {
            stmt.execute(parts[0]);
          }
          System.out.println("First transaction: SQL executed, signaling start");
          firstTransactionStarted.countDown();

          System.out.println("First transaction: sleeping to allow second transaction to execute");
          Thread.sleep(THREAD_MAX_SLEEP_MILLIS);

          System.out.println("First transaction: awake, executing one more read");
          try (Statement stmt = conn.createStatement()) {
            stmt.execute(parts[1]);
          }

          System.out.println("First transaction: waiting for second transaction to finish");
          boolean finished = secondTransactionFinished.await(30, TimeUnit.SECONDS);
          if (!finished) {
            System.out.println("First transaction: timeout waiting for second transaction to finish");
          } else {
            System.out.println("First transaction: second transaction finished, committing");
          }
        } finally {
          System.out.println("First transaction: closing connection");
        }
      } catch (Exception cause) {
        System.out.println("Exception in first transaction");
        throw new RuntimeException("Exception in first transaction", cause);
      }
    });

    Future<?> secondTask = executor.submit(() -> {
      System.out.println("Second transaction: waiting for first transaction to start");
      try {
        boolean started = firstTransactionStarted.await(30, TimeUnit.SECONDS);
        if (!started) {
          System.out.println("Second transaction: timeout waiting for first transaction to start");
          return;
        }

        System.out.println("Second transaction: first transaction started, executing SQL");
        try (Connection conn = postgres.getPostgresDatabase().getConnection()) {
          System.out.println("Second transaction: executing SQL");
          try (Statement stmt = conn.createStatement()) {
            stmt.execute(secondTransactionSql);
          }
          System.out.println("Second transaction: SQL executed, committing");
        } finally {
          System.out.println("Second transaction: closing connection");
          System.out.println("Second transaction: signaling completion");
          secondTransactionFinished.countDown();
        }
      } catch (Exception e) {
        System.out.println("Exception in second transaction");
        secondTransactionFinished.countDown();
        throw new RuntimeException("Exception in second transaction", e);
      }
    });


    try {
      System.out.println("Waiting for both transactions to complete");
      firstTask.get(60, TimeUnit.SECONDS);
      secondTask.get(60, TimeUnit.SECONDS);
      System.out.println("Both transactions completed successfully");
    } catch (ExecutionException e) {
      System.out.println("Exception during transaction execution");
      throw new SQLException("Exception during transaction execution", e.getCause());
    } catch (TimeoutException e) {
      System.out.println("Timeout during transaction execution");
      throw new SQLException("Timeout during transaction execution", e);
    } finally {
      executor.shutdown();
      try {
        if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
          System.out.println("Thread pull not terminated, forcing shutdown");
          executor.shutdownNow();
        }
      } catch (InterruptedException e) {
        System.out.println("Terminating thread pool interrupted, forcing shutdown");
        executor.shutdownNow();
      }
    }
  }

  public static Throwable getRootCause(Exception throwable) {
    // Keep going until we find the root cause (an exception with no cause)
    Throwable cause = throwable;
    while (cause.getCause() != null && cause.getCause() != cause) {
      cause = cause.getCause();
    }
    return cause;
  }

  protected boolean executeFunction(String sql) throws SQLException {
    try (var statement = connection.createStatement();
         var rs = statement.executeQuery(sql)) {

      if (rs.next()) {
        return rs.getBoolean(1);
      }
    }

    return false;
  }

  protected boolean execute(String sql) throws SQLException {
    try (Statement statement = connection.createStatement()) {
      return statement.execute(sql);
    }
  }

  protected List<Map<String, Object>> executeQuery(String sql) throws SQLException {
    System.out.println("Checking the results of the solution");

    List<Map<String, Object>> results = new ArrayList<>();

    try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
      ResultSetMetaData metaData = resultSet.getMetaData();
      int columnCount = metaData.getColumnCount();

      List<String> columnNames = new ArrayList<>();
      int[] columnWidths = new int[columnCount];

      for (int i = 1; i <= columnCount; i++) {
        String name = metaData.getColumnLabel(i);
        columnNames.add(name);
        columnWidths[i - 1] = name.length();
      }

      while (resultSet.next()) {
        Map<String, Object> row = new HashMap<>();

        for (int i = 1; i <= columnCount; i++) {
          String columnName = columnNames.get(i - 1);
          Object value = resultSet.getObject(i);
          String valueStr = value == null ? "NULL" : value.toString();
          columnWidths[i - 1] = Math.max(columnWidths[i - 1], valueStr.length());
          row.put(columnName, value);
        }

        results.add(row);
      }

      printRow(columnNames, columnWidths);
      printSeparator(columnWidths);

      for (Map<String, Object> row : results) {
        List<String> values = new ArrayList<>();
        for (String col : columnNames) {
          Object value = row.get(col);
          values.add(value == null ? "NULL" : value.toString());
        }
        printRow(values, columnWidths);
      }

      System.out.println("\n-------------------------------------------------------\n");
    }

    return results;
  }

  private void printRow(List<String> values, int[] widths) {
    StringBuilder sb = new StringBuilder("|");
    for (int i = 0; i < values.size(); i++) {
      sb.append(" ").append(String.format("%-" + widths[i] + "s", values.get(i))).append(" |");
    }
    System.out.println(sb);
  }

  private void printSeparator(int[] widths) {
    StringBuilder sb = new StringBuilder("|");
    for (int width : widths) {
      sb.append("-".repeat(width + 2)).append("|");
    }
    System.out.println(sb);
  }

  private static void executeSqlFile(String fileName) throws IOException, SQLException {
    Path path = Paths.get(getResourcePath(fileName));
    if (path.toFile().exists()) {
      String sql = Files.readString(path).trim();
      try (Statement stmt = connection.createStatement()) {
        stmt.execute(sql);
      }
    }
  }

  private static void executeSqlFileRuntime(String fileName) {
    try {
      executeSqlFile(fileName);
    } catch (IOException | SQLException e) {
      throw new RuntimeException(e);
    }
  }
}