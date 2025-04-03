-- This transaction is used to demonstrate the non-repeatable read anomaly.
-- It retrieves the account balance twice within the same transaction.
-- and the transaction 1-2-... execute update concurrently.

--The goal is to show that the balance can change between the two reads.
BEGIN;
----------------------------------
--WORKING AREA

    --set correct isolation level
    SET TRANSACTION ISOLATION LEVEL READ COMMITTED;

    --this is metadata, just ignore this row
    CREATE TEMP TABLE first_read AS
    --retrieve account balance for account with id 1


    --this is separator for threads, do not remove it
    --WAIT_HERE

    --retrieve account balance again for account with id 1

--WORKING AREA
----------------------------------
INSERT INTO isolation_level_test (isolation_level, first_read, second_read)
    SELECT 'READ COMMITTED',
           (SELECT balance FROM first_read), second_read
    FROM (SELECT balance AS second_read FROM accounts WHERE account_id = 1) as t;
COMMIT;