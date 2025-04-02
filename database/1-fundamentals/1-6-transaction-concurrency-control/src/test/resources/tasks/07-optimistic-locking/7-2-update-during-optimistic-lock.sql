-- Second transaction: Update account 1 to cause optimistic lock conflict
BEGIN;
-- Record the start time
CREATE TEMP TABLE start_time AS
SELECT clock_timestamp() AS time;

-- Read the current account data including version
SELECT account_id, balance, version FROM accounts WHERE account_id = 1;

-- Record the initial state
CREATE TEMP TABLE account1_initial AS
SELECT balance, version FROM accounts WHERE account_id = 1;

-- Update the account, which increments the version
UPDATE accounts
SET balance = balance + 100, version = version + 1
WHERE account_id = 1;

-- Record end time
CREATE TEMP TABLE end_time AS
SELECT clock_timestamp() AS time;

-- Calculate execution duration
CREATE TEMP TABLE execution_duration AS
SELECT extract(milliseconds from (SELECT time FROM end_time) - (SELECT time FROM start_time)) AS ms;

-- Record the results
INSERT INTO lock_test_results
(transaction_id, lock_type, account_id, initial_balance, final_balance, success, execution_time_ms, version_before, version_after)
VALUES (
           2,
           'OPTIMISTIC',
           1,
           (SELECT balance FROM account1_initial),
           (SELECT balance FROM accounts WHERE account_id = 1),
           true,
           (SELECT ms FROM execution_duration),
           (SELECT version FROM account1_initial),
           (SELECT version FROM accounts WHERE account_id = 1)
       );
COMMIT;