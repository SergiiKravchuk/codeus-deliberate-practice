package org.codeus.database.fundamentals.transaction_management.dao;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

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
//    executeSqlFile(getResourcePath(TEST_DATA_FILE));

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

    try (Statement statement = connection.createStatement()) {
      // Execute each statement separately
      for (String query : sql.split(";")) {
        if (!query.trim().isEmpty()) {
          statement.execute(query);
        }
      }
    }
  }

  protected String getResourcePath(String resourceName) {
    // In a real application, you would use a resource loader
    // Here we're simplifying by using a relative path
    return "src/test/resources/" + resourceName;
  }

//  @BeforeAll
//  static void startDatabase() throws IOException {
//    postgres = EmbeddedPostgres.start();
//    try {
//      connection = postgres.getPostgresDatabase().getConnection();
//    } catch (SQLException e) {
//      throw new RuntimeException("Failed to get database connection", e);
//    }
//  }
//
//  @AfterAll
//  static void stopDatabase() throws IOException {
//    if (connection != null) {
//      try {
//        connection.close();
//      } catch (SQLException e) {
//        // Log error
//      }
//    }
//
//    if (postgres != null) {
//      postgres.close();
//    }
//  }
//
//  @BeforeEach
//  void setupSchema() throws SQLException, IOException {
//    // Optional: Clear database before each test
//    // clearDatabase();
//
//    // Initialize database schema and test data
//    executeSqlFile("src/test/resources/schema.sql");
//    executeSqlFile("src/test/resources/test-data.sql");
//  }
//
//  private void clearDatabase() throws SQLException {
//    try (Statement statement = connection.createStatement()) {
//      // Drop all tables, views, etc.
//      statement.execute("DROP SCHEMA public CASCADE; CREATE SCHEMA public;");
//    }
//  }
//
//  protected void executeSqlFile(String filePath) throws IOException, SQLException {
//    Path path = Paths.get(filePath);
//    String sql = Files.readString(path);
//
//    try (Statement statement = connection.createStatement()) {
//      // Split by semicolon to execute multiple statements
//      for (String query : sql.split(";")) {
//        if (!query.trim().isEmpty()) {
//          statement.execute(query);
//        }
//      }
//    }
//  }
}
