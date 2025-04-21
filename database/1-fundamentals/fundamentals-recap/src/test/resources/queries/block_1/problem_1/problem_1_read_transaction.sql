-- ===================================================================================================
-- PROBLEM 1: Read Transaction
-- ===================================================================================================
--Description:
-- This transaction reads the balance of account_id 1 twice.
-- The first read is stored in a temporary table, and the second read is done after a wait point.
--
--TODO:
-- Analyze and set a proper Isolation Level (if needed).
------------------------------------------------------------------------------------------------------
----------------------------------
--WORKING AREA
    SET TRANSACTION ISOLATION LEVEL REPEATABLE READ;
--WORKING AREA
----------------------------------

    CREATE TEMP TABLE first_read AS
    SELECT balance
    FROM accounts
    WHERE account_id = 1;

    --WAIT_HERE

    CREATE TEMP TABLE second_read AS
    SELECT balance
    FROM accounts
    WHERE account_id = 1;
