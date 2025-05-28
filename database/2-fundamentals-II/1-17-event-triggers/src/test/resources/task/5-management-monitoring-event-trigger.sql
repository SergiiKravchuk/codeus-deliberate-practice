-- ====================================================================================================================
-- TASK 5: Simple Trigger Management
-- ====================================================================================================================
-- TODO:
-- 1. Create a function named `log_trigger_activity` that will:
--    - Simply record that event triggers are being executed
--    - Track basic information about DDL commands
--    - Log to trigger_execution_stats table

CREATE OR REPLACE FUNCTION log_trigger_activity()
    RETURNS event_trigger
    LANGUAGE plpgsql
AS
$$
BEGIN
    INSERT INTO trigger_execution_stats (trigger_name,
                                         event_type,
                                         command_tag,
                                         user_name)
    VALUES ('activity_logger',
            tg_event,
            tg_tag,
            session_user);
END;
$$;

-- 2. Create an event trigger to track activity
CREATE EVENT TRIGGER activity_logger
    ON ddl_command_end
EXECUTE FUNCTION log_trigger_activity();

-- 3. Create a function named `disable_event_trigger` that will:
--    - Take a trigger name as parameter
--    - Disable that event trigger
--    - Return success or error message

CREATE OR REPLACE FUNCTION disable_event_trigger(trigger_name TEXT)
    RETURNS TEXT
    LANGUAGE plpgsql
AS
$$
BEGIN
    EXECUTE 'ALTER EVENT TRIGGER ' || quote_ident(trigger_name) || ' DISABLE';
    RETURN 'Trigger ' || trigger_name || ' disabled successfully';
EXCEPTION
    WHEN OTHERS THEN
        RETURN 'Error disabling trigger: ' || SQLERRM;
END;
$$;

-- 4. Create a function named `enable_event_trigger` that will:
--    - Take a trigger name as parameter
--    - Enable that event trigger
--    - Return success or error message

CREATE OR REPLACE FUNCTION enable_event_trigger(trigger_name TEXT)
    RETURNS TEXT
    LANGUAGE plpgsql
AS
$$
BEGIN
    EXECUTE 'ALTER EVENT TRIGGER ' || quote_ident(trigger_name) || ' ENABLE';
    RETURN 'Trigger ' || trigger_name || ' enabled successfully';
EXCEPTION
    WHEN OTHERS THEN
        RETURN 'Error enabling trigger: ' || SQLERRM;
END;
$$;
