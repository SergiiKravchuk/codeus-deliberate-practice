-- 21. Find the branch with the highest number of employees.
SELECT branch_id, COUNT(*) AS employee_count
FROM employees
GROUP BY branch_id
ORDER BY employee_count DESC
LIMIT 1;