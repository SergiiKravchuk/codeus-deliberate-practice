-- Second transaction: Attempts to update account 1 during the first transaction
BEGIN;
----------------------------------
--WORKING AREA

    -- Record the start time to measure how long we're blocked
    CREATE TEMP TABLE start_time AS
    SELECT clock_timestamp() AS time;

    -- Try to access the same account - this will block until the first transaction releases its lock
    -- Lock the account with FOR UPDATE to prevent concurrent modifications with id 1

    -- Record the time after acquiring the lock (to measure blocking duration)
    CREATE TEMP TABLE end_time AS
    SELECT clock_timestamp() AS time;

    -- Record initial balance after we get the lock
    CREATE TEMP TABLE account1_balance AS
    -- Select the balance of account 1

    --update statement is used to increment +100 balance of account with id 1


    -- Calculate how long we were blocked waiting for the lock
    CREATE TEMP TABLE blocking_duration AS
    SELECT extract(milliseconds from (SELECT time FROM end_time) - (SELECT time FROM start_time)) AS ms;

--WORKING AREA
----------------------------------
INSERT INTO lock_test_results
(transaction_id, lock_type, account_id, initial_balance, final_balance, success, execution_time_ms, blocking_time_ms)
VALUES (2,
        'PESSIMISTIC',
        1,
        (SELECT balance FROM account1_balance),
        (SELECT balance FROM accounts WHERE account_id = 1),
        true,
        extract(milliseconds from clock_timestamp() - transaction_timestamp()),
        (SELECT ms FROM blocking_duration));

COMMIT;