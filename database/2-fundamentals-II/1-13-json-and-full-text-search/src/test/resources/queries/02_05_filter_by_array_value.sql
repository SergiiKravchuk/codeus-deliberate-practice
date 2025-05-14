-- ===================================================================================================
-- EXERCISE 2.5 Filter by array value
-- ===================================================================================================
-- Description:
-- Search for title and details that has 'freedom' in a tag list
-- Use search by inner array field

-- ===================================================================================================
-- WORKING AREA 👇

SELECT title, details
FROM history_facts
WHERE details -> 'tags' @> '["freedom"]';

