-- ===================================================================================================
-- EXERCISE 1.3 Fulltext search using prefix
-- ===================================================================================================
-- Description:
-- Search for person and quote with prefix 'underst'.
-- Please add 'english' language as a parameter

-- ===================================================================================================
-- WORKING AREA 👇

SELECT person, quote
FROM quotes
WHERE to_tsvector('english', quote) @@ to_tsquery('english', 'underst:*');