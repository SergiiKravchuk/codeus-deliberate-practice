-- ===================================================================================================
-- PROBLEM 3: Update Transaction
-- ===================================================================================================
--Description:
-- This transaction insert a new account with EUR currency for customer id 1.
-- It is executed during the Read Transaction.

--TODO:
-- Analyze and set a proper Isolation Level (if needed).
------------------------------------------------------------------------------------------------------
BEGIN;
----------------------------------
--WORKING AREA
SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
--WORKING AREA
------------------------------------
INSERT INTO accounts (account_id, customer_id, balance, currency, version)
VALUES (5, 1, 500.00, 'EUR', 1);

END;
