package org.codeus.fundamentals.advanced_join_and_subqueries;

import org.codeus.database.common.EmbeddedPostgreSqlSetup;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Base class for advanced SQL join and subquery tests.
 * Provides common infrastructure, utilities, and helper methods.
 */

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class BaseAdvancedJoinTest extends EmbeddedPostgreSqlSetup {

    protected static final String QUERIES_DIR = "src/test/resources/";
    protected List<Map<String, Object>> result = new ArrayList<>();
    protected String lastExecutedQuery = "";

    /**
     * Executes a SQL query from a file and returns the results.
     */
    protected List<Map<String, Object>> executeQueryFromFile(String fileName) throws IOException, SQLException {
        String fullPath = getResourcePath(fileName);
        String sqlContent = Files.readString(Paths.get(fullPath));

        // Store for debugging purposes
        lastExecutedQuery = sqlContent;

        // Clean the SQL content
        String cleanedSql = sqlContent.replaceAll("--.*", "").trim();
        if (cleanedSql.endsWith(";")) {
            cleanedSql = cleanedSql.substring(0, cleanedSql.length() - 1);
        }

        return executeQuery(cleanedSql);
    }

    /**
     * Executes a SQL query and returns results as a list of maps.
     */
    protected List<Map<String, Object>> executeQuery(String query) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnLabel(i).toLowerCase();
                    Object value = rs.getObject(i);
                    row.put(columnName, value);
                }
                result.add(row);
            }
        }
        return result;
    }

    /**
     * Extracts customer IDs from query results.
     */
    protected Set<Integer> extractCustomerIds(List<Map<String, Object>> result) {
        return result.stream()
                .map(row -> {
                    Object id = row.get("customer_id");
                    if (id == null) {
                        id = row.get("id");
                    }
                    if (id == null) {
                        // Try to find any numeric ID field
                        for (Map.Entry<String, Object> entry : row.entrySet()) {
                            if (entry.getKey().toLowerCase().contains("customer") &&
                                    entry.getValue() instanceof Number) {
                                return ((Number) entry.getValue()).intValue();
                            }
                        }
                    }
                    return id instanceof Number ? ((Number) id).intValue() : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * Extracts account IDs from query results.
     */
    protected Set<Integer> extractAccountIds(List<Map<String, Object>> result) {
        return result.stream()
                .map(row -> {
                    Object id = row.get("account_id");
                    if (id == null) {
                        // Try to find any account-related ID field
                        for (Map.Entry<String, Object> entry : row.entrySet()) {
                            if (entry.getKey().toLowerCase().contains("account") &&
                                    entry.getValue() instanceof Number) {
                                return ((Number) entry.getValue()).intValue();
                            }
                        }
                    }
                    return id instanceof Number ? ((Number) id).intValue() : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * Extracts email addresses from query results.
     */
    protected Set<String> extractEmails(List<Map<String, Object>> result) {
        return result.stream()
                .flatMap(row -> row.values().stream())
                .filter(Objects::nonNull)
                .map(Object::toString)
                .filter(val -> val.contains("@"))
                .collect(Collectors.toSet());
    }

    /**
     * Checks if the result row contains customer information fields.
     */
    protected boolean hasCustomerInfoField(Map<String, Object> row) {
        return row.keySet().stream().anyMatch(key ->
                key.toLowerCase().contains("customer") ||
                        key.toLowerCase().contains("name") ||
                        key.toLowerCase().contains("email"));
    }

    /**
     * Checks if the result row contains customer details fields.
     */
    protected boolean hasCustomerDetailsField(Map<String, Object> row) {
        return row.keySet().stream().anyMatch(key ->
                key.toLowerCase().contains("name") ||
                        key.toLowerCase().contains("email") ||
                        key.toLowerCase().contains("first_name") ||
                        key.toLowerCase().contains("customer"));
    }

    /**
     * Checks if the result row contains loan-related fields.
     */
    protected boolean hasLoanRelatedFields(Map<String, Object> row) {
        return row.keySet().stream().anyMatch(key ->
                key.toLowerCase().contains("loan"));
    }

    /**
     * Checks if the result row contains valid email field.
     */
    protected boolean hasValidEmailField(Map<String, Object> row) {
        return row.values().stream().anyMatch(val ->
                val != null && val.toString().contains("@"));
    }

    /**
     * Converts a ResultSet to a list of maps.
     */
    protected List<Map<String, Object>> resultSetToMapList(ResultSet rs) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        while (rs.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnLabel(i).toLowerCase();
                Object value = rs.getObject(i);
                row.put(columnName, value);
            }
            result.add(row);
        }

        return result;
    }

    /**
     * Gets the resource path for SQL files.
     */
    protected String getResourcePath(String fileName) {
        return QUERIES_DIR + fileName;
    }

    /**
     * Returns the last executed query for debugging purposes.
     */
    protected String getLastExecutedQuery() {
        return lastExecutedQuery;
    }

    /**
     * Subclasses can override this to provide custom validation logic.
     */
    protected void validateBasicResultStructure(List<Map<String, Object>> results, String testName) {

        if (!results.isEmpty()) {
            System.out.println(testName + " returned " + results.size() + " results");
        }
    }
}