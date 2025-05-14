-- ===================================================================================================
-- EXERCISE 1.1 Basic tsvector / tsquery usage
-- ===================================================================================================
-- Description:
-- Search for person and quote that mention the word 'imagination'.
-- Use quotes table and tsvector with tsquery data types.
-- Please add 'english' language as a parameter

-- ===================================================================================================
-- WORKING AREA 👇
SELECT person, quote
FROM quotes
WHERE to_tsvector('english', quote) @@ to_tsquery('english', 'imagination');