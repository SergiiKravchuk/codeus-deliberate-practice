package org.codeus.fundamentals.security_fundamentals;

import org.codeus.database.common.EmbeddedPostgreSqlSetup;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class SqlQueriesTest extends EmbeddedPostgreSqlSetup {

  @Order(1)
  @Test
  void test() {
    assertTrue(true);
  }
}