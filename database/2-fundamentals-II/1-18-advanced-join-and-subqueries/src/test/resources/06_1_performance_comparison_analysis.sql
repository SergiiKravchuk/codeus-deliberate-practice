-- ================================================================================
-- FILE: 06_1_performance_comparison_analysis.sql
-- Main query for the test - using EXISTS method (most efficient for existence checks)
-- ================================================================================

-- Task 6.1: Find customers with high-value transactions (>= 500)
-- Using EXISTS (recommended for checking existence)
SELECT
    c.id as customer_id,
    CONCAT(c.first_name, ' ', c.last_name) AS customer_name,
    c.email,
    c.phone
FROM customers c
WHERE EXISTS (
    SELECT 1
    FROM accounts a
    JOIN transactions t ON a.id = t.account_id
    WHERE a.customer_id = c.id
    AND t.amount >= 500
);