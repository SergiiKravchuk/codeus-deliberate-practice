-- ===================================================================================================
-- TASK 1: Basic Indexing on Transactions
-- ===================================================================================================
-- Description: Create an index to improve filter performance.
--
-- Index Name: transaction_amount_idx
--
-- Analyze the query with EXPLAIN ANALYZE.
-- Query:
SELECT amount FROM transactions WHERE amount > 200;
-- TODO: Create Index:
CREATE INDEX transaction_amount_idx ON transactions(amount);
--
-- After index creation: analyze the query with EXPLAIN ANALYZE again.