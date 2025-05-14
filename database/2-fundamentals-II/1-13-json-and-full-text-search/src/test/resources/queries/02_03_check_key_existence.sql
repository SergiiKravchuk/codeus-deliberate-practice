-- ===================================================================================================
-- EXERCISE 2.3 Check key existence
-- ===================================================================================================
-- Description:
-- Search for title and details for the key 'tags'
-- Use ? operand

-- ===================================================================================================
-- WORKING AREA 👇

SELECT title, details
FROM history_facts
WHERE details ? 'tags';

