-- ===================================================================================================
-- EXERCISE 1.5 Multiple word search with OR
-- ===================================================================================================
-- Description:
-- Search for person and quote with any of words 'imagine', 'impossible'.
-- Please add 'english' language as a parameter

-- ===================================================================================================
-- WORKING AREA 👇

SELECT person, quote
FROM quotes
WHERE to_tsvector('english', quote) @@ to_tsquery('english', 'imagine | impossible');