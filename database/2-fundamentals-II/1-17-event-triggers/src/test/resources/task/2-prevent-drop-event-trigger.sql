-- ====================================================================================================================
-- TASK 2: Create an event trigger that prevents DROP operations for security
-- ====================================================================================================================
-- TODO:
-- 1. Create a function named `prevent_dangerous_drops` that will:
--    - Check if the command tag starts with 'DROP' (use: tg_tag LIKE 'DROP %')
--    - Raise an exception to prevent the operation from completing
--    - Include appropriate error messages for different types of DROP operations
--    - Use RAISE EXCEPTION 'message' to stop the operation

CREATE OR REPLACE FUNCTION prevent_dangerous_drops()
    RETURNS event_trigger
    LANGUAGE plpgsql
AS
$$
BEGIN
    -- Check if this is any kind of DROP operation
    IF tg_tag LIKE 'DROP %' THEN

        -- Handle specific DROP operations with custom messages
        IF tg_tag = 'DROP TABLE' THEN
            RAISE EXCEPTION 'DROP TABLE is forbidden! Table deletion not allowed for security reasons.';

        ELSIF tg_tag = 'DROP INDEX' THEN
            RAISE EXCEPTION 'DROP INDEX is forbidden! Index deletion not allowed for security reasons.';

        ELSIF tg_tag = 'DROP VIEW' THEN
            RAISE EXCEPTION 'DROP VIEW is forbidden! View deletion not allowed for security reasons.';

        ELSIF tg_tag = 'DROP FUNCTION' THEN
            RAISE EXCEPTION 'DROP FUNCTION is forbidden! Function deletion not allowed for security reasons.';

        ELSIF tg_tag = 'DROP DATABASE' THEN
            RAISE EXCEPTION 'DROP DATABASE is forbidden! Database deletion strictly prohibited!';

        ELSIF tg_tag = 'DROP SCHEMA' THEN
            RAISE EXCEPTION 'DROP SCHEMA is forbidden! Schema deletion not allowed for security reasons.';

        ELSE
            -- Block any other DROP operations we have not explicitly handled
            RAISE EXCEPTION 'Operation % is forbidden for security reasons!', tg_tag;
        END IF;
    END IF;
END;
$$;

-- 2. Create an event trigger named `security_drop_prevention` that will:
--    - Execute on ddl_command_start events (IMPORTANT: use START to prevent the operation before it begins)
--    - Call the `prevent_dangerous_drops` function

CREATE EVENT TRIGGER security_drop_prevention
    ON ddl_command_start
EXECUTE FUNCTION prevent_dangerous_drops();
