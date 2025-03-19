-- 43. Find branches where at least one employee has a salary greater than $50,000.
SELECT branch_id
FROM employees
GROUP BY branch_id
HAVING MAX(salary) > 50000;