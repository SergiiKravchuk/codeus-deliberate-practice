--Task: Show All Branches and Their Employee Count
--Requirements:
--- List all branches
--- Count employees in each branch
--- Include branches without employees
--- Basic LEFT JOIN example
--
--Expected columns: branch_name, employee_count
SELECT
    b.branch_name,
    COUNT(e.employee_id) as employee_count
FROM branches b
LEFT JOIN employees e ON b.branch_id = e.branch_id
GROUP BY b.branch_name;