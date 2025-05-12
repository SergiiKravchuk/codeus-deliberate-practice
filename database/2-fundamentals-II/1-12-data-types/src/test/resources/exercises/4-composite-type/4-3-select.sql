-- ===================================================================================================
-- EXERCISE 4.3 Select data using the new COMPOSITE type
-- ===================================================================================================
-- Description:
--  Query `customers` to select `id` and `full_name` for all rows where last name part of the `full_name` ends with 'son'.
-- ===================================================================================================
-- WORKING AREA 👇

SELECT id, full_name
FROM customers
WHERE (full_name).last LIKE '%son';