-- ===================================================================================================
-- PROBLEM 2: Update Transaction
-- ===================================================================================================
--Description:
-- This transaction updates balance for account id 4 which has EUR currency.
-- It is executed during the Read Transaction.

--TODO:
-- Analyze and set a proper Isolation Level (if needed).
------------------------------------------------------------------------------------------------------
BEGIN;
----------------------------------
--WORKING AREA

--WORKING AREA
------------------------------------
    UPDATE accounts
    SET balance = balance + 10000
    WHERE account_id = 4;
END;
