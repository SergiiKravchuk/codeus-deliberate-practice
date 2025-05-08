-- ===================================================================================================
-- PROBLEM 3: Read Transaction
-- ===================================================================================================
--Description:
-- This transaction reads a set of data - account balance for a specific currency (EUR) twice within
-- the same transaction.
--
--TODO:
-- Analyze and set a proper Isolation Level (if needed).
------------------------------------------------------------------------------------------------------
----------------------------------
--WORKING AREA
    SET TRANSACTION ISOLATION LEVEL REPEATABLE READ;
--WORKING AREA
------------------------------------

    CREATE TEMP TABLE first_read AS
    SELECT account_id, balance
    FROM accounts
    WHERE currency = 'EUR';

    --WAIT_HERE

    CREATE TEMP TABLE second_read AS
    SELECT account_id, balance
    FROM accounts
    WHERE currency = 'EUR';
