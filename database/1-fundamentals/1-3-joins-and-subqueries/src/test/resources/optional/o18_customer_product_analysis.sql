--Task: Comprehensive Customer Value Analysis
--
--Write a query to analyze customer value using multiple CTEs and weighted scoring.
--Requirements:
--- Use two CTEs: transaction_metrics and loan_metrics
--- In transaction_metrics CTE:
    -- Calculate total number of transactions
    -- Sum deposits and withdrawals separately
    -- Calculate average balance across all accounts
--- In loan_metrics CTE:
    -- Count total loans per customer
    -- Calculate total loan amount
    -- Calculate average interest rate
--- Main query should:
    -- Display customer's first and last name
    -- Show all metrics from both CTEs
    -- Calculate customer value score using weighted formula:
--       * 40% weight for average balance
--       * 30% weight for total deposits
--       * 30% weight for total loan amount
    -- Round all monetary values to 2 decimal places
    -- Divide final score by 1000 for readability
--
--Expected columns:
--- first_name
--- last_name
--- total_transactions
--- total_deposits
--- total_withdrawals
--- avg_balance
--- loan_count
--- total_loan_amount
--- customer_value_score (rounded to 2 decimal places)
--
--Note: All NULL values should be converted to 0 in calculations using COALESCE
--Sort by customer_value_score in descending order
WITH transaction_metrics AS (
    -- First CTE: Calculate transaction patterns
    SELECT
        c.customer_id,
        COUNT(t.transaction_id) as total_transactions,
        SUM(CASE WHEN t.transaction_type = 'deposit' THEN t.amount ELSE 0 END) as total_deposits,
        SUM(CASE WHEN t.transaction_type = 'withdrawal' THEN t.amount ELSE 0 END) as total_withdrawals,
        AVG(a.balance) as avg_balance
    FROM customers c
    JOIN accounts a ON c.customer_id = a.customer_id
    LEFT JOIN transactions t ON a.account_id = t.account_id
    GROUP BY c.customer_id
),
loan_metrics AS (
    -- Second CTE: Analyze loan behavior
    SELECT
        c.customer_id,
        COUNT(l.loan_id) as loan_count,
        SUM(l.amount) as total_loan_amount,
        AVG(l.interest_rate) as avg_interest_rate
    FROM customers c
    LEFT JOIN loans l ON c.customer_id = l.customer_id
    GROUP BY c.customer_id
)
SELECT
    c.first_name,
    c.last_name,
    tm.total_transactions,
    tm.total_deposits,
    tm.total_withdrawals,
    tm.avg_balance,
    lm.loan_count,
    lm.total_loan_amount,
    -- Calculate customer value score
    ROUND(
        (COALESCE(tm.avg_balance, 0) * 0.4 +
         COALESCE(tm.total_deposits, 0) * 0.3 +
         COALESCE(lm.total_loan_amount, 0) * 0.3) / 1000, 2
    ) as customer_value_score
FROM customers c
JOIN transaction_metrics tm ON c.customer_id = tm.customer_id
JOIN loan_metrics lm ON c.customer_id = lm.customer_id
ORDER BY customer_value_score DESC;