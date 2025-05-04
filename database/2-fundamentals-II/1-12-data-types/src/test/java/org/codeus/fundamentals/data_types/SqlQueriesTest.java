package org.codeus.fundamentals.data_types;

import org.codeus.database.common.EmbeddedPostgreSqlSetup;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SqlQueriesTest extends EmbeddedPostgreSqlSetup {

  @Order(1)
  @Test
  void test() {
    assertTrue(true);
  }
}