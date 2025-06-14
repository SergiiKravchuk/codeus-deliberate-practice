-- Exercise 2.3: VACUUM with Multiple Options
-- Description: Run VACUUM with detailed logs AND update table statistics in one command

-- Create some fresh dead tuples for this exercise
UPDATE transactions SET amount = amount + 0.03 WHERE id % 5 = 0;
DELETE FROM transactions WHERE id % 30 = 0;

-- TODO: Implement your solution here
VACUUM (VERBOSE, ANALYZE) transactions;