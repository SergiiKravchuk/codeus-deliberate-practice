------------------------------------------------------------------------------------------------------------------------
-- TASK 8: Casting Prevents Index Usage
------------------------------------------------------------------------------------------------------------------------
-- Description: An index exists on transactions.transaction_date:
--
CREATE INDEX idx_transactions_date ON transactions(transaction_date);
-- Analyze the query with EXPLAIN ANALYZE.
-- Query:
SELECT * FROM transactions WHERE CAST(transaction_date AS DATE) = '2025-04-01';
--
-- ISSUE: Casting the column to a DATE disables the use of the simple B‑Tree index on transaction_date.
--
-- Task: Drop the current index and create a functional index with casting
-- TODO: Drop index:

--
-- TODO: Create Index:
--
-- Index Name: idx_transactions_date
--

-- After index creation: analyze the query with EXPLAIN ANALYZE again.
