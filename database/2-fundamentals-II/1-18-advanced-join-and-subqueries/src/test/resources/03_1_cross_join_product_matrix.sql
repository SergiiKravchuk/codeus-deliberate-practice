-- ================================================================================
-- TASK 3: CROSS JOIN - Product Recommendation Matrix
-- ================================================================================
-- LEARNING OBJECTIVE: Understand CROSS JOIN for generating all possible combinations
-- and its practical applications in business analysis
--
-- BUSINESS SCENARIO: Generate a matrix of all customers vs all account types
-- to identify cross-selling opportunities
--
-- DIFFICULTY: Hard
-- FILE: 03_1_cross_join_product_matrix.sql

-- Task 3.1: Customer-Account Type Opportunity Matrix
-- Generate all possible customer-account type combinations
-- Identify which customers don't have certain account types
WITH account_types AS (
    SELECT DISTINCT account_type FROM accounts
),
customer_current_accounts AS (
    SELECT
        c.id AS customer_id,
        c.first_name,
        c.last_name,
        a.account_type
    FROM customers c
    JOIN accounts a ON c.id = a.customer_id
)
SELECT
    c.id AS customer_id,
    CONCAT(c.first_name, ' ', c.last_name) AS customer_name,
    at.account_type,
    CASE
        WHEN cca.account_type IS NOT NULL THEN 'HAS_ACCOUNT'
        ELSE 'OPPORTUNITY'
    END AS opportunity_status,
    -- Calculate potential revenue if they open this account type
    CASE
        WHEN cca.account_type IS NULL AND at.account_type = 'checking' THEN 50.00
        WHEN cca.account_type IS NULL AND at.account_type = 'savings' THEN 25.00
        ELSE 0.00
    END AS potential_monthly_revenue
FROM customers c
CROSS JOIN account_types at
LEFT JOIN customer_current_accounts cca
    ON c.id = cca.customer_id
    AND at.account_type = cca.account_type
ORDER BY
    c.id,
    at.account_type;