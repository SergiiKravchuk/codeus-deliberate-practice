-- ===================================================================================================
-- EXERCISE 4.2 Insert data using the new COMPOSITE type
-- ===================================================================================================
-- Description:
--  Update existing `customers` rows to populate `full_name` from `first_name` and `last_name`.
-- ===================================================================================================
-- WORKING AREA 👇

UPDATE customers
SET full_name = ROW(first_name, last_name)::full_name;