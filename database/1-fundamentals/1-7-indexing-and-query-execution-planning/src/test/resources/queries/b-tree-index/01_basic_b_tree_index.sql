-- TASK 1: Basic Indexing on Transactions
--
-- Create an index to improve performance.
--
-- Index Name: transaction_amount_idx
--
-- Analyze the query with EXPLAIN ANALYZE.
-- Query:
SELECT amount FROM transactions WHERE amount > 200;

create index transaction_amount_idx on transactions(amount);
-- Solution:
-- create index transaction_amount_idx on transactions(amount);
