-- 38. Find branches where total employee salaries exceed $100,000.
SELECT branch_id, SUM(salary) AS total_salary
FROM employees
GROUP BY branch_id
HAVING SUM(salary) > 100000;