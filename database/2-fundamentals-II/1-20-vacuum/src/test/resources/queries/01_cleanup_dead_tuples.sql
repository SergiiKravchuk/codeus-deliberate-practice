-- Exercise 1: Basic VACUUM
-- Description: Clean up dead tuples from the transactions table

-- Create some dead tuples for demonstration
UPDATE transactions SET amount = amount + 0.01 WHERE id % 2 = 0;
DELETE FROM transactions WHERE id % 10 = 0;

-- TODO: Implement your solution here
VACUUM transactions;
