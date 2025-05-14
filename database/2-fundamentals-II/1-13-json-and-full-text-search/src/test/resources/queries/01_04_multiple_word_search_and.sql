-- ===================================================================================================
-- EXERCISE 1.4 Multiple word search with AND
-- ===================================================================================================
-- Description:
-- Search for person and quote with both words 'imagine', 'impossible'.
-- Please add 'english' language as a parameter

-- ===================================================================================================
-- WORKING AREA 👇

SELECT person, quote
FROM quotes
WHERE to_tsvector('english', quote) @@ to_tsquery('english', 'imagine & impossible');