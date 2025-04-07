package org.codeus.database.common;

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

public abstract class EmbeddedPostgreSqlSetup {

  protected static EmbeddedPostgres postgres;
  protected static Connection connection;

  private static final String SCHEMA_FILE = "schema.sql";
  private static final String TEST_DATA_FILE = "test-data.sql";

  @BeforeAll
  static void startDatabase() throws IOException {
    System.out.println("Starting embedded PostgreSQL...");
    postgres = EmbeddedPostgres.start();
    try {
      connection = postgres.getPostgresDatabase().getConnection();
      connection.setAutoCommit(false); // For transaction control
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
    executeSqlFile(getResourcePath(SCHEMA_FILE));

    // Load test data
    executeSqlFile(getResourcePath(TEST_DATA_FILE));

    System.out.println("Setup complete");
  }

  private void clearDatabase() throws SQLException {
    try (Statement statement = connection.createStatement()) {
      statement.execute("DROP SCHEMA public CASCADE; CREATE SCHEMA public;");
    }
  }

  private void executeSqlFile(String filePath) throws IOException, SQLException {
    Path path = Paths.get(filePath);
    String sql = Files.readString(path);
    sql = filterOutCommentedLines(sql);
    try (Statement statement = connection.createStatement()) {
      // Execute each statement separately
      for (String query : sql.split(";")) {
        if (!query.trim().isEmpty()) {
          statement.execute(query);
        }
      }
    } catch (SQLException e) {
      connection.rollback();
    }
  }

  protected String getResourcePath(String resourceName) {
    // In a real application, you would use a resource loader
    // Here we're simplifying by using a relative path
    return "src/test/resources/" + resourceName;
  }

  /**
   * Executes an SQL query from a file and returns the results.
   */
  protected List<Map<String, Object>> executeQueryFromFile(String filePath) throws IOException, SQLException {
    String fileFullPath = getResourcePath(filePath);
    String sql = Files.readString(Paths.get(fileFullPath)).trim();

    System.out.printf("Executing query:%n%s%n%n", sql);

    List<Map<String, Object>> result = executeQuery(sql);
    printQueryResults(result);
    return result;
  }

  protected void executeQueriesFromFile(String filePath) throws IOException, SQLException {
    String fileFullPath = getResourcePath(filePath);
    executeSqlFile(fileFullPath);
  }

  private String filterOutCommentedLines(String sql) {
    StringBuilder builder = new StringBuilder();
    String commentOperator = "--";
    for (String line: sql.split("\n")) {
      if (!line.trim().startsWith(commentOperator)) {
        builder.append(line);
      }
    }
    return builder.toString();
  }

  /**
   * Executes an SQL query and returns the results as a list of maps.
   */
  protected List<Map<String, Object>> executeQuery(String sql) throws SQLException {
    List<Map<String, Object>> results = new ArrayList<>();

    try (Statement statement = connection.createStatement();
         ResultSet resultSet = statement.executeQuery(sql)) {
      ResultSetMetaData metaData = resultSet.getMetaData();
      int columnCount = metaData.getColumnCount();

      List<String> columnNames = new ArrayList<>();
      for (int i = 1; i <= columnCount; i++) {
        columnNames.add(metaData.getColumnLabel(i));
      }

      while (resultSet.next()) {
        Map<String, Object> row = new LinkedHashMap<>();
        for (int i = 1; i <= columnCount; i++) {
          row.put(columnNames.get(i - 1), resultSet.getObject(i));
        }
        results.add(row);
      }
    }

    return results;
  }

  /**
   * Prints query results in a formatted table.
   */
  protected void printQueryResults(List<Map<String, Object>> results) {
    if (results.isEmpty()) {
      System.out.println("No results found.");
      return;
    }

    // Extract column names from the first row
    List<String> columnNames = new ArrayList<>(results.get(0).keySet());

    // Calculate column widths
    int[] columnWidths = calculateColumnWidths(results, columnNames);

    // Print the header
    printRow(columnNames, columnWidths);
    printSeparator(columnWidths);

    // Print each row of results
    for (Map<String, Object> row : results) {
      List<String> values = new ArrayList<>();
      for (String col : columnNames) {
        Object value = row.get(col);
        values.add(value == null ? "NULL" : value.toString());
      }
      printRow(values, columnWidths);
    }

    System.out.println();
  }

  /**
   * Calculates the width needed for each column based on data and headers.
   */
  private int[] calculateColumnWidths(List<Map<String, Object>> results, List<String> columnNames) {
    int[] columnWidths = new int[columnNames.size()];

    // Initialize with header widths
    for (int i = 0; i < columnNames.size(); i++) {
      columnWidths[i] = columnNames.get(i).length();
    }

    // Update with data widths
    for (Map<String, Object> row : results) {
      for (int i = 0; i < columnNames.size(); i++) {
        String columnName = columnNames.get(i);
        Object value = row.get(columnName);
        String valueStr = value == null ? "NULL" : value.toString();
        columnWidths[i] = Math.max(columnWidths[i], valueStr.length());
      }
    }

    return columnWidths;
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
}