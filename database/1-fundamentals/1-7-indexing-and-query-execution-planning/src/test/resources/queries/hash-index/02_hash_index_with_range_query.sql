--------------------------------------------------------------------------------
-- Task 2: Demonstrate Hash Index Limitation with a Range Query
--------------------------------------------------------------------------------
-- Description:
-- Hash indexes in PostgreSQL support equality operators but not range queries.
-- In this task, create a hash index on a numeric column (accounts.balance) and observe
-- that it is not used when the query uses a range condition.
--
-- TODO: Create a hash index on the accounts.balance column:
CREATE INDEX idx_accounts_balance_hash ON accounts USING hash (balance);
--
-- EXPLAIN ANALYZE QUERY:
SELECT * FROM accounts WHERE balance > 5000;
--
-- ISSUE:
-- Since hash indexes do not support range queries, the execution plan should show that
-- PostgreSQL ignores the hash index (likely opting for a sequential scan).
--