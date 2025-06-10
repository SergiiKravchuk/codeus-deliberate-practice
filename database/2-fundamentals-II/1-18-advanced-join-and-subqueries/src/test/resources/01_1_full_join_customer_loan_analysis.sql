-- ================================================================================
-- TASK 1: FULL OUTER JOIN - Customer-Loan Relationship Analysis
-- ================================================================================
-- LEARNING OBJECTIVE: Understand when FULL JOIN reveals gaps in data relationships
--
-- BUSINESS SCENARIO: The bank wants to identify:
-- 1. Customers who have loans (normal case)
-- 2. Customers who DON'T have loans (potential loan prospects)
-- 3. Orphaned loans without valid customer records (data integrity issues)
--
-- DIFFICULTY: Medium
-- FILE: 01_1_full_join_customer_loan_analysis.sql

-- Task 1.1: Basic Full Join Analysis
-- Find ALL customers and ALL loans, showing the complete relationship picture
-- Include customers without loans and any orphaned loan records

-- TODO:
-- write your code here