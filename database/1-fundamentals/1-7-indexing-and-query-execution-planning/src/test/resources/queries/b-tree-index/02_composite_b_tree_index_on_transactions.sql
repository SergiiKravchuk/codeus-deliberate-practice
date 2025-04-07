------------------------------------------------------------------------------------------------------------------------
-- TASK 2: Composite Index on Transactions
------------------------------------------------------------------------------------------------------------------------
-- Description: Create a composite B-Tree index to optimize this query.
--
-- Index Name: transaction_amount_and_date_idx
--
-- Analyze the query with EXPLAIN ANALYZE.
-- Query:
SELECT amount, transaction_date
 FROM transactions
 WHERE amount > 500 AND transaction_date > NOW() - INTERVAL '10 days';
-- TODO: Create index:
CREATE INDEX transaction_amount_and_date_idx on transactions(amount, transaction_date);
--
-- After index creation: analyze the query with EXPLAIN ANALYZE again.