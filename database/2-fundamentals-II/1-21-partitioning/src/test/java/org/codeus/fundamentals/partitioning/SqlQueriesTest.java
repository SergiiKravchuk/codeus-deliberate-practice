package org.codeus.fundamentals.partitioning;

import org.codeus.database.common.EmbeddedPostgreSqlSetup;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SqlQueriesTest extends EmbeddedPostgreSqlSetup {
  private final static String TASKS_DIR = "tasks/";

  @Test
  @Order(1)
  void P01_testRangePartitionTransactionsByDate() throws IOException, SQLException {
    String filename = "01_range_partition_transactions_by_date.sql";
    executeQueriesFromFile(TASKS_DIR + filename);

    String parentTable = "transactions_partitioned";

    List<String> expectedPartitions = List.of(
            "transactions_2024_01",
            "transactions_2024_02",
            "transactions_2024_03"
    );

    for (String partitionName : expectedPartitions) {
      assertPartitionExists(parentTable, partitionName);
    }

    List<Map<String, Object>> febRows = fetchRowsFromTable("transactions_2024_02");
    assertEquals(1, febRows.size(), "Expected one row in February 2024 partition");
  }

  @Test
  @Order(2)
  void P02_testListPartitionAccountsByType() throws IOException, SQLException {
    String filename = "02_list_partition_accounts_by_type.sql";
    executeQueriesFromFile(TASKS_DIR + filename);

    String parentTable = "accounts_partitioned";

    List<String> expectedPartitions = List.of(
            "checking_accounts",
            "savings_accounts"
    );

    for (String partitionName : expectedPartitions) {
      assertPartitionExists(parentTable, partitionName);
    }

    List<Map<String, Object>> checkingRows = fetchRowsFromTable("checking_accounts");
    List<Map<String, Object>> savingsRows = fetchRowsFromTable("savings_accounts");

    assertEquals(1, checkingRows.size(), "Expected one row in checking_accounts");
    assertEquals(1, savingsRows.size(), "Expected one row in savings_accounts");
  }

  @Test
  @Order(3)
  void P03_testListPartitionWithDefault() throws IOException, SQLException {
    String filename = "03_list_partition_with_default.sql";
    executeQueriesFromFile(TASKS_DIR + filename);

    String parentTable = "accounts_partitioned";

    String defaultPartitionName = "default_accounts";
    assertPartitionExists(parentTable, defaultPartitionName);

    List<Map<String, Object>> defaultRows = fetchRowsFromTable(defaultPartitionName);
    assertEquals(1, defaultRows.size(), "Expected one row in default_accounts");
  }

  @Test
  @Order(4)
  void P04_attachHistoricalPartition() throws IOException, SQLException {
    String filename = "04_attach_partition_for_historical_data.sql";
    executeQueriesFromFile(TASKS_DIR + filename);

    assertPartitionExists("transactions_partitioned", "transactions_before_2022");

    List<Map<String, Object>> rows = fetchRowsFromTable("transactions_before_2022");
    assertEquals(1, rows.size(), "Expected one transaction in the historical partition");
  }

  @Test
  @Order(5)
  void P05_testPartitionByYear() throws IOException, SQLException {
    String filename = "05_range_partition_transactions_by_year.sql";
    executeQueriesFromFile(TASKS_DIR + filename);

    assertPartitionExists("transactions_partitioned", "transactions_2022");
    assertPartitionExists("transactions_partitioned", "transactions_2023");
    assertPartitionExists("transactions_partitioned", "transactions_2024");

    List<Map<String, Object>> rows2022 = fetchRowsFromTable("transactions_2022");
    assertEquals(1, rows2022.size(), "Expected one row in transactions_2022");

    List<Map<String, Object>> rows2023 = fetchRowsFromTable("transactions_2023");
    assertEquals(1, rows2023.size(), "Expected one row in transactions_2023");

    List<Map<String, Object>> rows2024 = fetchRowsFromTable("transactions_2024");
    assertEquals(1, rows2024.size(), "Expected one row in transactions_2024");
  }

  @Test
  @Order(6)
  void P06_testHashPartitioningCustomers() throws IOException, SQLException {
    String filename = "06_hash_partition_customers_by_id.sql";
    executeQueriesFromFile(TASKS_DIR + filename);

    for (int i = 0; i < 4; i++) {
      assertPartitionExists("customers_partitioned", "customers_part_" + i);
    }

    String expectedFirstName = "Anna";
    String expectedLastName = "Nowak";

    boolean found = false;
    for (int i = 0; i < 4; i++) {
      List<Map<String, Object>> rows = fetchRowsFromTable("customers_part_" + i);
      if (!rows.isEmpty()) {
        for (Map<String, Object> row : rows) {
          if (Objects.equals(row.get("id"), 101)) {
            assertEquals(expectedFirstName, row.get("first_name"));
            assertEquals(expectedLastName, row.get("last_name"));
            found = true;
          }
        }
      }
    }

    assertTrue(found, "Expected to find customer with id=101 in one of the hash partitions");
  }

  @Test
  @Order(7)
  void P07_testDetachAndDropPartition() throws IOException, SQLException {
    String filename = "07_drop_partition_and_reinsert_data.sql";
    executeQueriesFromFile(TASKS_DIR + filename);

    assertPartitionDoesNotExist("transactions_partitioned", "transactions_2023");

    List<Map<String, Object>> copyRows = fetchRowsFromTable("transactions_2023_copy");
    assertEquals(1, copyRows.size(), "Expected backup rows to be copied before partition dropped");

    Map<String, Object> row = copyRows.get(0);
    assertEquals(100, row.get("account_id"));
    assertEquals("withdrawal", row.get("transaction_type"));
  }

  @Test
  @Order(8)
  void P08_testMultiLevelPartitioning() throws IOException, SQLException {
    String filename = "08_subpartition_year_type.sql";
    executeQueriesFromFile(TASKS_DIR + filename);

    assertPartitionExists("transactions_multilevel", "transactions_2023");

    assertPartitionExists("transactions_2023", "transactions_2023_withdrawal");

    List<Map<String, Object>> rows = fetchRowsFromTable("transactions_2023_withdrawal");
    assertEquals(1, rows.size(), "Expected one row in 2023_withdrawal partition");

    Map<String, Object> row = rows.get(0);
    assertEquals(2, row.get("account_id"));
    assertEquals("withdrawal", row.get("transaction_type"));
    assertEquals(500.00, ((Number) row.get("amount")).doubleValue(), 0.01);
  }

  @Test
  @Order(9)
  void P09_testMultiLevelPartitioningMigrationAndPlans() throws IOException, SQLException {
    String filename = "09_subpartition_year_type.sql";
    executeQueriesFromFile(TASKS_DIR + filename);

    List<Map<String, Object>> rawRows = fetchRowsFromTable("transactions_raw");
    assertEquals(10000, rawRows.size(), "Expected 1000 rows in unpartitioned table");

    assertPartitionExists("transactions_multilevel", "transactions_2023");
    assertPartitionExists("transactions_2023", "transactions_2023_deposit");
    assertPartitionExists("transactions_2023", "transactions_2023_withdrawal");

    List<Map<String, Object>> rowsDeposit = fetchRowsFromTable("transactions_2023_deposit");
    List<Map<String, Object>> rowsWithdrawal = fetchRowsFromTable("transactions_2023_withdrawal");
    assertEquals(5000, rowsDeposit.size(), "Expected ~500 deposits in subpartition");
    assertEquals(5000, rowsWithdrawal.size(), "Expected ~500 withdrawals in subpartition");

    System.out.println("\n--- Raw Table Query Plan ---");
    printQueryPlan("""
        EXPLAIN ANALYZE
        SELECT * FROM transactions_raw
        WHERE transaction_type = 'withdrawal'
          AND transaction_date BETWEEN '2023-03-01' AND '2023-04-01'
    """);

    System.out.println("\n--- Partitioned Table Query Plan ---");
    printQueryPlan("""
        EXPLAIN ANALYZE
        SELECT * FROM transactions_multilevel
        WHERE transaction_type = 'withdrawal'
          AND transaction_date BETWEEN '2023-03-01' AND '2023-04-01'
    """);
  }

  private void assertPartitionExists(String parentTable, String partitionName) throws SQLException {
    String sql = """
        SELECT inhrelid::regclass::text AS child
        FROM pg_inherits
        JOIN pg_class parent ON pg_inherits.inhparent = parent.oid
        JOIN pg_class child ON pg_inherits.inhrelid = child.oid
        WHERE parent.relname = ?
          AND child.relname = ?
    """;

    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
      stmt.setString(1, parentTable);
      stmt.setString(2, partitionName);

      try (ResultSet rs = stmt.executeQuery()) {
        if (!rs.next()) {
          fail("Partition '" + partitionName + "' does not exist under '" + parentTable + "'");
        }
      }
    }
  }

  public List<Map<String, Object>> fetchRowsFromTable(String tableName) throws SQLException {
    List<Map<String, Object>> rows = new ArrayList<>();

    String sql = "SELECT * FROM " + tableName;

    try (PreparedStatement stmt = connection.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {

      ResultSetMetaData meta = rs.getMetaData();
      int columnCount = meta.getColumnCount();

      while (rs.next()) {
        Map<String, Object> row = new LinkedHashMap<>();
        for (int i = 1; i <= columnCount; i++) {
          row.put(meta.getColumnLabel(i), rs.getObject(i));
        }
        rows.add(row);
      }
    }

    return rows;
  }

  private void assertPartitionDoesNotExist(String parentTable, String partitionTable) throws SQLException {
    String sql = """
        SELECT inhrelid::regclass::text
        FROM pg_inherits
        JOIN pg_class parent ON pg_inherits.inhparent = parent.oid
        JOIN pg_class child ON pg_inherits.inhrelid = child.oid
        WHERE parent.relname = ? AND child.relname = ?
        """;

    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
      stmt.setString(1, parentTable);
      stmt.setString(2, partitionTable);

      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          fail("Expected partition '%s' to be detached or dropped from table '%s'".formatted(partitionTable, parentTable));
        }
      }
    }
  }

  private void printQueryPlan(String sql) throws SQLException {
    try (PreparedStatement stmt = connection.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {
      while (rs.next()) {
        System.out.println(rs.getString(1));
      }
    }
  }
}