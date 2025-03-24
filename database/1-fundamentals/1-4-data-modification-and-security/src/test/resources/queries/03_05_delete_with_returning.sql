-- check the schema for employees table in 'resources/schema.sql'
-- delete row with Pes Patron and return first_name, last_name and salary
DELETE FROM employees
where last_name = 'Patron'
RETURNING first_name, last_name, salary