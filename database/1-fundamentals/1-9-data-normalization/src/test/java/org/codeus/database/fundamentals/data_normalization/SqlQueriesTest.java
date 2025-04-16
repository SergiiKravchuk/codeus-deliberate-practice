package org.codeus.database.fundamentals.data_normalization;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class SqlQueriesTest {

    protected static EmbeddedPostgres postgres;
    protected static Connection connection;

    @BeforeAll
    static void beforeAll() throws IOException {
        startDatabase();
    }

    @AfterAll
    static void afterAll() throws IOException {
        stopDatabase();
    }

    static void startDatabase() throws IOException {
        postgres = EmbeddedPostgres.builder()
                .setPort(5434)
                .start();
        try {
            connection = postgres.getPostgresDatabase().getConnection();
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get database connection", e);
        }
    }

    static void stopDatabase() throws IOException {
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


    @Nested
    @DisplayName("1NF: ")
    @Order(1)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class FirstNF {

        @Test
        @Order(1)
        @DisplayName("1.1: Normalization with ROW result independence")
        void shouldNormalizeToFirstNF() throws SQLException, IOException {
            System.out.println("1.1: Normalization with ROW result independence\n");

            executeSqlFile("1NF/1.1_task_modify_table_to_1nf_row_independency.sql");

            List<Map<String, Object>> tableColumnsD = executeQuery(
                    "SELECT column_name, data_type FROM information_schema.columns " +
                            "WHERE table_name = 'customers_denormalized_order'"
            );
            for (final String column : new String[]{"customer_name", "customer_contact"}) {
                boolean found = tableColumnsD.stream()
                        .anyMatch(row -> column.equals(row.get("column_name")));

                assertTrue(found, "Expected column '" + column + "' was not found in customers_denormalized_order table");
            }

            List<Map<String, Object>> tableColumns = executeQuery(
                    "SELECT column_name, data_type FROM information_schema.columns " +
                            "WHERE table_name = 'customers_normalized_order'"
            );
            for (final String column : new String[]{"customer_name", "customer_contact", "reliability_score"}) {
                boolean found = tableColumns.stream()
                        .anyMatch(row -> column.equals(row.get("column_name")));

                assertTrue(found, "Expected column '" + column + "' was not found in customers_normalized_order table");
            }


            executeSqlFile("1NF/data/_1.1_denormalization_data.sql");
            // Check that millions of rows have been generated
            List<Map<String, Object>> rowCount = executeQuery("SELECT COUNT(*) as count FROM customers_denormalized_order");
            long count = ((Number) rowCount.get(0).get("count")).longValue();
            assertTrue(count >= 100_000, "Expected at least 100,000 rows to be generated, but found " + count);

            // This part has the representation of changes
            System.out.println("First 5 customers of DENORMALIZED schema");
            List<Map<String, Object>> selectTop5Denormalized = executeQuery("select * from customers_denormalized_order limit 5;");
            printQueryResults(selectTop5Denormalized);


            executeSqlFile("1NF/data/_1.1_normalization_row_order_data.sql");
            // Check that millions of rows have been generated
            System.out.println("First 5 customers of NORMALIZED schema");
            List<Map<String, Object>> selectTop5Normalized = executeQuery("select * from customers_normalized_order limit 5;");
            printQueryResults(selectTop5Normalized);

            // This part have some statistical representation, of data size and rows
            System.out.println("Lets see DENORMALIZED table size:");
            printTableSize("customers_denormalized_order");

            System.out.println("Lets see NORMALIZED ROW independency table size:");
            printTableSize("customers_normalized_order");

            System.out.println("""
                    -- After adding the `reliability_score` column, we no longer depend on the ROW order as an indicator
                    -- of the customer's reliability score and can explicitly see it whenever we need.
                    --
                    -- With COLUMN order the same principle applies - we must not depend on it!
                    --
                    -- Also, you can see that after adding such a column, the table size becomes larger - this is the
                    -- trade-off that we pay for this normalization.
                    """);

            System.out.println("=========================================================================================================================================================================\n");
        }

        @Test
        @Order(5)
        @DisplayName("1.2: Support Entity integrity with Primary Key")
        void addPK() throws SQLException, IOException {
            System.out.println("1.2: Support Entity integrity with Primary Key\n");

            executeSqlFile("1NF/1.2_task_modify_table_to_1nf_PK.sql");
            List<Map<String, Object>> tableColumns = executeQuery(
                    "SELECT column_name, data_type FROM information_schema.columns " +
                            "WHERE table_name = 'customers_normalized_order'"
            );
            for (final String column : new String[]{"customer_name", "customer_contact", "reliability_score"}) {
                boolean found = tableColumns.stream()
                        .anyMatch(row -> column.equals(row.get("column_name")));

                assertTrue(found, "Expected column '" + column + "' was not found in customers_normalized_order table");
            }

            List<Map<String, Object>> tableColumnsPK = executeQuery(
                    "SELECT column_name, data_type FROM information_schema.columns " +
                            "WHERE table_name = 'customers_normalized_order_with_pk'"
            );
            for (final String column : new String[]{"ssn", "customer_name", "customer_contact", "reliability_score"}) {
                boolean found = tableColumnsPK.stream()
                        .anyMatch(row -> column.equals(row.get("column_name")));

                assertTrue(found, "Expected column '" + column + "' was not found in customers_normalized_order_with_pk table");
            }

            List<Map<String, Object>> tableColumnPK = executeQuery(
                    """
                            SELECT
                                kcu.column_name as cn
                            FROM
                                information_schema.table_constraints tc
                                JOIN information_schema.key_column_usage kcu
                                  ON tc.constraint_name = kcu.constraint_name
                                  AND tc.table_schema = kcu.table_schema
                            WHERE
                                tc.constraint_type = 'PRIMARY KEY'
                                AND tc.table_name = 'customers_normalized_order_with_pk'
                                AND kcu.column_name = 'ssn';
                            """
            );

            assertTrue(tableColumnPK.get(0).get("cn") != null &&
                            tableColumnPK.get(0).get("cn").equals("ssn"),
                    "Expected column 'ssn' was not marked as Primary Key in customers_normalized_order_with_pk table"
            );

            executeSqlFile("1NF/data/_1.2_normalization_entity_integrity_pk.sql");

            System.out.println("First 5 customers of NORMALIZED schema with Entity integrity via Primary Key");
            List<Map<String, Object>> selectTop5Normalized = executeQuery("select * from customers_normalized_order_with_pk limit 5;");
            printQueryResults(selectTop5Normalized);

            System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------");

            System.out.println("Lets check possibility of rows duplications:");
            String oldTableInsert = """
                    INSERT INTO customers_normalized_order (customer_name, customer_contact, reliability_score)
                    VALUES ('Roman Svystun', 'Personal Email: romka@gmail.com, Personal Phone: 4308441611', 'vip');
                    """;
            System.out.println("x2:" + oldTableInsert);
            executeCUD(oldTableInsert);
            executeCUD(oldTableInsert);

            String selectFromTable = "SELECT * FROM %s WHERE customer_name = 'Roman Svystun';";

            String oldTableSelect = selectFromTable.formatted("customers_normalized_order");
            System.out.println(oldTableSelect);
            printQueryResults(executeQuery(oldTableSelect));

            String newTableInsert = """
                    INSERT INTO customers_normalized_order_with_pk (ssn, customer_name, customer_contact, reliability_score)
                    VALUES (239902239, 'Roman Svystun', 'Personal Email: romka@gmail.com, Personal Phone: 4308441611', 'vip');
                    """;
            System.out.println("x2:" + newTableInsert);
            executeCUD(newTableInsert);
            connection.commit();
            try {
                executeCUD(newTableInsert);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage() + "\n");
                connection.rollback();
            }

            String newTableSelect = selectFromTable.formatted("customers_normalized_order_with_pk");

            System.out.println(newTableSelect);
            printQueryResults(executeQuery(newTableSelect));

            System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------");

            // This part have some statistical representation, of data size and rows
            System.out.println("Lets see DENORMALIZED Entity integrity table size:");
            printTableSize("customers_normalized_order");

            System.out.println("Lets see NORMALIZED Entity integrity table size:");
            printTableSize("customers_normalized_order_with_pk");

            System.out.println("""
                    -- After adding the SSN as a Primary Key, we gain several benefits that support 1NF principles:
                    -- 1. Entity integrity is enforced - the database now prevents duplicate customers through the PK constraint,
                    --    as shown in the failed INSERT attempt with the same SSN.
                    -- 2. We can now reliably identify each customer even when they share the same name - the SSN provides
                    --    a guaranteed unique identifier that distinguishes between homonyms.
                    -- 3. The PK creates an index which improves query performance when searching for specific customers.
                    -- 4. The total table increased in size, which is another trade-off we accept for proper
                    --    normalization and data integrity. This overhead is justified by the benefits gained.
                    --
                    -- In 1NF, having a primary key is a fundamental requirement as it ensures each row represents a unique
                    -- entity and eliminates row-order dependency, allowing for reliable data access and manipulation.
                    """);

            System.out.println("=========================================================================================================================================================================\n");
        }

        @Test
        @Order(10)
        @DisplayName("1.3.1: Remove repeating groups")
        void replaceGroups() throws SQLException, IOException {
            System.out.println("1.3.1: Remove repeating groups\n");

            executeSqlFile("1NF/1.3.1_task_modify_table_to_1nf_remove_repeating_group.sql");
            List<Map<String, Object>> oldTable = executeQuery(
                    "SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'customers_normalized_order_with_pk'"
            );
            for (final String column : new String[]{"ssn", "customer_name", "customer_contact", "reliability_score"}) {
                boolean found = oldTable.stream().anyMatch(row -> column.equals(row.get("column_name")));
                assertTrue(found, "Expected column '" + column + "' was not found in customers_normalized_order_with_pk table");
            }

            List<Map<String, Object>> newTable = executeQuery(
                    "SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'customers_remove_groups'"
            );
            for (final String column : new String[]{"ssn", "customer_name", "personal_phone", "work_phone", "personal_email", "work_email", "reliability_score"}) {
                boolean found = newTable.stream().anyMatch(row -> column.equals(row.get("column_name")));
                assertTrue(found, "Expected column '" + column + "' was not found in customers_remove_groups table");
            }

            executeSqlFile("1NF/data/_1.3.1_normalization_remove_repeating_groups.sql");

            System.out.println("First 5 customers of NORMALIZED schema with Entity integrity via Primary Key");
            List<Map<String, Object>> selectTop5Normalized = executeQuery("select * from customers_normalized_order_with_pk limit 5;");
            printQueryResults(selectTop5Normalized);

            System.out.println("First 5 customers of NORMALIZED schema with Entity integrity via Primary Key");
            List<Map<String, Object>> selectTop5RemovedGroups = executeQuery("select * from customers_remove_groups limit 5;");
            printQueryResults(selectTop5RemovedGroups);

            // This part have some statistical representation, of data size and rows
            System.out.println("Lets see DENORMALIZED Repeated groups size:");
            printTableSize("customers_normalized_order_with_pk");

            System.out.println("Lets see NORMALIZED Repeated groups size:");
            printTableSize("customers_remove_groups");

            System.out.println("""
                    -- After separating the concatenated `customer_contact` to several columns, we've almost fulfill
                    -- one of the key requirements of 1NF to remove repeating groups.
                    --
                    -- However, this is not yet the complete solution for repeating groups. This approach:
                    -- 1. Makes data access and updates much more straightforward - finding and updating just a work phone
                    --    is now a simple column update rather than string manipulation.
                    -- 2. Improves data quality by enforcing consistent structure for contact information.
                    -- 3. Facilitates validation of individual contact fields (e.g., email format validation).
                    -- 4. Interestingly, the total table size been reduced and the actual table data and indexes as well,
                    --    showing that structured data can be more storage-efficient.
                    --
                    -- However, this solution still doesn't fully follow the 1NF because we're limiting each person to
                    -- exactly one personal phone, one work phone, etc. The next task will address this limitation by
                    -- properly modeling the one-to-many relationship between customers and their contact methods.
                    """);

            System.out.println("=========================================================================================================================================================================\n");
        }

        @Test
        @Order(15)
        @DisplayName("1.3.2: Properly remove repeating groups")
        void removeGroups() throws SQLException, IOException {
            System.out.println("1.3.2: Properly remove repeating groups\n");

            executeSqlFile("1NF/1.3.2_task_modify_table_to_1nf_correct_remove_repeating_group.sql");
            List<Map<String, Object>> oldTable = executeQuery(
                    "SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'customers_normalized_order_with_pk'"
            );
            for (final String column : new String[]{"ssn", "customer_name", "customer_contact", "reliability_score"}) {
                boolean found = oldTable.stream().anyMatch(row -> column.equals(row.get("column_name")));
                assertTrue(found, "Expected column '" + column + "' was not found in customers_normalized_order_with_pk table");
            }

            List<Map<String, Object>> newTable = executeQuery(
                    "SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'customers_1nf'"
            );
            for (final String column : new String[]{"ssn", "customer_name", "contact_type", "contact_value", "reliability_score"}) {
                boolean found = newTable.stream().anyMatch(row -> column.equals(row.get("column_name")));
                assertTrue(found, "Expected column '" + column + "' was not found in customers_1nf table");
            }

            executeSqlFile("1NF/data/_1.3.2_normalization_correct_remove_repeating_groups.sql");

            System.out.println("First 5 customers of NORMALIZED schema with Entity integrity via Primary Key");
            List<Map<String, Object>> selectTop5Normalized = executeQuery("select * from customers_normalized_order_with_pk limit 5;");
            printQueryResults(selectTop5Normalized);

            String ssns = selectTop5Normalized.stream()
                    .map(row -> row.get("ssn"))
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));

            System.out.println("First 5 customers of NORMALIZED schema with Entity integrity via Primary Key");
            List<Map<String, Object>> selectTop5RemovedGroups = executeQuery("select * from customers_1nf where ssn in (%s);".formatted(ssns));
            printQueryResults(selectTop5RemovedGroups);

            // This part have some statistical representation, of data size and rows
            System.out.println("Lets see DENORMALIZED Repeated groups size:");
            printTableSize("customers_normalized_order_with_pk");

            System.out.println("Lets see NORMALIZED Repeated groups size:");
            printTableSize("customers_1nf");

            System.out.println("""
                    -- This solution properly implements 1NF by:
                    -- 1. Eliminating repeating groups - each contact is now a separate row instead of being combined
                    --    in a single field or restricted to predefined columns.
                    -- 2. Using a composite primary key (ssn, contact_type) that properly identifies each piece of contact information.
                    -- 3. Providing flexibility to add any contact type without schema changes.
                    -- 4. The trade-off is increased storage, and more rows but we gain data integrity, flexibility, and maintainability.
                    --
                    -- Now our table fully satisfies all 1NF requirements.
                    -- ˚ ༘ ೀ⋆｡˚ ✧⸜(｡˃ ᵕ ˂ )⸝♡ ˚ ༘ ೀ⋆｡˚
                    """);

            System.out.println("=========================================================================================================================================================================\n");
        }

        @Test
        @Order(20)
        @DisplayName("Optional 1.4: SELECT queries without 1NF and with 1NF")
        void shouldSelectFromBothTables() throws SQLException, IOException {
            System.out.println("Optional 1.4: SELECT queries without 1NF and with 1NF\n");

            // Check if customers_1nf table exists
            List<Map<String, Object>> tableExists = executeQuery(
                    "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'customers_1nf') as exists"
            );
            assertTrue((Boolean) tableExists.get(0).get("exists"), "Table customers_1nf doesn't exist. Please create it first.");

            List<String> sqlQueries = getSqlQueries("1NF/opt_1.4_task_selectivity_from_tables.sql");

            // Execute inserts
            executeInserts(sqlQueries);

            // Check that third query is select from customers_normalized_order_with_pk + execute
            assertTrue(sqlQueries.size() >= 3, "Please provide the query to select the 'Personal Phone' in table 'customers_normalized_order_with_pk'!");
            assertTrue(sqlQueries.get(2).toLowerCase().contains("select") &&
                            sqlQueries.get(2).toLowerCase().contains("customers_normalized_order_with_pk"),
                    "The 3rd query should be select from customers_normalized_order_with_pk, got: " + sqlQueries.get(2)
            );
            List<Map<String, Object>> firstSelectRes = executeQuery(sqlQueries.get(2), true);
            printQueryResults(firstSelectRes);
            assertEquals("+2708291611", firstSelectRes.get(0).get("personal_phone"),
                    "Select to `customers_normalized_order_with_pk` should return only 'personal_phone' with number '+2708291611'."
            );


            // Check that forth query is select from customers_1nf + execute
            assertTrue(sqlQueries.size() >= 4, "Please provide the query to select the 'Personal Phone' in table 'customers_1nf'!");
            assertTrue(sqlQueries.get(3).toLowerCase().contains("select") &&
                            sqlQueries.get(3).toLowerCase().contains("customers_1nf"),
                    "The 3rd query should be select from customers_1nf, got: " + sqlQueries.get(3)
            );
            List<Map<String, Object>> secondSelectRes = executeQuery(sqlQueries.get(3), true);
            printQueryResults(secondSelectRes);
            assertEquals("+2708291611", secondSelectRes.get(0).get("contact_value"),
                    "Select to `customers_1nf` should return only 'personal_phone' with number '+2708291611'"
            );

            System.out.println("=========================================================================================================================================================================\n");
        }

        @Test
        @Order(25)
        @DisplayName("Optional 1.5: UPDATE queries without 1NF and with 1NF")
        void shouldUpdateFromBothTables() throws SQLException, IOException {
            System.out.println("Optional 1.5: UPDATE queries without 1NF and with 1NF\n");

            // Check if customers_1nf table exists
            List<Map<String, Object>> tableExists = executeQuery(
                    "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'customers_1nf') as exists"
            );
            assertTrue((Boolean) tableExists.get(0).get("exists"), "Table customers_1nf doesn't exist. Please create it first.");

            List<String> sqlQueries = getSqlQueries("1NF/opt_1.5_task_data_modifications.sql");

            // Execute inserts
            executeInserts(sqlQueries);

            // Test if student made the updates queries
            assertTrue(sqlQueries.size() >= 4, "Please provide the UPDATE queries for both tables!");

            // Check update queries
            assertTrue(sqlQueries.get(2).toLowerCase().contains("update") &&
                            sqlQueries.get(2).toLowerCase().contains("customers_normalized_order_with_pk"),
                    "The 3rd query should be an UPDATE on customers_normalized_order_with_pk table"
            );

            assertTrue(sqlQueries.get(3).toLowerCase().contains("update") &&
                            sqlQueries.get(3).toLowerCase().contains("customers_1nf"),
                    "The 4th query should be an UPDATE on customers_1nf table"
            );

            // Execute update queries
            executeCUD(sqlQueries.get(2), true);
            executeCUD(sqlQueries.get(3), true);

            // Verify updates in denormalized table
            List<Map<String, Object>> denormalizedResults = executeQuery(
                    "SELECT SUBSTRING(customer_contact FROM 'Work Email: ([^,]+)') AS work_email " +
                            "FROM customers_normalized_order_with_pk WHERE customer_name = 'Ivan Kozhumyaka'"
            );
            assertEquals("ivan_kozhumyaka@codeus.com", denormalizedResults.get(0).get("work_email"),
                    "Update to customers_normalized_order_with_pk failed. Expected work email to be 'ivan_kozhumyaka@codeus.com'"
            );


            // Verify updates in normalized table
            List<Map<String, Object>> normalizedResults = executeQuery(
                    "SELECT contact_value FROM customers_1nf " +
                            "WHERE customer_name = 'Ivan Kozhumyaka' AND contact_type = 'Work Email'"
            );
            assertEquals("ivan_kozhumyaka@codeus.com", normalizedResults.get(0).get("contact_value"),
                    "Update to customers_1nf failed. Expected contact_value to be 'ivan_kozhumyaka@codeus.com'"
            );

            System.out.println("Update operations successful!");
            System.out.println("Denormalized table after update:");
            printQueryResults(executeQuery("SELECT * FROM customers_normalized_order_with_pk WHERE customer_name = 'Ivan Kozhumyaka'"));

            System.out.println("Normalized table after update:");
            printQueryResults(executeQuery("SELECT * FROM customers_1nf WHERE customer_name = 'Ivan Kozhumyaka'"));

            System.out.println("=========================================================================================================================================================================\n");
        }

        @Test
        @Order(30)
        @DisplayName("Optional 1.6: DELETE queries without 1NF and with 1NF")
        void shouldDeleteFromBothTables() throws SQLException, IOException {
            System.out.println("Optional 1.6: DELETE queries without 1NF and with 1NF\n");

            // Check if customers_1nf table exists
            List<Map<String, Object>> tableExists = executeQuery(
                    "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'customers_1nf') as exists"
            );
            assertTrue((Boolean) tableExists.get(0).get("exists"), "Table customers_1nf doesn't exist. Please create it first.");

            List<String> sqlQueries = getSqlQueries("1NF/opt_1.6_task_data_deletion.sql");

            // Execute inserts
            executeInserts(sqlQueries);

            // Test if student made the update/delete queries
            assertTrue(sqlQueries.size() >= 4, "Please provide the UPDATE/DELETE queries for both tables!");

            // Check update query for denormalized table (using UPDATE with empty string replacement)
            assertTrue(sqlQueries.get(2).toLowerCase().contains("update") &&
                            sqlQueries.get(2).toLowerCase().contains("customers_normalized_order_with_pk") &&
                            sqlQueries.get(2).toLowerCase().contains("''"),
                    "The 3rd query should be an UPDATE on customers_normalized_order_with_pk table that removes the email"
            );

            // Check delete query for normalized table
            assertTrue(sqlQueries.get(3).toLowerCase().contains("delete") &&
                            sqlQueries.get(3).toLowerCase().contains("customers_1nf"),
                    "The 4th query should be a DELETE on customers_1nf table"
            );

            // Execute update/delete queries
            executeCUD(sqlQueries.get(2), true);
            executeCUD(sqlQueries.get(3), true);

            // Verify updates in denormalized table - work email should be removed
            List<Map<String, Object>> denormalizedResults = executeQuery(
                    "SELECT customer_contact FROM customers_normalized_order_with_pk WHERE customer_name = 'Ruslan Zalizyaka'"
            );
            String contactInfo = (String) denormalizedResults.get(0).get("customer_contact");
            assertFalse(contactInfo.contains("Work Email: ruslan_zalizyaka@abc.com"),
                    "Work Email should be removed from customer_contact in the denormalized table"
            );

            long comasAmount = contactInfo.chars().filter(ch -> ch == ',').count();
            assertFalse(comasAmount == 2,
                    """
                            
                            Huh, caught you. There are the HUGE headache with these strings. You've removed 'Work email' but the coma is left there.
                            Now, you would probably correct your regex to 'Work Email: ruslan_zalizyaka@abc.com, ' but remember that this tiny part
                            holds a lot of edge cases, like: [last contact in a row, first contact in a row, etc...].
                            For now You can add just coma to pass this test but remember that in real life you need to handle these all cases.
                            The `customer_contact` now: '%s'
                            """.formatted(contactInfo)
            );


            // Verify deletion in normalized table - work email entry should not exist
            List<Map<String, Object>> normalizedResults = executeQuery(
                    "SELECT COUNT(*) as count FROM customers_1nf " +
                            "WHERE customer_name = 'Ruslan Zalizyaka' AND contact_type = 'Work Email'"
            );

            long count = ((Number) normalizedResults.get(0).get("count")).longValue();
            assertEquals(0, count, "Work Email entry should be deleted from customers_1nf table");

            System.out.println("Delete operations successful!");
            System.out.println("Denormalized table after delete operation:");
            printQueryResults(executeQuery("SELECT * FROM customers_normalized_order_with_pk WHERE customer_name = 'Ruslan Zalizyaka'"));

            System.out.println("Normalized table after delete operation (remaining contacts):");
            printQueryResults(executeQuery("SELECT * FROM customers_1nf WHERE customer_name = 'Ruslan Zalizyaka'"));

            System.out.println("=========================================================================================================================================================================\n");
        }

        @Test
        @Order(35)
        @DisplayName("Optional 1.7: Constraints for data deduplication")
        void deduplicationFromBothTables() throws SQLException, IOException {
            System.out.println("Optional 1.7: Constraints for data deduplication\n");
            // Check if customers_1nf table exists

            List<Map<String, Object>> tableExists = executeQuery(
                    "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'customers_1nf') as exists"
            );
            assertTrue((Boolean) tableExists.get(0).get("exists"), "Table customers_1nf doesn't exist. Please create it first.");

            // Reset tables and recreate constraints
            executeCUD("TRUNCATE TABLE customers_normalized_order_with_pk CASCADE");
            executeCUD("TRUNCATE TABLE customers_1nf CASCADE");

            // Apply constraints from the student solution
            executeSqlFile("1NF/opt_1.7_task_contacts_deduplication.sql");

            // Verify constraint exists on customers_1nf
            List<Map<String, Object>> constraintExists = executeQuery(
                    "SELECT count(*) as count FROM information_schema.constraint_column_usage " +
                            "WHERE table_name = 'customers_1nf' AND column_name = 'contact_value' " +
                            "AND constraint_name LIKE '%unique%'"
            );
            long constraintCount = ((Number) constraintExists.get(0).get("count")).longValue();
            assertTrue(constraintCount > 0, "Expected UNIQUE constraint on contact_value column in customers_1nf table");

            // Insert initial data
            executeCUD("INSERT INTO customers_normalized_order_with_pk (ssn, customer_name, customer_contact, reliability_score) " +
                    "VALUES (111111111, 'Roman Svystun', 'Personal Email: roman@gmail.com, Personal Phone: +12345678901', 'reliable')");

            executeCUD("INSERT INTO customers_1nf (ssn, customer_name, contact_type, contact_value, reliability_score) " +
                    "VALUES (111111111, 'Roman Svystun', 'Personal Email', 'roman@gmail.com', 'reliable'), " +
                    "(111111111, 'Roman Svystun', 'Personal Phone', '+12345678901', 'reliable')");

            // Commit these changes
            connection.commit();

            System.out.println("Initial data inserted:");
            printQueryResults(executeQuery("SELECT * FROM customers_normalized_order_with_pk"));
            printQueryResults(executeQuery("SELECT * FROM customers_1nf"));

            // Test 1: Try to insert a duplicate email in customers_1nf
            System.out.println("\nTesting constraint on customers_1nf:");
            String duplicateEmailInsert = "INSERT INTO customers_1nf (ssn, customer_name, contact_type, contact_value, reliability_score) " +
                    "VALUES (222222222, 'John Doe', 'Personal Email', 'roman@gmail.com', 'reliable')";

            System.out.println("Trying to insert: " + duplicateEmailInsert);
            try {
                executeCUD(duplicateEmailInsert);
                connection.commit();
                fail("Expected exception when inserting duplicate email in customers_1nf");
            } catch (Exception e) {
                System.out.println("Got expected error: " + e.getMessage());
                connection.rollback();
            }

            // Test 2: Try to insert a duplicate phone in customers_1nf
            String duplicatePhoneInsert = "INSERT INTO customers_1nf (ssn, customer_name, contact_type, contact_value, reliability_score) " +
                    "VALUES (222222222, 'John Doe', 'Personal Phone', '+12345678901', 'reliable')";

            System.out.println("Trying to insert: " + duplicatePhoneInsert);
            try {
                executeCUD(duplicatePhoneInsert);
                connection.commit();
                fail("Expected exception when inserting duplicate phone in customers_1nf");
            } catch (Exception e) {
                System.out.println("Got expected error: " + e.getMessage());
                connection.rollback();
            }

            // Test 3: Try to insert a duplicate email in customers_normalized_order_with_pk
            System.out.println("\nTesting trigger on customers_normalized_order_with_pk:");
            String duplicateEmailInDenormalized = "INSERT INTO customers_normalized_order_with_pk (ssn, customer_name, customer_contact, reliability_score) " +
                    "VALUES (222222222, 'John Doe', 'Personal Email: roman@gmail.com, Work Phone: +19876543210', 'reliable')";

            System.out.println("Trying to insert: " + duplicateEmailInDenormalized);
            try {
                executeCUD(duplicateEmailInDenormalized);
                connection.commit();
                fail("Expected exception when inserting duplicate email in customers_normalized_order_with_pk");
            } catch (Exception e) {
                System.out.println("Got expected error: " + e.getMessage());
                connection.rollback();
            }

            // Test 4: Try to insert a duplicate phone in customers_normalized_order_with_pk
            String duplicatePhoneInDenormalized = "INSERT INTO customers_normalized_order_with_pk (ssn, customer_name, customer_contact, reliability_score) " +
                    "VALUES (222222222, 'John Doe', 'Work Email: john@gmail.com, Personal Phone: +12345678901', 'reliable')";

            System.out.println("Trying to insert: " + duplicatePhoneInDenormalized);
            try {
                executeCUD(duplicatePhoneInDenormalized);
                connection.commit();
                fail("Expected exception when inserting duplicate phone in customers_normalized_order_with_pk");
            } catch (Exception e) {
                System.out.println("Got expected error: " + e.getMessage());
                connection.rollback();
            }

            // Test 5: Test that we can insert non-duplicate values
            System.out.println("\nTesting valid insertions:");
            String validInsert1 = "INSERT INTO customers_1nf (ssn, customer_name, contact_type, contact_value, reliability_score) " +
                    "VALUES (222222222, 'John Doe', 'Personal Email', 'john@gmail.com', 'reliable')";

            String validInsert2 = "INSERT INTO customers_normalized_order_with_pk (ssn, customer_name, customer_contact, reliability_score) " +
                    "VALUES (333333333, 'Jane Smith', 'Personal Email: jane@gmail.com, Personal Phone: +13456789012', 'reliable')";

            System.out.println("Trying valid inserts...");
            executeCUD(validInsert1);
            executeCUD(validInsert2);
            connection.commit();

            System.out.println("Final state of tables after testing:");
            printQueryResults(executeQuery("SELECT * FROM customers_normalized_order_with_pk"));
            printQueryResults(executeQuery("SELECT * FROM customers_1nf"));

            System.out.println("=========================================================================================================================================================================\n");
        }

        private void assertFalse(boolean condition, String message) {
            if (condition) {
                fail(message);
            }
        }

        private void executeInserts(List<String> sqlQueries) throws SQLException {
            // Check that first query is insert into customers_normalized_order_with_pk + execute
            assertTrue(sqlQueries.size() >= 2, "File should have at least 2 queries!");
            assertTrue(sqlQueries.get(0).toLowerCase().contains("insert") &&
                            sqlQueries.get(0).toLowerCase().contains("customers_normalized_order_with_pk"),
                    "The 1st query should be `insert into customers_normalized_order_with_pk`, got: " + sqlQueries.get(0)
            );
            executeCUD(sqlQueries.get(0));

            // Check that second query is insert into customers_1nf + execute
            assertTrue(sqlQueries.get(1).toLowerCase().contains("insert") &&
                            sqlQueries.get(1).toLowerCase().contains("customers_1nf"),
                    "The 2nd query should be `insert into customers_1nf`, got: " + sqlQueries.get(1)
            );
            executeCUD(sqlQueries.get(1));
        }
    }

    @Nested
    @DisplayName("2NF: ")
    @Order(5)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class SecondNF {

        @Test
        @Order(1)
        @DisplayName("2.1: Deletion anomaly")
        void deletionAnomaly() throws SQLException, IOException {
            System.out.println("2.1: Deletion anomaly\n");

            executeSqlFile("2NF/data/_2.1_denormalization_data.sql");

            List<String> sqlQueries = getSqlQueries("2NF/2.1_deletion_anomaly.sql");
            assertTrue(sqlQueries.size() >= 2, "Not implemented yet!");
            assertFalse(
                    sqlQueries.get(0).toLowerCase().startsWith("delete"),
                    "Huh... You need to delete only `reliability_score` of Petro Cherpak, not the whole row!\n" +
                            "Use UPDATE instead"
            );

            System.out.println(sqlQueries.get(1).trim());
            List<Map<String, Object>> query1 = executeQuery(sqlQueries.get(1));
            printQueryResults(query1);

            try {
                System.out.println(sqlQueries.get(0).trim());
                executeCUD(sqlQueries.get(0));
                connection.commit();
            } catch (SQLException e) {
                System.out.println("Error: " + e.getMessage() + "\n");
                connection.rollback();
            }

            System.out.println(sqlQueries.get(1).trim());
            List<Map<String, Object>> query = executeQuery(sqlQueries.get(1));
            printQueryResults(query);

            System.out.println(
                    """
                            -- Now, you have seen the deletion anomaly issue. We have some constrains that not allow to
                            -- delete customer's reliability_score. Here you have several options:
                            -- - Completely remove the customer from the system. (Available immediately)
                            -- - Remove constraints. (Need DDL)
                            -- - Add ability to set reliability_score as ''. (Need DDL)
                            """
            );

            assertTrue(
                    sqlQueries.size() == 3 &&
                            sqlQueries.get(2).trim().toLowerCase().startsWith("delete"),
                    "NOW ADD DELETE QUERY IN THE `2NF/2.1_deletion_anomaly.sql` AT THE BOTTOM, WHERE THE 'Petro Cherpak' WILL BE COMPLETELY DELETED FROM THE SYSTEM"
            );

            try {
                System.out.println(sqlQueries.get(2).trim());
                executeCUD(sqlQueries.get(2));
                connection.commit();
            } catch (SQLException e) {
                System.out.println("Error: " + e.getMessage());
                connection.rollback();
            }

            System.out.println(sqlQueries.get(1).trim());
            List<Map<String, Object>> nquery = executeQuery(sqlQueries.get(1));
            printQueryResults(nquery);

            System.out.println(
                    """
                            
                            -- A deletion anomaly occurs when we cannot remove specific customer-related data without deleting the entire customer record.
                            --
                            -- With insertion anomaly situation is different. We cannot add customer record to the system without
                            -- specific customer-related data (reliability_score).
                            -- The situation is simple: We have a new customer that we want to add to the system but we don't 
                            -- have enough information about the customer to measure his `reliability_score`  ¯\\_(ツ)_/¯
                            """
            );

            System.out.println("=========================================================================================================================================================================\n");
        }

        @Test
        @Order(5)
        @DisplayName("2.2: Update anomaly")
        void updateAnomaly() throws SQLException, IOException {
            System.out.println("2.2: Update anomaly\n");

            executeSqlFile("2NF/data/_2.1_denormalization_data.sql");

            List<String> sqlQueries = getSqlQueries("2NF/2.2_update_anomaly.sql");
            assertTrue(sqlQueries.size() > 1, "Not implemented yet!");
            assertFalse(
                    sqlQueries.get(0).contains("ssn"),
                    """
                            You are meticulous one :)
                            It 's cool that you got the point that we could have more than one 'Ivan Kozhumyaka' with different SSN.
                            But let's concentrate on the task and add update only by customer_name and contact_type with value:)"""
            );
            assertTrue(
                    sqlQueries.get(0).toLowerCase().startsWith("update"),
                    "The first query should be UPDATE, where you update 'Ivan Kozhumyaka' reliability_score!"
            );
            assertTrue(
                    sqlQueries.get(0).toLowerCase().contains("customer_name") &&
                            sqlQueries.get(0).contains("Ivan Kozhumyaka"),
                    "The UPDATE, should contain customer_name and Ivan Kozhumyaka!"
            );
            assertTrue(
                    sqlQueries.get(0).toLowerCase().contains("contact_type") &&
                            sqlQueries.get(0).contains("Personal Phone"),
                    "The UPDATE, should contain contact_type and Personal Phone!"
            );
            assertTrue(
                    sqlQueries.get(0).toLowerCase().contains("contact_value") &&
                            sqlQueries.get(0).contains("+380931111111"),
                    "The UPDATE, should contain contact_value and +380931111111!"
            );
            assertTrue(
                    sqlQueries.get(0).toLowerCase().contains("reliability_score") &&
                            sqlQueries.get(0).contains("most reliable"),
                    "The UPDATE, should contain reliability_score and 'most reliable'!"
            );


            try {
                System.out.println(sqlQueries.get(0).trim());
                executeCUD(sqlQueries.get(0));
                connection.commit();
            } catch (SQLException e) {
                System.out.println("Error: " + e.getMessage());
                connection.rollback();
            }


            List<Map<String, Object>> query = executeQuery(sqlQueries.get(1));
            System.out.println("\n" + sqlQueries.get(1).trim());
            printQueryResults(query);

            System.out.println(
                    """
                            -- You see that, right? We have the update anomaly here.
                            --
                            -- We see two 'Ivan Kozhumyaka' in the system with different ssn, phone numbers and `reliability_score`.
                            -- If you done the update only by customer_name = 'Ivan Kozhumyaka' in such case the Ivan Kozhumyaka
                            -- with ssn=123456789 lose his VIP score and it leads to data corruption and inconsistency.
                            -- One row, that you have updated is with 'most reliable' and 3 rows with 'reliable'.
                            -- Next time, when Ivan Kozhumyaka would try to open the credit line with huge sum by his Work Phone
                            -- as authentication parameter - he will be probably rejected because he is JUST 'reliable'!
                            --
                            -- Now, let's focus on redesigning to the 2NF!
                            """
            );

            System.out.println("=========================================================================================================================================================================\n");
        }

        @Test
        @Order(10)
        @DisplayName("2.3: Update to 2NF")
        void migrateTo2NF() throws SQLException, IOException {
            System.out.println("2.3: Update to 2NF\n");

            executeSqlFile("2NF/data/_2.1_denormalization_data.sql");

            String script = removeComments(getSqlScriptContent("2NF/2.3_update_to_2nf.sql"));

            assertTrue(
                    script.toLowerCase().contains("customers"),
                    "The new table name `customers` is absent in script!"
            );
            assertTrue(
                    script.toLowerCase().contains("customer_contacts"),
                    "The new table name `customers` is absent in script!"
            );


            try {
                System.out.println("Script:\n" + script.trim());
                executeCUD(script);
                connection.commit();
            } catch (SQLException e) {
                System.out.println("Error: " + e.getMessage());
                connection.rollback();
            }

            List<Map<String, Object>> customersConstraints = executeQuery(
                    "SELECT column_name, data_type, is_nullable FROM information_schema.columns " +
                            "WHERE table_name = 'customers' ORDER BY column_name"
            );
            printQueryResults(customersConstraints);

            List<Map<String, Object>> customersPK = executeQuery(
                    "SELECT kcu.column_name " +
                            "FROM information_schema.table_constraints tc " +
                            "JOIN information_schema.key_column_usage kcu " +
                            "  ON tc.constraint_name = kcu.constraint_name " +
                            "WHERE tc.constraint_type = 'PRIMARY KEY' " +
                            "  AND tc.table_name = 'customers'"
            );
            assertEquals("ssn", customersPK.get(0).get("column_name"),
                    "The customers table should have ssn as the primary key"
            );

            List<Map<String, Object>> reliabilityCheck = executeQuery(
                    "SELECT EXISTS (" +
                            "  SELECT 1 FROM information_schema.check_constraints cc " +
                            "  JOIN information_schema.constraint_column_usage ccu " +
                            "    ON cc.constraint_name = ccu.constraint_name " +
                            "  WHERE ccu.table_name = 'customers' " +
                            "    AND ccu.column_name = 'reliability_score'" +
                            ") as has_check"
            );
            assertTrue((Boolean) reliabilityCheck.get(0).get("has_check"),
                    "The reliability_score column should have a check constraint"
            );

            List<Map<String, Object>> contactsConstraints = executeQuery(
                    "SELECT column_name, data_type, is_nullable FROM information_schema.columns " +
                            "WHERE table_name = 'customer_contacts' ORDER BY column_name"
            );
            printQueryResults(contactsConstraints);

            List<Map<String, Object>> contactsPK = executeQuery(
                    "SELECT kcu.column_name " +
                            "FROM information_schema.table_constraints tc " +
                            "JOIN information_schema.key_column_usage kcu " +
                            "  ON tc.constraint_name = kcu.constraint_name " +
                            "WHERE tc.constraint_type = 'PRIMARY KEY' " +
                            "  AND tc.table_name = 'customer_contacts' " +
                            "ORDER BY kcu.ordinal_position"
            );
            assertEquals(2, contactsPK.size(),
                    "The customer_contacts table should have a composite primary key of two columns"
            );
            assertEquals("ssn", contactsPK.get(0).get("column_name"),
                    "The first part of the composite primary key should be ssn"
            );
            assertEquals("contact_type", contactsPK.get(1).get("column_name"),
                    "The second part of the composite primary key should be contact_type"
            );

            List<Map<String, Object>> contactsFK = executeQuery(
                    "SELECT EXISTS (" +
                            "  SELECT 1 FROM information_schema.key_column_usage kcu " +
                            "  JOIN information_schema.table_constraints tc " +
                            "    ON kcu.constraint_name = tc.constraint_name " +
                            "  WHERE tc.constraint_type = 'FOREIGN KEY' " +
                            "    AND tc.table_name = 'customer_contacts' " +
                            "    AND kcu.column_name = 'ssn'" +
                            ") as has_fk"
            );
            assertTrue((Boolean) contactsFK.get(0).get("has_fk"),
                    "The customer_contacts table should have a foreign key on ssn to the customers table"
            );

            executeCUD("INSERT INTO customers (ssn, customer_name, reliability_score) " +
                    "VALUES (999999999, 'Test User', 'reliable')");
            connection.commit();

            executeCUD("INSERT INTO customer_contacts (ssn, contact_type, contact_value) " +
                    "VALUES (999999999, 'Test Contact', 'test value')");
            connection.commit();

            try {
                executeCUD("INSERT INTO customer_contacts (ssn, contact_type, contact_value) " +
                        "VALUES (888888888, 'Invalid Contact', 'should fail')");
                fail("Should not be able to insert contact for non-existent customer");
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
            }

            try {
                executeCUD("INSERT INTO customers (ssn, customer_name, reliability_score) " +
                        "VALUES (777777777, 'Invalid Score User', 'invalid score')");
                fail("Should not be able to insert invalid reliability_score");
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
            }


            executeSqlFile("2NF/data/_2.3_migrate_data.sql");

            System.out.println("\nLets see NORMALIZED to 1NF table size:");
            printTableSize("customers_1nf");

            System.out.println("Lets see NORMALIZED to 2NF size:");
            printTableSize("customers");
            printTableSize("customer_contacts");

            System.out.println(
                    """
                            
                            -- Congrats!
                            -- Now, our tables fully follows 1NF and 2NF!
                            -- ˚ ༘ ೀ⋆｡˚ ヾ( ˃ᴗ˂ )◞ • *✰ ˚ ༘ ೀ⋆｡˚
                            """
            );

            System.out.println("=========================================================================================================================================================================\n");
        }

    }

    @Nested
    @DisplayName("3NF: ")
    @Order(10)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class ThirdNF {

        @Test
        @Order(1)
        @DisplayName("3.1.1: Add the `reliability_score_num` column")
        void newColumn() throws SQLException, IOException {
            System.out.println("3.1.1: Add the `reliability_score_num` column\n");

            executeSqlFile("3NF/data/_3_denormalization_data.sql");
            System.out.println("Lets see NORMALIZED to 2NF size BEFORE adding new column:");
            printTableSize("customers");
            printTableSize("customer_contacts");

            List<String> sqlQueries = getSqlQueries("3NF/3.1.1_add_reliability_score_num.sql");
            assertTrue(sqlQueries.size() >= 3, "Not implemented yet! Must be 3 queries!");
            assertTrue(
                    sqlQueries.get(0).toLowerCase().startsWith("alter"),
                    "Firstly need to Alter table and add column reliability_score_num!"
            );
            assertTrue(
                    sqlQueries.get(1).toLowerCase().startsWith("update"),
                    "You need to update all the rows in the table!"
            );
            assertTrue(
                    sqlQueries.get(2).toLowerCase().startsWith("alter"),
                    "Lastly you need to Alter table and add constraint NOT NULL on column reliability_score_num!"
            );


            executeSqlFile("3NF/3.1.1_add_reliability_score_num.sql");

            List<Map<String, Object>> newColumn = executeQuery(
                    "SELECT column_name, data_type, is_nullable FROM information_schema.columns " +
                            "WHERE table_name = 'customers' AND column_name = 'reliability_score_num'"
            );
            assertTrue(!newColumn.isEmpty(), "The column reliability_score_num was not added to the customers table");
            assertEquals("smallint", newColumn.get(0).get("data_type"),
                    "The reliability_score_num column should be of type SMALLINT");
            assertEquals("NO", newColumn.get(0).get("is_nullable"),
                    "The reliability_score_num column should have NOT NULL constraint");

            List<Map<String, Object>> valueMapping = executeQuery(
                    "SELECT DISTINCT reliability_score, reliability_score_num FROM customers ORDER BY reliability_score_num"
            );

            Map<String, Integer> expectedMappings = new HashMap<>();
            expectedMappings.put("not reliable", 1);
            expectedMappings.put("probably reliable", 3);
            expectedMappings.put("reliable", 7);
            expectedMappings.put("most reliable", 10);
            expectedMappings.put("vip", 13);

            for (Map<String, Object> mapping : valueMapping) {
                String scoreText = (String) mapping.get("reliability_score");
                int scoreNum = ((Number) mapping.get("reliability_score_num")).intValue();

                Integer expectedValue = expectedMappings.get(scoreText);
                assertNotNull(expectedValue, "Unexpected reliability_score value: " + scoreText);
                assertEquals(expectedValue.intValue(), scoreNum,
                        "Incorrect mapping for " + scoreText + ", expected " + expectedValue + " but got " + scoreNum);
            }

            List<Map<String, Object>> specificCustomers = executeQuery(
                    "SELECT ssn, customer_name, reliability_score, reliability_score_num FROM customers " +
                            "WHERE ssn IN (100000001, 200000002) ORDER BY ssn"
            );

            boolean petroFound = false;
            boolean ivanFound = false;

            for (Map<String, Object> customer : specificCustomers) {
                String name = (String) customer.get("customer_name");
                int scoreNum = ((Number) customer.get("reliability_score_num")).intValue();
                String scoreText = (String) customer.get("reliability_score");

                if ("Petro Cherpak".equals(name)) {
                    petroFound = true;
                    assertEquals("vip", scoreText, "Petro should have 'vip' reliability_score");
                    assertEquals(13, scoreNum, "Petro should have reliability_score_num of 13");
                } else if ("Ivan Kozhumyaka".equals(name) && "reliable".equals(scoreText)) {
                    ivanFound = true;
                    assertEquals(7, scoreNum, "Ivan with 'reliable' score should have reliability_score_num of 7");
                }
            }

            assertTrue(petroFound, "Test customer 'Petro Cherpak' was not found");
            assertTrue(ivanFound, "Test customer 'Ivan Kozhumyaka' with 'reliable' score was not found");

            List<Map<String, Object>> mismatchedRecords = executeQuery(
                    "SELECT COUNT(*) as count FROM customers WHERE " +
                            "(reliability_score = 'vip' AND reliability_score_num != 13) OR " +
                            "(reliability_score = 'most reliable' AND reliability_score_num != 10) OR " +
                            "(reliability_score = 'reliable' AND reliability_score_num != 7) OR " +
                            "(reliability_score = 'probably reliable' AND reliability_score_num != 3) OR " +
                            "(reliability_score = 'not reliable' AND reliability_score_num != 1)"
            );
            assertEquals(0L, ((Number) mismatchedRecords.get(0).get("count")).longValue(),
                    "There are customers with mismatched reliability_score and reliability_score_num values");


            System.out.println("Lets see NORMALIZED to 2NF size AFTER adding new column:");
            printTableSize("customers");
            printTableSize("customer_contacts");

            System.out.println("=========================================================================================================================================================================\n");
        }

        @Test
        @Order(5)
        @DisplayName("3.1.2: Lose data integrity")
        void loseDataIntegrity() throws SQLException, IOException {
            System.out.println("3.1.2: Lose data integrity\n");

            executeSqlFile("3NF/data/_3_denormalization_data.sql");
            executeSqlFile("3NF/data/_3_denormalization_data1.sql");

            List<String> sqlQueries = getSqlQueries("3NF/3.1.2_lose_data_integrity.sql");
            assertTrue(sqlQueries.size() >= 1, "Not implemented yet!");
            assertFalse(
                    sqlQueries.get(0).contains("most reliable"),
                    """
                            You are meticulous one :)
                            It 's cool that you got the point that we need to update the reliability_score as well.
                            But in this sample we need to update only the reliability_score  :)"""
            );
            assertTrue(
                    sqlQueries.get(0).toLowerCase().contains("customers") &&
                            sqlQueries.get(0).toLowerCase().contains("reliability_score_num") &&
                            sqlQueries.get(0).contains("10"),
                    "The UPDATE, should contain reliability_score_num and 10"
            );

            String select = "select * from customers where ssn = 200000002;";
            List<Map<String, Object>> query = executeQuery(select);
            System.out.println("Before update: \n" + select + "\n");
            printQueryResults(query);


            try {
                System.out.println(sqlQueries.get(0).trim());
                executeCUD(sqlQueries.get(0));
                connection.commit();
            } catch (SQLException e) {
                System.out.println("Error: " + e.getMessage());
                connection.rollback();
            }


            List<Map<String, Object>> query1 = executeQuery(select);
            System.out.println("After update: \n" + select + "\n");
            printQueryResults(query1);

            System.out.println(
                    """
                            -- Now we lost the data integrity with transitive dependency!
                            --
                            -- The `reliability_score_num` now is 10 and according to our matrix that we have build
                            -- earlier the `reliability_score` should be 'most reliable' but it is 'reliable'.
                            -- Does it violates the 1NF or the 2NF? - NO, because the rule says that:
                            -- '1NF + NON-KEY attribute should depend on an ENTIRE primary key!'
                            -- We fully covered 1NF, the `reliability_score_num` depend on an ENTIRE primary key `ssn` - NO, violations here.
                            --
                            -- What the 3NF says:
                            -- Every NON-KEY attribute in a table should depend on the key, the WHOLE key, and NOTHING but the key!
                            --
                            -- But what we see here? - We can see here such dependency flow:
                            -- `ssn` -> `reliability_score` -> `reliability_score_num` NOT THE `ssn` -> `reliability_score_num`.
                            -- In this sample we see the TRANSITIVE dependency between `ssn` and `reliability_score_num`
                            -- and this violates this part of 3NF 'the WHOLE key, and NOTHING but the key!'
                            --
                            -- Now, let's focus on redesigning to the 3NF!
                            """
            );

            System.out.println("=========================================================================================================================================================================\n");
        }

        @Test
        @Order(10)
        @DisplayName("3.2: Update to 3NF")
        void updateTo3NF() throws SQLException, IOException {
            System.out.println("3.2: Update to 3NF\n");

            executeSqlFile("3NF/data/_3_denormalization_data.sql");
            executeSqlFile("3NF/data/_3_denormalization_data1.sql");

            System.out.println("Tables size in 2NF:");
            printTableSize("customers");
            printTableSize("customer_contacts");

            List<String> sqlQueries = getSqlQueries("3NF/3.2_update_to_3nf.sql");
            assertTrue(sqlQueries.size() >= 4, "Not implemented yet!");

            assertTrue(
                    sqlQueries.get(0).toLowerCase().contains("create") &&
                            sqlQueries.get(0).toLowerCase().contains("table") &&
                            sqlQueries.get(0).toLowerCase().contains("reliability_scores"),
                    "First query should CREATE new table `reliability_scores`!"
            );

            assertTrue(
                    sqlQueries.get(1).toLowerCase().contains("insert") &&
                            sqlQueries.get(1).toLowerCase().contains("reliability_scores"),
                    "Send query should INSERT required data into the table `reliability_scores`!"
            );

            assertTrue(
                    sqlQueries.get(2).toLowerCase().contains("alter") &&
                            sqlQueries.get(2).toLowerCase().contains("constraint") &&
                            sqlQueries.get(2).toLowerCase().contains("foreign"),
                    "Third query should add the FK to `reliability_scores`!"
            );

            assertTrue(
                    sqlQueries.get(3).toLowerCase().contains("alter") &&
                            sqlQueries.get(3).toLowerCase().contains("drop") &&
                            sqlQueries.get(3).toLowerCase().contains("reliability_score"),
                    "Forth query should DROP the `reliability_score` column from the `customers` table!"
            );


            for (String sqlQuery : sqlQueries) {
                executeCUD(sqlQuery);
                connection.commit();
            }

            try (Connection vacuumConnection = postgres.getPostgresDatabase().getConnection()) {
                vacuumConnection.setAutoCommit(true);
                try (Statement statement = vacuumConnection.createStatement()) {
                    statement.execute("VACUUM FULL customers;");
                }
            }

            System.out.println("Tables size in 3NF:");
            printTableSize("reliability_scores");
            printTableSize("customers");
            printTableSize("customer_contacts");

            System.out.println(
                    """
                            
                            -- Congrats!
                            -- Now, our tables fully follows 1NF, 2NF and 3NF!
                            -- From the benefits you can see that now our data use less space and we have preserved data integrity!
                            -- ˚ ༘ ೀ⋆｡˚ ヾ( ˃ᴗ˂ )◞ • *✰ ˚ ༘ ೀ⋆｡˚
                            """
            );

            System.out.println("=========================================================================================================================================================================\n");
        }

        @Test
        @Order(15)
        @DisplayName("3.3: Try to lose data integrity")
        void tryToLoseDataIntegrity() throws SQLException, IOException {
            System.out.println("3.3: Try to lose data integrity\n");

            executeSqlFile("3NF/data/_3_denormalization_data.sql");
            executeSqlFile("3NF/data/_3_denormalization_data1.sql");
            executeSqlFile("3NF/data/_3_denormalization_data2.sql");

            List<Map<String, Object>> tableColumns2 = executeQuery(
                    """
                            SELECT column_name, data_type FROM information_schema.columns
                            WHERE table_name = 'reliability_scores'"""
            );
            for (final String column : new String[]{"reliability_score_num", "reliability_score"}) {
                boolean found = tableColumns2.stream().anyMatch(row -> column.equals(row.get("column_name")));
                assertTrue(found, "Expected column '" + column + "' was not found in customers_normalized_order table");
            }

            List<String> sqlQueries = getSqlQueries("3NF/3.3_try_to_lose_data_integrity.sql");
            assertTrue(sqlQueries.size() >= 2, "Not implemented yet!");
            assertTrue(
                    sqlQueries.get(0).toLowerCase().contains("customers") &&
                            sqlQueries.get(0).toLowerCase().contains("reliability_score_num") &&
                            sqlQueries.get(0).contains("10"),
                    "The UPDATE, should contain reliability_score_num and 10"
            );
            assertTrue(
                    sqlQueries.get(1).toLowerCase().contains("select") &&
                            sqlQueries.get(1).toLowerCase().contains("customers") &&
                            sqlQueries.get(1).toLowerCase().contains("reliability_scores") &&
                            sqlQueries.get(1).toLowerCase().contains("join") &&
                            sqlQueries.get(1).contains("200000002"),
                    "You need to make SELECT with JOIN to retrieve all the data described in the task"
            );

            List<Map<String, Object>> query = executeQuery(sqlQueries.get(1));
            System.out.println("Before update: \n" + sqlQueries.get(1) + "\n");
            printQueryResults(query);


            try {
                System.out.println(sqlQueries.get(0).trim());
                executeCUD(sqlQueries.get(0));
                connection.commit();
            } catch (SQLException e) {
                System.out.println("Error: " + e.getMessage());
                connection.rollback();
            }


            List<Map<String, Object>> query1 = executeQuery(sqlQueries.get(1));
            System.out.println("After update: \n" + sqlQueries.get(1) + "\n");
            printQueryResults(query1);

            System.out.println("=========================================================================================================================================================================\n");
        }
    }


    private static void printTableSize(String tableName) throws SQLException {
        System.out.printf("Table: `%s`%n", tableName);
        List<Map<String, Object>> tableSize = executeQuery(
                """
                        SELECT
                            pg_size_pretty(pg_total_relation_size('%s')) AS "Total Size",
                            pg_size_pretty(pg_relation_size('%s')) AS "Table Size (excluding indexes)",
                            pg_size_pretty(pg_indexes_size('%s')) AS "Total Index Size",
                            count(*) "Amount of rows" FROM %s;
                        """.formatted(tableName, tableName, tableName, tableName)
        );
        printQueryResults(tableSize);
    }

    private static List<String> getSqlQueries(String filePath) throws IOException {
        return getSqlQueries(filePath, ";");
    }

    private static List<String> getSqlQueries(String filePath, String spliterator) throws IOException {
        String sql = Files.readString(Paths.get("src/test/resources/" + filePath));
        String cleanSql = removeComments(sql);
        return Arrays.stream(cleanSql.split(spliterator))
                .map(String::trim)
                .map(s -> s + ";")
                .filter(s -> !s.equals(";"))
                .collect(Collectors.toList());
    }

    private static String removeComments(String sql) {
        if (sql == null || sql.isEmpty()) {
            return sql;
        }

        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < sql.length()) {
            if (i + 1 < sql.length() && sql.charAt(i) == '/' && sql.charAt(i + 1) == '*') {
                int endPos = sql.indexOf("*/", i + 2);
                if (endPos == -1) {
                    i += 2;
                } else {
                    i = endPos + 2;
                }
            } else if (i + 1 < sql.length() && sql.charAt(i) == '-' && sql.charAt(i + 1) == '-') {
                int endPos = sql.indexOf('\n', i);
                if (endPos == -1) {
                    break;
                } else {
                    i = endPos + 1;
                }
            } else {
                result.append(sql.charAt(i));
                i++;
            }
        }

        return result.toString();
    }

    private static void executeSqlFile(String filePath) throws IOException, SQLException {
        String sql = getSqlScriptContent(filePath);
        try (Statement statement = connection.createStatement()) {
            boolean execute = statement.execute(sql);
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    private static String getSqlScriptContent(String filePath) throws IOException {
        return Files.readString(Paths.get("src/test/resources/" + filePath));
    }

    private static List<Map<String, Object>> executeQuery(String sql) throws SQLException {
        return executeQuery(sql, false);
    }

    private static List<Map<String, Object>> executeQuery(String sql, boolean printQuery) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();

        if (printQuery) {
            System.out.println(sql);
        }

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

    private static void executeCUD(String sql) throws SQLException {
        executeCUD(sql, false);
    }

    private static void executeCUD(String sql, boolean printQuery) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private static void printQueryResults(List<Map<String, Object>> results) {
        if (results.isEmpty()) {
            System.out.println("No results found.");
            return;
        }

        List<String> columnNames = new ArrayList<>(results.get(0).keySet());

        int[] columnWidths = calculateColumnWidths(results, columnNames);

        var wholeLen = 1;
        for (int columnWidth : columnWidths) {
            wholeLen += columnWidth + 3;
        }
        System.out.println("_".repeat(wholeLen));
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

        System.out.println("_".repeat(wholeLen) + "\n");
    }

    private static int[] calculateColumnWidths(List<Map<String, Object>> results, List<String> columnNames) {
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

    private static void printRow(List<String> values, int[] widths) {
        StringBuilder sb = new StringBuilder("|");
        for (int i = 0; i < values.size(); i++) {
            sb.append(" ").append(String.format("%-" + widths[i] + "s", values.get(i))).append(" |");
        }
        System.out.println(sb);
    }

    private static void printSeparator(int[] widths) {
        StringBuilder sb = new StringBuilder("|");
        for (int width : widths) {
            sb.append("-".repeat(width + 2)).append("|");
        }
        System.out.println(sb);
    }

}