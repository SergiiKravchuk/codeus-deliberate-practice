-- ===================================================================================================
-- EXERCISE 1.6 Create GIN index
-- ===================================================================================================
-- Description:
-- Create GIN index 'body_gin' for posts table and body column
-- Search for id and body in posts table where body contains either basic or advanced.
-- Don't use to_tsvector.
-- Please add 'english' language as a parameter

-- ===================================================================================================
-- WORKING AREA 👇

CREATE INDEX body_gin
ON posts
USING GIN ((to_tsvector('english', body)));

SELECT id, body
FROM posts
WHERE body @@ to_tsquery('basic | advanced');

