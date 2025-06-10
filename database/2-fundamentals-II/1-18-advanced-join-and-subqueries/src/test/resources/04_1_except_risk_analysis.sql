-- ================================================================================
-- TASK 4: EXCEPT - Risk Analysis and Compliance
-- ================================================================================
-- LEARNING OBJECTIVE: Use EXCEPT for identifying missing relationships
-- and compliance gaps in banking operations
--
-- BUSINESS SCENARIO: Identify customers who have accounts but no loans
-- These are potential loan prospects with established banking relationships
--
-- DIFFICULTY: Medium-Hard
-- FILE: 04_1_except_risk_analysis.sql

-- Task 4.1: Identify Loan Prospects

-- Using EXCEPT (more explicit set operation)

WITH account_holders AS (
    SELECT DISTINCT customer_id FROM accounts
),
active_loan_holders AS (
    SELECT DISTINCT customer_id FROM loans WHERE status = 'active'
),
prospects AS (
    SELECT customer_id
    FROM account_holders
    EXCEPT
    SELECT customer_id FROM active_loan_holders
)
SELECT
    c.id AS customer_id,
    CONCAT(c.first_name, ' ', c.last_name) AS customer_name,
    c.email,
    COUNT(a.id) AS account_count,
    SUM(a.balance) AS total_balance,
    CASE
        WHEN SUM(a.balance) > 10000 THEN 'HIGH_VALUE_PROSPECT'
        WHEN SUM(a.balance) > 5000 THEN 'MEDIUM_VALUE_PROSPECT'
        ELSE 'STANDARD_PROSPECT'
    END AS prospect_category
FROM customers c
JOIN accounts a ON c.id = a.customer_id
WHERE c.id IN (SELECT customer_id FROM prospects)
GROUP BY c.id, c.first_name, c.last_name, c.email
ORDER BY total_balance DESC;