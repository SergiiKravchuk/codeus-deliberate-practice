package org.codeus.database.fundamentals.recap.setup;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.postgresql.ds.PGSimpleDataSource;
import org.postgresql.ds.common.BaseDataSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

public class ExternalPostgreSqlSetupHelper extends MetadataHolder {

  protected static Connection connection;
  protected static BaseDataSource postgres;

  @BeforeAll
  static void startDatabase() throws IOException {
    System.out.println("Connecting to PostgreSQL...");
    postgres = new PGSimpleDataSource();
    postgres.setUrl("jdbc:postgresql://localhost:5433/postgres");
    postgres.setUser("admin");
    postgres.setPassword("admin");

    try {
      connection = postgres.getConnection();
      connection.setAutoCommit(false); // For transaction control
      System.out.println("Successfully connected to PostgreSQL");
    } catch (SQLException e) {
      throw new RuntimeException("Failed to get database connection", e);
    }
  }

  @AfterAll
  static void stopDatabase() throws IOException {
    System.out.println("Closing connection to PostgreSQL...");
    if (connection != null) {
      try {
        connection.close();
      } catch (SQLException e) {
        System.err.println("Error closing connection: " + e.getMessage());
      }
    }
  }

  protected String getResourcePath(String resourceName) {
    // In a real application, you would use a resource loader
    // Here we're simplifying by using a relative path
    return "src/test/resources/" + resourceName;
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

  /**
   * Executes an SQL query and returns the results as a list of maps.
   */
  protected void executeQueryAndPrintResult(String sql) throws SQLException {
    List<Map<String, Object>> results = new ArrayList<>();

    try (Statement statement = connection.createStatement();
         ResultSet resultSet = statement.executeQuery(sql)) {
      ResultSetMetaData metaData = resultSet.getMetaData();
      int columnCount = metaData.getColumnCount();


      List<String> columnNames = new ArrayList<>();
      for (int i = 1; i <= columnCount; i++) {
        columnNames.add(metaData.getColumnLabel(i));
      }

      var numberOfResultToShow = 0;
      while (resultSet.next() && numberOfResultToShow <= 20) {
        Map<String, Object> row = new LinkedHashMap<>();
        for (int i = 1; i <= columnCount; i++) {
          row.put(columnNames.get(i - 1), resultSet.getObject(i));
        }
        results.add(row);
        numberOfResultToShow++;
      }
    }

    printQueryResults(results);
  }

  //TODO: align methods below with the EmbeddedPostgreSqlSetup.java to reduce duplication
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

    // Print the message header
    int borderLength = 120;
    System.out.println("-".repeat(borderLength));
    System.out.println("QUERY RESULT");
    System.out.println("-".repeat(borderLength));

    // Print the table header
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

    System.out.println("-".repeat(borderLength));
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
        columnWidths[i] = Math.max(columnWidths[i], getLargestSubstringWidth(valueStr));
      }
    }

    return columnWidths;
  }

  private int getLargestSubstringWidth(String string) {
    return Arrays.stream(string.split("\n")).map(String::length).max(Comparator.naturalOrder()).orElse(0);
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
