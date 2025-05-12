-- ===================================================================================================
-- EXERCISE 3.3 Select data using the new RANGE type
-- ===================================================================================================
-- Description:
--  Query `accounts` to find `account_id` where `credit_limit` includes the value 5000.
-- ===================================================================================================
-- WORKING AREA 👇

SELECT id AS account_id
FROM accounts
WHERE credit_limit @> 5000::NUMERIC;