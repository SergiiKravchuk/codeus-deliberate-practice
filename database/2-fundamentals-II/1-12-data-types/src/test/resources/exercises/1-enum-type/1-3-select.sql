-- ===================================================================================================
-- EXERCISE 1.3 Select data using the new ENUM type
-- ===================================================================================================
-- Description:
--  Write a query to select all `account_id` and `status` for rows where `status` = 'active'.
-- ===================================================================================================
-- WORKING AREA 👇
SELECT id AS account_id, status
FROM accounts
WHERE status = 'active';
