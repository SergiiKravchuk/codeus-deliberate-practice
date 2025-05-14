-- ===================================================================================================
-- EXERCISE 1.2 Basic plainto usage
-- ===================================================================================================
-- Description:
-- Search for person and quote with 'create the impossible' text using tsquery with plainto.
-- Please add 'english' language as a parameter

-- ===================================================================================================
-- WORKING AREA 👇

SELECT person, quote
FROM quotes
WHERE to_tsvector('english', quote) @@ plainto_tsquery('english', 'create the impossible');