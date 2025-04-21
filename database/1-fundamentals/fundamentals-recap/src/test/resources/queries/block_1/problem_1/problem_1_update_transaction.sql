-- ===================================================================================================
-- PROBLEM 1: Update Transaction
-- ===================================================================================================
--Description:
-- This transaction updates the balance of account_id 1.
-- It is executed during the Read Transaction.
--
--TODO:
-- Analyze and set a proper Isolation Level (if needed).
------------------------------------------------------------------------------------------------------

BEGIN;
----------------------------------
--WORKING AREA
SET TRANSACTION ISOLATION LEVEL REPEATABLE READ;
--WORKING AREA
----------------------------------

UPDATE accounts
SET balance = balance + 10
WHERE account_id = 1;
END;
