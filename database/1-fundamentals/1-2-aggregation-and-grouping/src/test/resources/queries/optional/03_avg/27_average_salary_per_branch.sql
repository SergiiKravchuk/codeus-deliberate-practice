-- 27. Find the average salary per branch.
SELECT branch_id, AVG(salary) AS avg_salary
FROM employees
GROUP BY branch_id;