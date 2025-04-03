--This transaction should update the balance of account_id 2
--by withdrawing $1000 if the initial balance is greater than or equal to $1500.
BEGIN;
SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;
----------------------------------
--WORKING AREA

    -- Read the current total balance
    CREATE TEMP TABLE initial_balance AS
    -- Select the balance of account 2

    -- Withdraw $1000 from account 2 if total balance >= 1500
    -- using update statement

--WORKING AREA
----------------------------------
INSERT INTO serialization_test_results
    (isolation_level, transaction_id, initial_total, final_balance, status)
VALUES ('SERIALIZABLE',
        2,
        (SELECT balance FROM initial_balance),
        (SELECT balance FROM accounts WHERE account_id = 2),
        'COMMITTED');
COMMIT;