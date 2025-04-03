------------------------------------------------------------------------------------------------------------------------
-- TASK 6: Improving Join Performance on Transactions and Accounts
------------------------------------------------------------------------------------------------------------------------
-- Create an index on the accounts table that covers the join key.
--
-- Index Name: accounts_customer_id_idx
--
-- Analyze the query with EXPLAIN ANALYZE.
-- Query:
SELECT c.first_name, c.last_name, a.created_at FROM customers c
 JOIN accounts a ON a.customer_id = c.id
 WHERE c.id = 42;
-- TODO: Create Index:
CREATE INDEX accounts_customer_id_idx ON accounts(customer_id);
-- After index creation: analyze the query with EXPLAIN ANALYZE again.