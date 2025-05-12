-- ===================================================================================================
-- EXERCISE 3.1 Declare RANGE type
-- ===================================================================================================
-- Description:
--  Create a new RANGE type `balance_range` as NUMERIC RANGE and alter `accounts` to add a column `credit_limit` of this type.
-- ===================================================================================================
-- WORKING AREA 👇
CREATE TYPE balance_range AS RANGE (SUBTYPE = NUMERIC);
ALTER TABLE accounts
    ADD COLUMN credit_limit balance_range;