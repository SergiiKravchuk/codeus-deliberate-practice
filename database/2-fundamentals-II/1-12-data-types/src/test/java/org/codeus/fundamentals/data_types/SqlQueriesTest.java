package org.codeus.fundamentals.data_types;

import org.codeus.fundamentals.data_types.setup.EmbeddedPostgreSqlSetup;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class SqlQueriesTest extends EmbeddedPostgreSqlSetup {

  private static final String MAIN_DIR = "exercises/";

  @Order(1)
  @Nested
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class EnumExerciseTest {

    @AfterAll
    public static void rollbackBlockChanges() throws SQLException {
      connection.rollback();
    }

    private static final String EXEC_DIR = "1-enum-type/";

    @Test
    @Order(1)
    void testDeclareEnum() throws Exception {
      executeQueryWithoutResultFromFile(MAIN_DIR + EXEC_DIR + "1-1-declare-type.sql");

      DatabaseMetaData meta = connection.getMetaData();
      try (ResultSet rs = meta.getColumns(null, null, "accounts", "status")) {
        assertTrue(rs.next(), "Column 'status' should exist");
        assertTrue(rs.getString("TYPE_NAME").startsWith("account_status"));
      }
    }

    @Test
    @Order(5)
    void testInsertEnum() throws Exception {
      // count rows before  
      ResultSet before = connection.createStatement().executeQuery("SELECT COUNT(*) FROM accounts");
      before.next();
      int countBefore = before.getInt(1);

      executeQueryWithoutResultFromFile(MAIN_DIR + EXEC_DIR + "1-2-insert.sql");

      ResultSet after = connection.createStatement().executeQuery("SELECT COUNT(*) FROM accounts");
      after.next();
      int countAfter = after.getInt(1);

      assertEquals(countBefore + 5, countAfter, "Five new accounts should be inserted");
    }

    @Test
    @Order(10)
    void testSelectEnum() throws Exception {
      List<Map<String, Object>> result = executeQueryFromFile(MAIN_DIR + EXEC_DIR + "1-3-select.sql");

      assertTrue(!result.isEmpty(), "Should return at least one 'active' account");
      // TODO: add further assertions on returned values
    }

    @Test
    @Order(15)
    void testModifyEnum() throws Exception {
      executeQueryWithoutResultFromFile(MAIN_DIR + EXEC_DIR + "1-4-modify-type.sql");

      List<Map<String, Object>> result = executeQuery("SELECT id, status FROM accounts WHERE id = 101;");
      printQueryResults(result);

      assertEquals(1, result.size(), "There should be only one Account with ID=1101");
      assertEquals(101, result.get(0).get("id"), "Account with ID=101 must exist in database");
      assertEquals("suspended", result.get(0).get("status"), "account with ID=101 must have 'suspended' status");
    }
  }

  @Order(5)
  @Nested
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class ArrayExerciseTest {
    private static final String EXEC_DIR = "2-array-type/";

    @AfterAll
    public static void rollbackBlockChanges() throws SQLException {
      connection.rollback();
    }

    @Test
    @Order(1)
    void testDeclareArray() throws Exception {
      executeQueryWithoutResultFromFile(MAIN_DIR + EXEC_DIR + "2-1-declare-type.sql");
      DatabaseMetaData meta = connection.getMetaData();
      try (ResultSet rs = meta.getColumns(null, null, "customers", "tags")) {
        assertTrue(rs.next(), "Column 'tags' should exist on customers");
        assertEquals("_text", rs.getString("TYPE_NAME")); //_text is an internal name for text[], the same as int8 for bigint.
      }
    }

    @Test
    @Order(5)
    void testInsertArray() throws Exception {
      ResultSet before = connection.createStatement().executeQuery("SELECT COUNT(*) FROM customers");
      before.next();
      int countBefore = before.getInt(1);

      executeQueryWithoutResultFromFile(MAIN_DIR + EXEC_DIR + "2-2-insert.sql");

      ResultSet after = connection.createStatement().executeQuery("SELECT COUNT(*) FROM customers");
      after.next();
      int countAfter = after.getInt(1);

      assertEquals(countBefore + 5, countAfter, "Five new customers should be inserted");
    }

    @Test
    @Order(10)
    void testSelectArray() throws Exception {
      var result = executeQueryFromFile(MAIN_DIR + EXEC_DIR + "2-3-select.sql");

      assertTrue(!result.isEmpty(), "Should return customers tagged 'premium'");
    }
  }

  @Order(10)
  @Nested
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class RangeExerciseTest {
    private static final String EXEC_DIR = "3-range-type/";

    @AfterAll
    public static void rollbackBlockChanges() throws SQLException {
      connection.rollback();
    }

    @Test
    @Order(1)
    void testDeclareRange() throws Exception {
      executeQueryWithoutResultFromFile(MAIN_DIR + EXEC_DIR + "3-1-declare-type.sql");
      DatabaseMetaData meta = connection.getMetaData();
      try (ResultSet rs = meta.getColumns(null, null, "accounts", "credit_limit")) {
        assertTrue(rs.next(), "Column 'tags' should exist on customers");
        assertEquals("balance_range", rs.getString("TYPE_NAME"));
      }
    }

    @Test
    @Order(5)
    void testInsertRange() throws Exception {
      ResultSet before = connection.createStatement().executeQuery("SELECT COUNT(*) FROM accounts");
      before.next();
      int b = before.getInt(1);
      executeQueryWithoutResultFromFile(MAIN_DIR + EXEC_DIR + "3-2-insert.sql");
      ResultSet after = connection.createStatement().executeQuery("SELECT COUNT(*) FROM accounts");
      after.next();
      int a = after.getInt(1);
      assertEquals(b + 5, a);
    }

    @Test
    @Order(10)
    void testSelectRange() throws Exception {
      var result = executeQueryFromFile(MAIN_DIR + EXEC_DIR + "3-3-select.sql");

      assertTrue(!result.isEmpty(), "Should return accounts covering 5000 in their credit_limit");
    }
  }

  @Order(15)
  @Nested
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class CompositeExerciseTest {
    private static final String EXEC_DIR = "4-composite-type/";

    @AfterAll
    public static void rollbackBlockChanges() throws SQLException {
      connection.rollback();
    }

    @Test
    @Order(1)
    void testDeclareComposite() throws Exception {
      executeQueryWithoutResultFromFile(MAIN_DIR + EXEC_DIR + "4-1-declare-type.sql");
      DatabaseMetaData meta = connection.getMetaData();
      try (ResultSet rs = meta.getColumns(null, null, "customers", "full_name")) {
        assertTrue(rs.next(), "Column 'full_name' should exist");
        assertTrue(rs.getString("TYPE_NAME").startsWith("full_name"));
      }
    }

    @Test
    @Order(5)
    void testInsertComposite() throws Exception {
      executeQueryWithoutResultFromFile(MAIN_DIR + EXEC_DIR + "4-2-insert.sql");
      //TODO: some check
      // verify no errors and data populated; optionally query a known row
    }

    //TODO: check and fix test
    @Test
    @Order(10)
    void testSelectComposite() throws Exception {
      var result = executeQueryFromFile(MAIN_DIR + EXEC_DIR + "4-3-select.sql");

      assertTrue(!result.isEmpty());
    }

    @Test
    @Order(15)
    void testModifyComposite() throws Exception {
      executeQueryWithoutResultFromFile(MAIN_DIR + EXEC_DIR + "4-4-modify-type.sql");

      List<Map<String, Object>> result = executeQuery("SELECT id, full_name FROM customers WHERE id = 1;");
      printQueryResults(result);

      assertEquals(1, result.size(), "There should be only one customer with ID=1");
      assertEquals(1, result.get(0).get("id"), "Customer with ID=1 must exist in database");
      assertEquals("(John,Marie,Doe)", result.get(0).get("full_name").toString(), "Customer with ID=1 must have middle name value ('Marie') present");
    }
  }

  @Order(20)
  @Nested
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class DomainExerciseTest {
    private static final String EXEC_DIR = "5-domain-type/";

    @AfterAll
    public static void rollbackBlockChanges() throws SQLException {
      connection.rollback();
    }

    @Test
    @Order(1)
    void testDeclareDomain() throws Exception {
      executeQueryWithoutResultFromFile(MAIN_DIR + EXEC_DIR + "5-1-declare-type.sql");
      DatabaseMetaData meta = connection.getMetaData();
      try (ResultSet rs = meta.getColumns(null, null, "customers", "local_phone")) {
        assertTrue(rs.next(), "Column 'local_phone' should exist");
        assertTrue(rs.getString("TYPE_NAME").startsWith("us_phone"));
      }
    }

    @Test
    @Order(5)
    void testInsertDomain() throws Exception {
      ResultSet before = connection.createStatement().executeQuery("SELECT COUNT(*) FROM customers");
      before.next();
      int b = before.getInt(1);
      executeQueryWithoutResultFromFile(MAIN_DIR + EXEC_DIR + "5-2-insert.sql");
      ResultSet after = connection.createStatement().executeQuery("SELECT COUNT(*) FROM customers");
      after.next();
      int a = after.getInt(1);
      assertEquals(b + 5, a);
    }

    @Test
    @Order(10)
    void testSelectDomain() throws Exception {
      var result = executeQueryFromFile(MAIN_DIR + EXEC_DIR + "5-3-select.sql");

      assertEquals(2, result.size(), "There are should be two local_phone numbers starting with 1");
    }
  }

  @Test
  @Order(999)
  public void playground() throws SQLException, IOException {
    executeQueryWithoutResultFromFile("playground.sql");
  }

}