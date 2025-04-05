-- TASK 3: Index for Sorting Transactions
--
-- Create an index to support the ORDER BY clause.
--
-- Index Name: transaction_account_id_and_date_idx
--
-- Analyze the query with EXPLAIN ANALYZE.
-- Query:
SELECT account_id, transaction_date FROM transactions WHERE account_id = 5 ORDER BY transaction_date;

CREATE INDEX transaction_account_id_and_date_idx on transactions(account_id, transaction_date);
--
-- Expected solution:
-- CREATE INDEX idx_transactions_account_date ON transactions(account_id, transaction_date DESC);