-- ================================================================================
-- FILE: 06_4_performance_comparison_explain.sql (For performance analysis)
-- This file contains EXPLAIN statements for performance comparison
-- ================================================================================

-- Performance analysis queries (run separately for analysis)(>= 500)
-- Method 1: EXISTS
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT c.id, CONCAT(c.first_name, ' ', c.last_name) AS customer_name, c.email
FROM customers c
WHERE EXISTS (
    SELECT 1 FROM accounts a JOIN transactions t ON a.id = t.account_id
    WHERE a.customer_id = c.id AND t.amount >= 500
);

-- Method 2: IN
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT c.id, CONCAT(c.first_name, ' ', c.last_name) AS customer_name, c.email
FROM customers c
WHERE c.id IN (
    SELECT DISTINCT a.customer_id FROM accounts a JOIN transactions t ON a.id = t.account_id
    WHERE t.amount >= 500 AND a.customer_id IS NOT NULL
);

-- Method 3: JOIN
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT DISTINCT c.id, CONCAT(c.first_name, ' ', c.last_name) AS customer_name, c.email
FROM customers c JOIN accounts a ON c.id = a.customer_id JOIN transactions t ON a.id = t.account_id
WHERE t.amount >= 500;