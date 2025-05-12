-- ===================================================================================================
-- EXERCISE 4.1 Declare COMPOSITE type
-- ===================================================================================================
-- Description:
--  Declare a COMPOSITE type `full_name` with fields (first TEXT, last TEXT) and
--  alter `customers` to add a column `full_name` of this type.
-- ===================================================================================================
-- WORKING AREA 👇
CREATE TYPE full_name AS (
    first TEXT,
    last TEXT
    );

ALTER TABLE customers
    ADD COLUMN full_name full_name;