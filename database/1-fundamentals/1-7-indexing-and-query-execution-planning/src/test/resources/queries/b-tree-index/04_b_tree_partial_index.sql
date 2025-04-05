-- TASK 4: Partial Index on Accounts
--
-- Create a partial B-Tree index on the account type column to optimize queries filtering for 'savings' accounts.
--
-- Index Name: account_type_idx
--
-- Analyze the query with EXPLAIN ANALYZE.
-- Query:
SELECT account_type FROM accounts WHERE account_type = 'savings';
-- Create index

-- After index creation, check these queries:
-- SELECT account_type FROM accounts WHERE account_type = 'savings';
-- SELECT account_type FROM accounts WHERE account_type = 'checking';

create index account_type_idx on accounts(account_type) where account_type = 'savings';
--
-- Expected solution:
-- create index account_type_idx on accounts(account_type) where account_type = 'savings';