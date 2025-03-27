package org.codeus.database.fundamentals.config;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public abstract class EmbeddedPostgreSqlModificationSetup {

    protected static EmbeddedPostgres postgres;
    protected static Connection connection;

    @BeforeAll
    protected static void startDatabase() throws IOException {
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
    protected static void stopDatabase() throws IOException {
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

    protected abstract void setupSchema(String schemaFileName, String dataFileName) throws SQLException, IOException;

    protected void clearDatabase() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("DROP SCHEMA public CASCADE; CREATE SCHEMA public;");
        }
    }

    protected void executeSqlFile(String filePath) throws IOException, SQLException {
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

    /**
     * Executes an SQL query from a file and returns the results.
     */
    protected List<Map<String, Object>> executeQueryFromFile(String filePath) throws IOException, SQLException {
        final String sql = getSqlRequestFromFilePath(filePath);

        String rowSeparator = "==============================";
        System.out.printf("Executing query:%n%s%n%s%s%n%n%n", rowSeparator, sql, rowSeparator);

        List<Map<String, Object>> result = executeQuery(sql);
        printQueryResults(result);
        return result;
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
     * Executes an SQL modification query (INSERT, UPDATE, DELETE) from a file.
     * @param filePath the path from source root
     *
     * @return either (1) the row count for SQL Data Manipulation Language (DML) statements
     *          or (2) 0 for SQL statements that return nothing
     */
    protected int executeModificationQueryFromFile(String filePath) throws IOException, SQLException {
        final String sql = getSqlRequestFromFilePath(filePath);
        String separator = "=================";
        System.out.printf("Executing modification query:%n%s%n%s%n%s%n%n", separator, sql, separator);

        return executeModificationQuery(sql);
    }

    /**
     * Get SQL command from a source root file
     * @param filePath a source root file like queries/01_01_insert_with_columns.sql
     * @return the SQL that should be executed
     */
    protected String getSqlRequestFromFilePath(String filePath) throws IOException {
        String fileFullPath = getResourcePath(filePath);
        return Files.readString(Paths.get(fileFullPath)).trim();
    }

    protected int executeModificationQuery(String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            return statement.executeUpdate(sql);
        }
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
