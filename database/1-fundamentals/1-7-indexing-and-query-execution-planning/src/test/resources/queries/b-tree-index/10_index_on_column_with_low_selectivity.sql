-- ===================================================================================================
-- TASK 10: Improve Query Performance by Understanding Selectivity and Using Partial Indexes
-- ===================================================================================================
--
-- DESCRIPTION:
-- This task demonstrates how query performance can vary based on the selectivity of the values
-- in indexed columns. We'll explore how indexes behave differently when:
--   - Querying a low-selectivity value ('deposit' = 80% of rows)
--   - Querying a more selective value ('transfer` = 10% of rows)
--
-- =======================================================================================
-- STEP 1: Initial Index and Query for a Low Selectivity Value (80% = 'deposit')
-- =======================================================================================
--
-- GIVEN INDEX:
CREATE INDEX idx_transactions_type ON transactions(transaction_type);
--
-- EXPLAIN ANALYZE QUERY:
SELECT * FROM transactions
 WHERE transaction_type = 'deposit';
--
--  Observation:
-- 'deposit' is a low selectivity value (e.g., 80% of rows), so PostgreSQL may choose
-- to ignore the index and use a sequential scan instead.
--
-- =======================================================================================
-- STEP 2: Use a More Selective Value (10% = 'transfer')
-- =======================================================================================
--
-- EXPLAIN ANALYZE QUERY:
SELECT * FROM transactions
 WHERE transaction_type = 'transfer';
--
-- Observation:
-- This value is more selective, so PostgreSQL is more likely to use the index `idx_transactions_type`.
--
-- Optimization:
-- For values such as 'transfer', you can reduce memory consumption even further by using a partial index,
-- which can also work in combination with other filters.
--
-- TODO: DROP INDEX idx_transactions_type:
--

--
-- TODO: CREATE PARTIAL INDEX (for 'transfer'):
--
-- Index name: idx_transactions_transfer_partial
--

--
-- EXPLAIN ANALYZE query again:
--
SELECT * FROM transactions
 WHERE transaction_type = 'transfer';
-- =======================================================================================
