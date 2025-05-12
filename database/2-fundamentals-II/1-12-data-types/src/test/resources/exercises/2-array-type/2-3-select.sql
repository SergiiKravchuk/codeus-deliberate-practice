-- ===================================================================================================
-- EXERCISE 2.3 Select data using the new ARRAY type
-- ===================================================================================================
-- Description:
--  Query `customers` to find all `id` and `tags` for rows where the 'premium' tag is present.
-- ===================================================================================================
-- WORKING AREA 👇

SELECT id, tags
FROM customers
WHERE tags @> ARRAY['premium']::TEXT[];