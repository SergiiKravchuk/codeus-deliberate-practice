-- 15. Find branches where the average salary of employees is above $50,000.
SELECT branch_id, AVG(salary) AS avg_salary
FROM employees
GROUP BY branch_id
HAVING AVG(salary) > 50000;