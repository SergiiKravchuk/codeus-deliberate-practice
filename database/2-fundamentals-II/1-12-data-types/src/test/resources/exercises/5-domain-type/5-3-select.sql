-- ===================================================================================================
-- EXERCISE 5.3 Select data using the new DOMAIN type
-- ===================================================================================================
-- Description:
--  Query `customers` to select `id` and `local_phone` for all rows where `local_phone` starts with '1'
-- ===================================================================================================
-- WORKING AREA 👇
SELECT id, local_phone
FROM customers
WHERE local_phone LIKE '1%';