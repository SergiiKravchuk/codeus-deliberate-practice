-- ================================================================================
-- TASK 7: INTERSECT - Compliance and Audit Queries
-- ================================================================================
-- LEARNING OBJECTIVE: Use INTERSECT for finding common elements
-- between datasets for compliance and audit purposes
--
-- BUSINESS SCENARIO: Identify customers who appear in both
-- high-balance accounts AND high-value loans (VIP customers)
--
-- DIFFICULTY: Medium
-- FILE: 07_1_intersect_vip_analysis.sql

-- Task 7.1: Find VIP customers (high balance + high loans)
WITH high_balance_customers AS (
    SELECT customer_id
    FROM accounts
    WHERE balance > 5000
),
high_loan_customers AS (
    SELECT customer_id
    FROM loans
    WHERE amount > 10000 AND status = 'active'
)
SELECT
    c.id AS customer_id,
    CONCAT(c.first_name, ' ', c.last_name) AS customer_name,
    c.email,
    COALESCE(SUM(a.balance), 0) AS total_account_balance,
    COALESCE(SUM(l.amount), 0) AS total_active_loans,
    COUNT(DISTINCT a.id) AS number_of_accounts,
    COUNT(DISTINCT l.id) AS number_of_active_loans
FROM customers c
JOIN accounts a ON c.id = a.customer_id
JOIN loans l ON c.id = l.customer_id AND l.status = 'active'
WHERE c.id IN (
    SELECT customer_id FROM high_balance_customers
    INTERSECT
    SELECT customer_id FROM high_loan_customers
)
GROUP BY c.id, c.first_name, c.last_name, c.email
ORDER BY (COALESCE(SUM(a.balance), 0) + COALESCE(SUM(l.amount), 0)) DESC;