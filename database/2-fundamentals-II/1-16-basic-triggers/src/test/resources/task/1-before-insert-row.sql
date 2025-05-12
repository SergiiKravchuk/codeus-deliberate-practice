-- ====================================================================================================================
-- TASK 1: Create a trigger that will capitalize customers first and last name
-- ====================================================================================================================
-- TODO:
-- 1. Create a function with name `capitalize_customer_names`, that will:
--    - https://www.postgresql.org/docs/current/sql-createfunction.html
--    - Capitalize the first letter for `first_name` and `last_name`.
--    - To capitalize the string. (Hint: use INITCAP method). https://neon.tech/postgresql/postgresql-string-functions/postgresql-letter-case-functions
--    - return NEW
--    - hint: to access data from row use key word `NEW` and the call the column that you need to access
CREATE OR REPLACE FUNCTION capitalize_customer_names()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.first_name = INITCAP(NEW.first_name);
    NEW.last_name = INITCAP(NEW.last_name);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 2. Create trigger with name `trg_capitalize_names_before_customer_insert` that will be called
--    BEFORE INSERT into `customers` and will call the `capitalize_customer_names` function FOR EACH ROW.
CREATE TRIGGER trg_capitalize_names_before_customer_insert
    BEFORE INSERT ON customers
    FOR EACH ROW
EXECUTE FUNCTION capitalize_customer_names();