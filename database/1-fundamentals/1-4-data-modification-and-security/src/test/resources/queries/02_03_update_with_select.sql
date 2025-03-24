-- check the schema for employees table in 'resources/schema.sql'
-- update the salary for everyone who has less than $50_000 to the highest salary among all employees
UPDATE employees
SET salary = (SELECT MAX(salary) FROM employees)
WHERE salary < 50000;