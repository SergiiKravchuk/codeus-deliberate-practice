-- ===================================================================================================
-- EXERCISE 4.4 Modify the exising COMPOSITE type
-- ===================================================================================================
-- Description:
--  Alter the composite type `full_name` to add a field `middle TEXT`,
--  then update one row to include a middle name: add middle 'Marie' for customer with ID=1
-- ===================================================================================================
-- WORKING AREA 👇
ALTER TYPE full_name
    ADD ATTRIBUTE middle TEXT;

UPDATE customers
SET full_name = ROW(first_name, 'Marie', last_name)::full_name
WHERE id = 1;