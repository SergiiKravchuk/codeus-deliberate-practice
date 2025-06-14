-- Exercise 3: Complete Table Rewrite
-- Description: Reclaim all disk space from heavily bloated table (use bloated_transactions table)

-- Create significant table bloat for demonstration
UPDATE transactions SET amount = amount + 1 WHERE id % 2 = 0;
UPDATE transactions SET amount = amount - 1 WHERE id % 2 = 0;
DELETE FROM transactions WHERE id % 20 = 0;

-- TODO: Implement your solution here
