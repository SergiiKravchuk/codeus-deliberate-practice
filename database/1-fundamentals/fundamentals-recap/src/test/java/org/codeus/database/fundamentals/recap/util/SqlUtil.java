package org.codeus.database.fundamentals.recap.util;

import java.sql.Connection;
import java.sql.SQLException;

public class SqlUtil {

  public static boolean checkFileSyntax(String fileName) {
    //TODO: finish
    return true;
  }


  // Normalize SQL by removing extra whitespace, line breaks, semicolons, etc.
  public static String normalizeSql(String sql) {
    return sql
      .replaceAll("\\s+", " ")// Replace multiple whitespace with single space
      .replaceAll("[\\r\\n();]+", "")// Remove extra symbols
      .trim()
      .toLowerCase();              // Case-insensitive comparison
  }

  public static boolean dropCustomIndexesForTable(String table, Connection connection) {
    var procedure = """
        DO $$
            DECLARE
                idx RECORD;
            BEGIN
                FOR idx IN
                    SELECT ci.relname AS indexname
                    FROM pg_class ct
                             JOIN pg_index i ON ct.oid = i.indrelid
                             JOIN pg_class ci ON ci.oid = i.indexrelid
                    WHERE ct.relname = '%s'
                      AND NOT i.indisprimary
                      AND NOT i.indisunique
                    LOOP
                        EXECUTE 'DROP INDEX IF EXISTS ' || quote_ident(idx.indexname);
                    END LOOP;
            END$$;

        """.formatted(table);

    try (var statement = connection.prepareStatement(procedure)) {
      System.out.println("dropCustomIndexesForTable" + statement.toString());
      return statement.execute();
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }
}
