-- ===================================================================================================
-- EXERCISE 1.1 Declare ENUM type
-- ===================================================================================================
-- Description:
--  Declare a new ENUM type `account_status` with values ('active', 'inactive', 'closed') and
--  alter the `accounts` table to add a column `status` of this type (not null, default 'active').

-- ===================================================================================================
-- WORKING AREA 👇
CREATE TYPE account_status AS ENUM ('active','inactive','closed');
ALTER TABLE accounts
    ADD COLUMN status account_status NOT NULL DEFAULT 'active';