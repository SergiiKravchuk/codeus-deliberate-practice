BEGIN;
----------------------------------
--WORKING AREA

    SET TRANSACTION ISOLATION LEVEL READ COMMITTED;

    CREATE TEMP TABLE first_read AS
    SELECT account_id, balance
    FROM accounts
    WHERE currency = 'EUR';

    --WAIT_HERE

    CREATE TEMP TABLE second_read AS
    SELECT account_id, balance
    FROM accounts
    WHERE currency = 'EUR';

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