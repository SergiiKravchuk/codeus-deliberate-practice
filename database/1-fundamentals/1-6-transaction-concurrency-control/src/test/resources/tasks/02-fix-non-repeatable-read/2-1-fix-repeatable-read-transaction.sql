--This is a test script for the task of fixing non-repeatable reads in a transaction.
-- It demonstrates how to set the isolation level to REPEATABLE READ,
-- perform two reads of the same data, and insert the results into a test table.
--
-- The script includes a placeholder for a wait point to simulate concurrent transactions.
-- The goal is to ensure that the balance remains consistent across multiple reads
BEGIN;
----------------------------------
--WORKING AREA

    --set correct isolation level as REPEATABLE READ

    CREATE TEMP TABLE first_read AS
    --read balance from accounts where account_id = 1;

    --WAIT_HERE

    --read again balance from accounts where account_id = 1;

--WORKING AREA
----------------------------------
INSERT INTO isolation_level_test (isolation_level, first_read, second_read)
SELECT 'REPEATABLE READ',
       (SELECT balance FROM first_read),
       (SELECT balance FROM second_read);
COMMIT;