--Task: Create a Branch Performance Report
--
--Write a query that provides a comprehensive overview of each bank branch's performance.
--Requirements:
--- Display branch name
--- Show total number of employees in each branch
--- Count total number of accounts managed by the branch
--- Calculate average account balance for each branch
--- Sort results by average account balance in descending order
--- Include branches even if they have no employees or accounts (use appropriate join type)
--
--Expected columns: branch_name, employee_count, account_count, avg_account_balance
SELECT
    b.branch_name,
    COUNT(DISTINCT e.employee_id) as employee_count,
    COUNT(DISTINCT a.account_id) as account_count,
    ROUND(AVG(a.balance), 2) as avg_account_balance
FROM branches b
LEFT JOIN employees e ON b.branch_id = e.branch_id
LEFT JOIN accounts a ON b.branch_id = a.branch_id
GROUP BY b.branch_name
ORDER BY avg_account_balance DESC;