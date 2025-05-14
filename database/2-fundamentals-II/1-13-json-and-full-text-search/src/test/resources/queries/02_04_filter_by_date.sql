-- ===================================================================================================
-- EXERCISE 2.4 Filter by date
-- ===================================================================================================
-- Description:
-- Search for title and details that happened earlier than 1800
-- Use int casting

-- ===================================================================================================
-- WORKING AREA 👇

SELECT title, details
FROM history_facts
WHERE (details ->> 'year')::int < 1800;

