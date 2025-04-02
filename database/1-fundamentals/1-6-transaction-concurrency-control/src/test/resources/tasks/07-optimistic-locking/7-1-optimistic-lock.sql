-- First transaction: Transfer $500 from account 1 to account 2 using optimistic locking
BEGIN;

-- Read the current account data including version
SELECT account_id, balance, version
FROM accounts
WHERE account_id = 1;

-- Record the initial state
CREATE TEMP TABLE account1_initial AS
SELECT balance, version
FROM accounts
WHERE account_id = 1;

--WAIT_HERE

-- Update the source account with version check
UPDATE accounts
SET balance = balance - 500,
    version = version + 1
WHERE account_id = 1
  AND version = (SELECT version FROM account1_initial);

-- Update the target account
UPDATE accounts
SET balance = balance + 500,
    version = version + 1
WHERE account_id = 2;

-- Record the results
INSERT INTO lock_test_results
(transaction_id, lock_type, account_id, initial_balance, final_balance, success, execution_time_ms, version_before,
 version_after)
SELECT 1,
       'OPTIMISTIC',
       1,
       ai.balance,
       (SELECT balance FROM accounts WHERE account_id = 1),
       (SELECT version FROM accounts WHERE account_id = 1) = (SELECT version FROM account1_initial),
       extract(milliseconds from clock_timestamp() - transaction_timestamp()),
       ai.version,
       (SELECT version FROM accounts WHERE account_id = 1)
FROM account1_initial ai;

COMMIT;