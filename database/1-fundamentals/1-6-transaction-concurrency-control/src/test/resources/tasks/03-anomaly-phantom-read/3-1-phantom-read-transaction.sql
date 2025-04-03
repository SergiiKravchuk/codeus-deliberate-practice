--This transaction is used to demonstrate the phantom read anomaly.
-- It reads the set of data - account balance for a specific currency (EUR) twice within the same transaction.
-- The goal is to show that the set of data can change between the two reads.
--
-- The transaction is expected to be executed in a concurrent environment,
-- where another transaction may insert or delete rows that match the criteria between the two reads.
-- The purpose of this transaction is to illustrate the phantom read anomaly.
BEGIN;
----------------------------------
--WORKING AREA

    -- Set the isolation level to READ COMMITTED

    CREATE TEMP TABLE first_read AS
    --read account_id and balance from accounts where currency = 'EUR';

    --WAIT_HERE

    CREATE TEMP TABLE second_read AS
    --read account_id and balance from accounts where currency = 'EUR';


--WORKING AREA
------------------------------------
-- Insert results into isolation_level_test, one row per account
    INSERT INTO isolation_level_test (isolation_level, account_id, first_read, second_read)
    SELECT
        'READ COMMITTED',
        fr.account_id,
        fr.balance,
        sr.balance
    FROM
        first_read fr
            JOIN
    second_read sr ON fr.account_id = sr.account_id;
COMMIT;