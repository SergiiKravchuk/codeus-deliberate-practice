-- ================================================================================
-- FILE: 06_3_performance_comparison_join.sql
-- Alternative using JOIN method
-- ================================================================================

-- Method 3: Using JOIN (when you need additional data)(>= 500)
SELECT DISTINCT
    c.id as customer_id,
    CONCAT(c.first_name, ' ', c.last_name) AS customer_name,
    c.email,
    c.phone
FROM customers c
JOIN accounts a ON c.id = a.customer_id
JOIN transactions t ON a.id = t.account_id
WHERE t.amount >= 500;
