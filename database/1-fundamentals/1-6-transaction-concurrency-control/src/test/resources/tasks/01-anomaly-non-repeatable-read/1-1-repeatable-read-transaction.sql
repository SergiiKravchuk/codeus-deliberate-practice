BEGIN;
----------------------------------
--WORKING AREA

    SET TRANSACTION ISOLATION LEVEL READ COMMITTED;

    CREATE TEMP TABLE first_read AS
    SELECT balance
    FROM accounts
    WHERE account_id = 1;

    --WAIT_HERE

    SELECT balance AS second_read
    FROM accounts
    WHERE account_id = 1;

--WORKING AREA
----------------------------------
INSERT INTO isolation_level_test (isolation_level, first_read, second_read)
    SELECT 'READ COMMITTED',
           (SELECT balance FROM first_read), second_read
    FROM (SELECT balance AS second_read FROM accounts WHERE account_id = 1) as t;
COMMIT;