-- First transaction: Transfer $500 from account 1 to account 2 using pessimistic locking
BEGIN;
    -- Lock the source account with FOR UPDATE to prevent concurrent modifications
    SELECT *
    FROM accounts
    WHERE account_id = 1 FOR UPDATE;

    -- Record initial balance
    CREATE TEMP TABLE account1_initial AS
    SELECT balance
    FROM accounts
    WHERE account_id = 1;

    --WAIT_HERE

    -- Update source account (withdraw)
    UPDATE accounts
    SET balance = balance - 500
    WHERE account_id = 1;

    -- Update target account (deposit)
    UPDATE accounts
    SET balance = balance + 500
    WHERE account_id = 2;

    -- Record the results
INSERT INTO lock_test_results
(transaction_id, lock_type, account_id, initial_balance, final_balance, success, execution_time_ms)
VALUES (1,
        'PESSIMISTIC',
        1,
        (SELECT balance FROM account1_initial),
        (SELECT balance FROM accounts WHERE account_id = 1),
        true,
        extract(milliseconds from clock_timestamp() - transaction_timestamp()));
COMMIT;