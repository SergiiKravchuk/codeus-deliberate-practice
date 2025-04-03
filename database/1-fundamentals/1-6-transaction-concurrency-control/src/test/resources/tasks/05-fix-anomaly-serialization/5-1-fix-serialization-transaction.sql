--This transaction fix the SERIALIZATION anomaly using SERIALIZABLE isolation level.
BEGIN;
----------------------------------
--WORKING AREA

    --set isolation level to SERIALIZABLE

    -- Read the current total balance
    CREATE TEMP TABLE initial_balance AS

    --select the balance of account 2

    --WAIT_HERE

    -- Withdraw $1000 from account 2 if total balance >= 1500
    -- using update statement


--WORKING AREA
----------------------------------
INSERT INTO serialization_test_results
    (isolation_level, transaction_id, initial_total, final_balance, status)
VALUES ('SERIALIZABLE',
        1,
        (SELECT balance FROM initial_balance),
        (SELECT balance FROM accounts WHERE account_id = 2),
        'COMMITTED');
COMMIT;
