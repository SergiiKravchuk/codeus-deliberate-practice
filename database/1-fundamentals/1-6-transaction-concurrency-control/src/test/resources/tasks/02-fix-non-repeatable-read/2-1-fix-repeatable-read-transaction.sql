-- This transaction is used to test the REPEATABLE READ isolation level.
-- It reads the balance of account_id 1 twice and inserts the results into the isolation_level_test table.
-- The first read is stored in a temporary table, and the second read is done after a wait point.
-- The transaction is committed at the end.
-- The purpose of this transaction is to demonstrate that the balance remains consistent across multiple reads
-- within the same transaction, even if other transactions modify the data in between.
-- The point is where you can simulate a delay or wait for another transaction to modify the data.

BEGIN;
----------------------------------
--WORKING AREA

    SET TRANSACTION ISOLATION LEVEL REPEATABLE READ;

    CREATE TEMP TABLE first_read AS
    SELECT balance
    FROM accounts
    WHERE account_id = 1;

    --WAIT_HERE

    CREATE TEMP TABLE second_read AS
    SELECT balance
    FROM accounts
    WHERE account_id = 1;

--WORKING AREA
----------------------------------
INSERT INTO isolation_level_test (isolation_level, first_read, second_read)
SELECT 'REPEATABLE READ',
       (SELECT balance FROM first_read),
       (SELECT balance FROM second_read);
COMMIT;