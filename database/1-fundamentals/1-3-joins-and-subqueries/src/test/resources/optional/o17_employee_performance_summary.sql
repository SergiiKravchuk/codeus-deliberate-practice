--Task: Create Employee Portfolio Performance Report
--
--Write a query to analyze employee performance metrics using CTEs.
--Requirements:
--- Display employee's first and last name
--- Count unique accounts managed by each employee
--- Calculate total portfolio balance under management
--- Count approved loans processed
--- Consider only accounts and loans in employee's branch
--- Use COALESCE for handling null balances
--- Sort by portfolio balance in descending order
--- Use CTE for metric calculations
--
--Expected columns: first_name, last_name, accounts_managed, loans_processed, portfolio_balance
WITH employee_metrics AS (
    SELECT
        e.employee_id,
        e.branch_id,
        COUNT(DISTINCT a.account_id) as accounts_managed,
        COALESCE(SUM(a.balance), 0) as total_portfolio_balance,
        COUNT(DISTINCT l.loan_id) as loans_processed
    FROM employees e
    LEFT JOIN accounts a ON e.branch_id = a.branch_id
    LEFT JOIN loans l ON e.branch_id = l.branch_id AND l.status = 'approved'
    GROUP BY e.employee_id, e.branch_id
)
SELECT
    e.first_name,
    e.last_name,
    em.accounts_managed,
    em.loans_processed,
    ROUND(em.total_portfolio_balance::numeric, 2) as portfolio_balance
FROM employees e
JOIN employee_metrics em ON e.employee_id = em.employee_id
ORDER BY em.total_portfolio_balance DESC;