-- 18. Count the number of employees per branch.
SELECT branch_id, COUNT(*) AS employee_count
FROM employees
GROUP BY branch_id;