-- First transaction: Transfer $500 from account 1 to account 2 using optimistic locking
-- Optimistic Locking Example using versioning approach
BEGIN;
----------------------------------
--WORKING AREA

    -- Read the current account data including version
    -- select account_id the balance and version of account with id 1


    -- Record the initial state
    CREATE TEMP TABLE account1_initial AS
    -- select balance and version of account with id 1

    --WAIT_HERE

    -- Update the source account with version check
    -- update statement is used to withdraw -500 from balance and update version +1 of account with id 1
    -- current version should be compared with previous one and update performed only if they are the same,
    -- temp table account1_initial is used for this purpose

    -- Update the target account
    -- update statement is used to withdraw -500 from balance and update version +1 of account with id 2


--WORKING AREA
------------------------------------
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