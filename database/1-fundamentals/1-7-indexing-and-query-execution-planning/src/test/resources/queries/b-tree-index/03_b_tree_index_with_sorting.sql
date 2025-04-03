------------------------------------------------------------------------------------------------------------------------
-- TASK 3: Index for Sorting Transactions
------------------------------------------------------------------------------------------------------------------------
-- Description: Create an index to support the filter and ORDER BY clause.
--
-- Index Name: transaction_account_id_and_date_idx
--
-- Analyze the query with EXPLAIN ANALYZE.
-- Query:
SELECT account_id, transaction_date FROM transactions WHERE account_id = 5 ORDER BY transaction_date;
-- TODO: Create index:
CREATE INDEX transaction_account_id_and_date_idx on transactions(account_id, transaction_date);
--
-- After index creation: analyze the query with EXPLAIN ANALYZE again.
