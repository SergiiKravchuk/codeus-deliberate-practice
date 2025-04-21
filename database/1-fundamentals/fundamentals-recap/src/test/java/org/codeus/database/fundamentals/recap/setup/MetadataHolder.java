package org.codeus.database.fundamentals.recap.setup;

import java.util.Collections;
import java.util.List;

public abstract class MetadataHolder {

  private final List<ProblemMetadata> problemMetadataList;

  private static final String QUERIES_DIR = "queries/";
  private static final String BLOCK_2 = "block_2/";
  private static final String PATH = QUERIES_DIR + BLOCK_2;

  public MetadataHolder() {
    this.problemMetadataList = List.of(
      new ProblemMetadata(PATH + "problem_1.sql",  "SELECT id FROM accounts WHERE created_at > NOW() - INTERVAL '10 days';", List.of("accounts"), List.of("ON public.accounts USING btree (created_at)", "ON public.accounts USING btree (created_at) INCLUDE (id)"), Collections.emptyList()),
      new ProblemMetadata(PATH + "problem_2.sql",  "SELECT c.id AS customer_id, c.first_name, c.last_name, SUM(a.balance) AS total_balance FROM customers c JOIN accounts a ON c.id = a.customer_id GROUP BY c.id, c.first_name, c.last_name HAVING SUM(a.balance) > 2000;", List.of("customers", "accounts"), List.of(), List.of("SELECT c.id AS customer_id, c.first_name, c.last_name, SUM(a.balance) AS total_balance FROM customers c JOIN accounts a ON c.id = a.customer_id GROUP BY c.id, c.first_name, c.last_name HAVING SUM(a.balance) > 2000;")),
      new ProblemMetadata(PATH + "problem_3.sql",  "SELECT account_id, amount, transaction_date FROM transactions WHERE amount > 200 AND transaction_date > NOW() - INTERVAL '15 days';", List.of("transactions"), List.of("ON public.transactions USING btree (amount, transaction_date)", "ON public.transactions USING btree (amount, transaction_date) INCLUDE (account_id)"), List.of()),
      new ProblemMetadata(PATH + "problem_4.sql",  "SELECT a.id AS account_id, a.account_type, COUNT(t.id) AS transaction_count, SUM(t.amount) AS total_amount FROM accounts a JOIN transactions t ON a.id = t.account_id WHERE t.transaction_date > NOW() - INTERVAL '30 days' GROUP BY a.id, a.account_type;", List.of("accounts", "transactions"), List.of(), List.of("SELECT a.id AS account_id, a.account_type, COUNT(t.id) AS transaction_count, SUM(t.amount) AS total_amount FROM accounts a JOIN transactions t ON a.id = t.account_id WHERE t.transaction_date > NOW() - '30 days'::interval GROUP BY a.id, a.account_type;")),
      new ProblemMetadata(PATH + "problem_5.sql",  "SELECT id FROM customers WHERE LOWER(address) = 'davis street, city, country';", List.of("customers"), List.of("CREATE INDEX idx_customers_lower_address ON public.customers USING btree (lower(address))"), List.of()),
      new ProblemMetadata(PATH + "problem_6.sql",  "SELECT id, balance, created_at FROM accounts WHERE account_type = 'checking' AND created_at > NOW() - INTERVAL '10 days';", List.of("accounts"), List.of("ON public.accounts USING btree (account_type, created_at) INCLUDE (id, balance)"), List.of()),
      new ProblemMetadata(PATH + "problem_7.sql", "SELECT t.id, t.account_id, t.amount, t.transaction_date, a.balance FROM transactions t JOIN accounts a ON t.account_id = a.id WHERE t.transaction_type IN ('withdrawal', 'transfer') AND t.transaction_date > NOW() - INTERVAL '14 days' AND a.balance > 10000;", List.of("transactions", "accounts"), List.of("ON public.transactions USING btree (transaction_date) INCLUDE (account_id, amount) WHERE ((transaction_type)::text = ANY ((ARRAY['withdrawal'::character varying, 'transfer'::character varying])::text[]))", "ON public.accounts USING btree (balance)"), List.of()),
      new ProblemMetadata(PATH + "problem_8.sql", "SELECT id, email, phone FROM customers WHERE first_name LIKE 'Ann%';", List.of("customers"), List.of("ON public.customers USING btree (first_name text_pattern_ops)"), List.of())
    );
  }

  /**
   * Retrieves ProblemMetadata for given problemIndex (non-zero based).
   * @param problemIndex - regular index scheme (non-zero based)
   */
  protected ProblemMetadata getProblemMetadata(int problemIndex) {
    return problemMetadataList.get(problemIndex - 1);
  }


  public record ProblemMetadata(String filename, String query, List<String> tables, List<String> indexes, List<String> views) {}
}
