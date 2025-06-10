-- ================================================================================
-- FILE: 06_2_performance_comparison_in.sql
-- Alternative using IN method
-- ================================================================================

-- Method 2: Using IN (can have issues with NULLs)(>= 500)
SELECT
    c.id as customer_id,
    CONCAT(c.first_name, ' ', c.last_name) AS customer_name,
    c.email,
    c.phone
FROM customers c
WHERE c.id IN (
    SELECT DISTINCT a.customer_id
    FROM accounts a
    JOIN transactions t ON a.id = t.account_id
    WHERE t.amount >= 500
    AND a.customer_id IS NOT NULL  -- Handle potential NULLs
);