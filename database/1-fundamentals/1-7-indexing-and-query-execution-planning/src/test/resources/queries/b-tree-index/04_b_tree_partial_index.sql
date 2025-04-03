------------------------------------------------------------------------------------------------------------------------
-- TASK 4: Partial Index on Accounts
------------------------------------------------------------------------------------------------------------------------
-- Description: Create a partial B-Tree index on the account type column to
-- optimize queries filtering for 'transfer' accounts.
--
-- Index Name: account_type_idx
--
-- Analyze the query with EXPLAIN ANALYZE.
-- Query:
SELECT account_type FROM accounts WHERE account_type = 'transfer';
-- TODO: Create index:
CREATE INDEX account_type_idx on accounts(account_type) where account_type = 'transfer';
-- After index creation, check these queries:
-- SELECT account_type FROM accounts WHERE account_type = 'transfer';
-- SELECT account_type FROM accounts WHERE account_type = 'withdrawal';
