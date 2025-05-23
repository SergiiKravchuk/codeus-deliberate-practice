-- ===================================================================================================
-- PROBLEM 1
-- ===================================================================================================
--Description:
-- Multiple API clients of the application fetch account ids data created for specific time interval.
-- During peak hours, multiple simultaneous reads affect database performance.
--
--TODO:
-- Analyze and optimize the query to reduce time spend on reading.
--
--Typical Query:
-- SELECT id
-- FROM accounts
-- WHERE created_at > NOW() - INTERVAL '10 days';
-- ===================================================================================================
-- WORKING AREA 👇