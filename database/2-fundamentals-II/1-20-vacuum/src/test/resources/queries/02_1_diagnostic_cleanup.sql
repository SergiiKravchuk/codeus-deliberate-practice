-- Exercise 2.1: VACUUM with Diagnostics
-- Description: Run VACUUM with detailed diagnostic output

-- First, create some dead tuples for cleanup demonstration
UPDATE transactions SET amount = amount + 0.10 WHERE id % 3 = 0;
DELETE FROM transactions WHERE id % 15 = 0;

-- TODO: Implement your solution here
VACUUM VERBOSE transactions;