-- 23. Find the total salary expenditure per branch.
SELECT branch_id, SUM(salary) AS total_salary
FROM employees
GROUP BY branch_id;