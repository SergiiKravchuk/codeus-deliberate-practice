-- ===================================================================================================
-- EXERCISE 1.4 Modify the exising ENUM type
-- ===================================================================================================
-- Description:
--  Alter the enum type `account_status` to add a new value `suspended`,
--  then update one row to include the new `account_status`: add status 'suspended' for account with ID=101
-- ===================================================================================================
-- WORKING AREA 👇

ALTER TYPE account_status ADD VALUE 'suspended';
COMMIT;
UPDATE accounts
SET status = 'suspended'
WHERE id = 101;
