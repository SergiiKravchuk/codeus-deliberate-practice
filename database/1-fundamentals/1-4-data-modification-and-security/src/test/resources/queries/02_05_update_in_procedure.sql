-- check the schema for employees table in 'resources/schema.sql'
-- start a procedure with DO $$
-- declare the patron_salary variable with the appropriate type
-- initialize the variable by select for update request, select Pes Patron salary
-- if Pes Patron has salary less than 55000, update it to the maximum salary
-- end procedure with END $$
DO $$
DECLARE patron_salary DECIMAL;
BEGIN
    SELECT salary INTO patron_salary FROM employees WHERE last_name = 'Patron' FOR UPDATE;
IF patron_salary < 55000 THEN
    UPDATE employees
    SET salary = (SELECT MAX(salary) from employees)
    WHERE last_name = 'Patron';
END IF;
END $$;
