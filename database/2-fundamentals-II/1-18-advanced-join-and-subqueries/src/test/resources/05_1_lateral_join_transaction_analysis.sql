-- ================================================================================
-- TASK 5: LATERAL JOIN - Advanced Transaction Analysis
-- ================================================================================
-- LEARNING OBJECTIVE: Master LATERAL JOINs for correlated subqueries
-- and row-by-row processing scenarios
--
-- BUSINESS SCENARIO: For each account, get the latest transaction details
-- along with transaction velocity analysis
--
-- DIFFICULTY: Hard
-- FILE: 05_1_lateral_join_transaction_analysis.sql

-- Task 5.1: Latest Transaction Analysis per Account
-- Get the most recent transaction for each account with context
SELECT
    a.id AS account_id,
    CONCAT(c.first_name, ' ', c.last_name) AS account_holder,
    a.account_type,
    a.balance AS current_balance,
    latest_trans.transaction_type AS last_transaction_type,
    latest_trans.amount AS last_transaction_amount,
    latest_trans.transaction_date AS last_transaction_date,
    EXTRACT(DAY FROM CURRENT_TIMESTAMP - latest_trans.transaction_date) AS days_since_last_transaction,
    -- Calculate account activity level
    CASE
        WHEN EXTRACT(DAY FROM CURRENT_TIMESTAMP - latest_trans.transaction_date) <= 7 THEN 'VERY_ACTIVE'
        WHEN EXTRACT(DAY FROM CURRENT_TIMESTAMP - latest_trans.transaction_date) <= 30 THEN 'ACTIVE'
        WHEN EXTRACT(DAY FROM CURRENT_TIMESTAMP - latest_trans.transaction_date) <= 90 THEN 'MODERATE'
        ELSE 'DORMANT'
    END AS activity_level
FROM accounts a
JOIN customers c ON a.customer_id = c.id
LEFT JOIN LATERAL (
    SELECT
        transaction_type,
        amount,
        transaction_date,
        target_account_id
    FROM transactions t
    WHERE t.account_id = a.id
    ORDER BY t.transaction_date DESC
    LIMIT 1
) latest_trans ON true
ORDER BY
    CASE
        WHEN latest_trans.transaction_date IS NULL THEN 1
        ELSE 0
    END,
    latest_trans.transaction_date DESC;
