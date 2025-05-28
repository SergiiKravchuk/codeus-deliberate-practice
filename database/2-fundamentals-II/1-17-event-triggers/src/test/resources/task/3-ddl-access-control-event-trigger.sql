-- ====================================================================================================================
-- TASK 3: Simple DDL Access Control
-- ====================================================================================================================
-- TODO:
-- 1. Create a function named `control_ddl_access` that will:
--    - Block operations for users with 'guest' in their name
--    - Block operations during working hours (10 AM to 5 PM) for non-admin users
--    - Allow operations for users with 'admin' or 'dba' in their name
--    - Log blocked attempts to ddl_audit_log table

CREATE OR REPLACE FUNCTION control_ddl_access()
    RETURNS event_trigger
    LANGUAGE plpgsql
AS $$

DECLARE
    current_hour      INTEGER;
    current_user_name TEXT;
    is_admin          BOOLEAN := FALSE;
BEGIN
    -- Get current user and time
    current_user_name := session_user;
    current_hour := EXTRACT(hour FROM CURRENT_TIME);

    -- Check if user is admin
    IF current_user_name ILIKE '%admin%' OR current_user_name ILIKE '%dba%' THEN
        is_admin := TRUE;
    END IF;

    -- Block guest users completely
    IF current_user_name ILIKE '%guest%' THEN
        INSERT INTO ddl_audit_log (command_tag, object_name, user_name)
        VALUES ('BLOCKED - ' || tg_tag, 'ACCESS_DENIED', current_user_name);

        RAISE EXCEPTION 'User % is not allowed to perform DDL operations', current_user_name;
    END IF;

    -- Block non-admin users during working hours (10 AM to 5 PM)
    IF current_hour >= 10 AND current_hour < 17 AND NOT is_admin THEN
        INSERT INTO ddl_audit_log (command_tag, object_name, user_name)
        VALUES ('BLOCKED - ' || tg_tag, 'TIME_RESTRICTED', current_user_name);

        RAISE EXCEPTION 'DDL operations not allowed during working hours for user %. Current time: %:00',
            current_user_name, current_hour;
    END IF;
END;
$$;

-- 2. Create an event trigger named `ddl_access_control` that will:
--    - Execute on ddl_command_start events (to block operations before they execute)
--    - Call the `control_ddl_access` function

CREATE EVENT TRIGGER ddl_access_control
    ON ddl_command_start
EXECUTE FUNCTION control_ddl_access();
