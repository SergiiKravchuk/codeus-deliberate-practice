-- 45. Find branches where the number of employees is at least 10% of the total number of employees.
SELECT branch_id, COUNT(*) AS employee_count
FROM employees
GROUP BY branch_id
HAVING COUNT(*) >= (SELECT COUNT(*) FROM employees) * 0.1;