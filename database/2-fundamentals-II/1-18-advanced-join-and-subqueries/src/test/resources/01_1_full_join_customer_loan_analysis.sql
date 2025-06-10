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
SELECT
    c.id AS customer_id,
    CONCAT(c.first_name, ' ', c.last_name) AS customer_name,
    c.email,
    l.id AS loan_id,
    l.amount,
    l.status,
    l.interest_rate,
    CASE
        WHEN c.id IS NULL THEN 'ORPHANED_LOAN'
        WHEN l.id IS NULL THEN 'NO_LOANS'
        ELSE 'HAS_LOANS'
    END AS relationship_status
FROM customers c
FULL OUTER JOIN loans l ON c.id = l.customer_id
ORDER BY
    CASE WHEN c.id IS NULL THEN 1 ELSE 0 END,  -- Orphaned loans first
    c.id,
    l.amount DESC;