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
create index transaction_amount_idx on transactions(amount);
--
-- After index creation: analyze the query with EXPLAIN ANALYZE again.