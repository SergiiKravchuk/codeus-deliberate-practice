-- TASK 5: Covering Index on Transactions
--
-- Create a covering index that includes account_id and transaction_type to optimize the query.
--
-- Index Name: transaction_amount_covering_idx
--
-- Analyze the query with EXPLAIN ANALYZE.
-- Query:
SELECT account_id, transaction_type FROM transactions WHERE amount < 50;
-- Create Index:
CREATE INDEX transaction_amount_covering_idx ON transactions(amount) INCLUDE (account_id, transaction_type);
-- After index creation, check these queries:
-- SELECT account_id, transaction_type FROM transactions WHERE amount < 50; (idx scan)
-- SELECT account_id, transaction_type, transaction_date FROM transactions WHERE amount < 50; (seq scan)

-- Expected solution:
-- CREATE INDEX idx_employees_branch_covering ON employees(branch_id) INCLUDE (first_name, last_name);