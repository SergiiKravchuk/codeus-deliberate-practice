-- ====================================================================================================================
-- TASK 1: Create a DDL audit event trigger that logs all DDL operations
-- ====================================================================================================================
-- TODO:
-- 1. Create a function named `audit_ddl_operations` that will:
--    - Insert a record into the `ddl_audit_log` table for each DDL command
--    - Use pg_event_trigger_ddl_commands() to get information about executed commands
--    - Log only the essential fields: command_tag, object_type, object_name, user_name
--    - Use a FOR loop to iterate through all objects returned by pg_event_trigger_ddl_commands()

CREATE OR REPLACE FUNCTION audit_ddl_operations()
    RETURNS event_trigger
    LANGUAGE plpgsql
AS
$$
DECLARE
    ddl_object RECORD;
BEGIN
    FOR ddl_object IN SELECT * FROM pg_event_trigger_ddl_commands()
        LOOP
            INSERT INTO ddl_audit_log (command_tag,
                                       object_type,
                                       object_name,
                                       user_name)
            VALUES (tg_tag,
                    ddl_object.object_type,
                    ddl_object.object_identity,
                    session_user);
        END LOOP;
END;
$$;

-- 2. Create an event trigger named `ddl_audit_trigger` that will:
--    - Execute on ddl_command_end events (ability to collect full object information)
--    - Call the `audit_ddl_operations` function

CREATE EVENT TRIGGER ddl_audit_trigger
    ON ddl_command_end
EXECUTE FUNCTION audit_ddl_operations();