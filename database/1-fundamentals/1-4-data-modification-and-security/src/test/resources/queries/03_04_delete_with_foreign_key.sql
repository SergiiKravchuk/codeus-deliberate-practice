-- check the schema for employees table in 'resources/schema.sql'
-- execute delete query for the employee with id = 1 and notice the foreign key violation
-- check the schema for orders table in 'resources/schema.sql'
-- delete the dependent record first and then delete the employee with id = 1
DELETE FROM orders WHERE employee_id = 1;
DELETE FROM employees WHERE id = 1;