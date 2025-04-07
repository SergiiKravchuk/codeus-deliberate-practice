------------------------------------------------------------------------------------------------------------------------
-- TASK 12: Investigate Unused Index Due to Column Order in Composite Index
------------------------------------------------------------------------------------------------------------------------
--
-- DESCRIPTION:
-- A composite index was created on (target_account_id, transaction_date).
-- However, a query filtering only by transaction_date does NOT use the index.
--
------------------------------------------------------------------------------------------------------------------------
-- STEP 1: Create Composite Index
------------------------------------------------------------------------------------------------------------------------
CREATE INDEX idx_transactions_type_date ON transactions(target_account_id, transaction_date);
------------------------------------------------------------------------------------------------------------------------
-- STEP 2: EXPLAIN ANALYZE Query Filtering by transaction_date Only
------------------------------------------------------------------------------------------------------------------------
SELECT transaction_date, transaction_type, target_account_id
 FROM transactions
 WHERE transaction_date >= NOW() - interval '30 days';
--
-- Observation:
-- The index is not used because `target_account_id` is the leading column in the index,
-- but it's not part of the WHERE clause. PostgreSQL skips this index.
--
------------------------------------------------------------------------------------------------------------------------
-- STEP 3
-- TODO: Drop the Incorrect Index
------------------------------------------------------------------------------------------------------------------------
DROP INDEX idx_transactions_type_date;
------------------------------------------------------------------------------------------------------------------------
-- STEP 4
-- TODO: Create Correct Index with transaction_date as Leading Column
--
-- Index name: idx_transactions_date
--
------------------------------------------------------------------------------------------------------------------------
CREATE INDEX idx_transactions_date ON transactions(transaction_date);
------------------------------------------------------------------------------------------------------------------------
-- STEP 5: Re-run the Query and Observe Index Usage
-- Goal: Confirm that the correct index is now used
------------------------------------------------------------------------------------------------------------------------
SELECT transaction_date, transaction_type, target_account_id
 FROM transactions
 WHERE transaction_date >= NOW() - interval '30 days';
