-- ===================================================================================================
-- PROBLEM 8
-- ===================================================================================================
--Description:
-- A support dashboard allows staff to search for customers by partial first name.
-- These searches use LIKE 'Ann%', but the current query performs full scans.
--
--TODO:
-- Enable efficient indexed lookups for prefix-matching LIKE.
--
--Query:
-- SELECT id, email, phone
-- FROM customers
-- WHERE first_name LIKE 'Ann%';
-- ===================================================================================================
-- WORKING AREA
