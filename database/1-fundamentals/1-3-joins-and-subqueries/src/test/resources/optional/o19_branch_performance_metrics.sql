--Task: Advanced Branch Performance Scoring
--
--Write a query to evaluate branch performance using multiple CTEs and efficiency metrics.
--Requirements:
--- Use two CTEs: employee_performance and transaction_efficiency
--- In employee_performance CTE:
    -- Count unique employees per branch
    -- Calculate average salary
    -- Count and sum processed loans
--- In transaction_efficiency CTE:
    -- Count unique accounts
    -- Count total transactions
    -- Calculate total deposits
    -- Compute transactions per account ratio
--- Main query should:
    -- Display branch name
    -- Show employee metrics
    -- Show transaction metrics
    -- Calculate efficiency ratios:
--       * Transactions per account (round to 2 decimals)
--       * Deposits per employee (round to 2 decimals)
    -- Calculate performance score using weighted formula:
--       * 30% weight for total deposits
--       * 30% weight for total loan amount
--       * 20% weight for transaction count
--       * 20% weight for processed loans
    -- Divide final score by 1000 for readability
--
--Expected columns:
--- branch_name
--- employee_count
--- processed_loans
--- avg_employee_salary (rounded to 2 decimals)
--- account_count
--- transaction_count
--- transactions_per_account (rounded to 2 decimals)
--- deposits_per_employee (rounded to 2 decimals)
--- performance_score (rounded to 2 decimals)
--
--Notes:
--- Use NULLIF when dividing to prevent division by zero
--- Use COALESCE to convert NULL to 0 in calculations
--- Handle edge case where branch has no employees
--- Round all monetary values to 2 decimal places
--Sort by performance_score in descending order
WITH employee_performance AS (
    -- First CTE: Calculate employee metrics per branch
    SELECT
        b.branch_id,
        COUNT(DISTINCT e.employee_id) as employee_count,
        AVG(e.salary) as avg_salary,
        COUNT(DISTINCT l.loan_id) as processed_loans,
        SUM(l.amount) as total_loan_amount
    FROM branches b
    LEFT JOIN employees e ON b.branch_id = e.branch_id
    LEFT JOIN loans l ON b.branch_id = l.branch_id
    GROUP BY b.branch_id
),
transaction_efficiency AS (
    -- Second CTE: Analyze transaction efficiency
    SELECT
        b.branch_id,
        COUNT(DISTINCT a.account_id) as account_count,
        COUNT(t.transaction_id) as transaction_count,
        SUM(a.balance) as total_deposits,
        COUNT(t.transaction_id) / NULLIF(COUNT(DISTINCT a.account_id), 0) as transactions_per_account
    FROM branches b
    LEFT JOIN accounts a ON b.branch_id = a.branch_id
    LEFT JOIN transactions t ON a.account_id = t.account_id
    GROUP BY b.branch_id
)
SELECT
    b.branch_name,
    ep.employee_count,
    ep.processed_loans,
    ROUND(ep.avg_salary, 2) as avg_employee_salary,
    te.account_count,
    te.transaction_count,
    ROUND(te.transactions_per_account, 2) as transactions_per_account,
    ROUND(te.total_deposits / NULLIF(ep.employee_count, 0), 2) as deposits_per_employee,
    -- Calculate branch performance score
    ROUND(
        (COALESCE(te.total_deposits, 0) * 0.3 +
         COALESCE(ep.total_loan_amount, 0) * 0.3 +
         COALESCE(te.transaction_count, 0) * 0.2 +
         COALESCE(ep.processed_loans, 0) * 0.2) / 1000, 2
    ) as performance_score
FROM branches b
LEFT JOIN employee_performance ep ON b.branch_id = ep.branch_id
LEFT JOIN transaction_efficiency te ON b.branch_id = te.branch_id
ORDER BY performance_score DESC;