--This transaction should update the balance of account_id 2
--withdrawing $1000 if the initial balance is greater than or equal to $1500.
--It is not expecting that some transaction will be executed in parallel.
--
--It should demonstrate LOST_UPDATE anomaly.
BEGIN;
SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;

    -- Read the current total balance
    CREATE TEMP TABLE initial_balance AS
    SELECT balance
    FROM accounts
    WHERE account_id = 2;

    --WAIT_HERE

    -- Withdraw $1000 from account 2 if total balance >= 1500
    UPDATE accounts
    SET balance = balance - 1000
    WHERE account_id = 2
      AND (SELECT balance FROM initial_balance) >= 1500;

INSERT INTO serialization_test_results
    (isolation_level, transaction_id, initial_total, final_balance, status)
VALUES ('SERIALIZABLE',
        1,
        (SELECT balance FROM initial_balance),
        (SELECT balance FROM accounts WHERE account_id = 2),
        'COMMITTED');
COMMIT;
