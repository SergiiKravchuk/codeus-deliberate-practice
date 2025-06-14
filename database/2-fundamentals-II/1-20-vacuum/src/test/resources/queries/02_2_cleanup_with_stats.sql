-- Exercise 2.2: VACUUM with Statistics Update
-- Description: Clean dead tuples and update table statistics

-- Create some dead tuples for cleanup demonstration
UPDATE transactions SET amount = amount + 0.10 WHERE id % 3 = 0;
DELETE FROM transactions WHERE id % 15 = 0;

-- TODO: Implement your solution here
VACUUM ANALYZE transactions;

