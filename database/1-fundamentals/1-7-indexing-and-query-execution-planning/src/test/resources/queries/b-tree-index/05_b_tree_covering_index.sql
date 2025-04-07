------------------------------------------------------------------------------------------------------------------------
-- TASK 5: Covering Index on Transactions
------------------------------------------------------------------------------------------------------------------------
-- Description: Create a covering index that includes account_id and transaction_type to optimize the query.
--
-- Index Name: transaction_amount_covering_idx
--
-- Analyze the query with EXPLAIN ANALYZE.
-- Query:
SELECT account_id, transaction_type FROM transactions WHERE amount < 50;
-- TODO: Create Index:
CREATE INDEX transaction_amount_covering_idx ON transactions(amount) INCLUDE (account_id, transaction_type);
-- After index creation, EXPLAIN ANALYZE these queries:
--
-- 1. SELECT account_id, transaction_type FROM transactions WHERE amount < 50;
--
-- 2. SELECT account_id, transaction_type, transaction_date FROM transactions WHERE amount < 50; (add transaction_date column to query)
--
-- Pay attention on (Execution Time)
