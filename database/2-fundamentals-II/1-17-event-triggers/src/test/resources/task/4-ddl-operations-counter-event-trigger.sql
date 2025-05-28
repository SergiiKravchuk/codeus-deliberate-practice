-- ====================================================================================================================
-- TASK 4: DDL Operations Counter
-- ====================================================================================================================
-- This task focuses on counting and tracking DDL operations by users

-- TODO:
-- 1. Create a function named `count_ddl_operations` that will:
--    - Track how many DDL operations each user performs
--    - Count different types of operations (CREATE TABLE, ALTER INDEX, etc.)
--    - Update counters in the ddl_operations_stats table
--    - Use simple INSERT or UPDATE logic

CREATE OR REPLACE FUNCTION count_ddl_operations()
    RETURNS event_trigger
    LANGUAGE plpgsql
AS
$$
DECLARE
    ddl_object        RECORD;
    current_user_name TEXT;
    existing_count    INTEGER;
BEGIN
    current_user_name := session_user;

    IF tg_event = 'ddl_command_end' THEN
        FOR ddl_object IN SELECT * FROM pg_event_trigger_ddl_commands()
            LOOP
                -- Check if we already have a record for this user and command
                SELECT operation_count
                INTO existing_count
                FROM ddl_operations_stats
                WHERE user_name = current_user_name
                  AND command_tag = tg_tag;

                IF existing_count IS NULL THEN
                    -- First time this user performs this operation
                    INSERT INTO ddl_operations_stats (user_name,
                                                      command_tag,
                                                      operation_count,
                                                      last_executed)
                    VALUES (current_user_name,
                            tg_tag,
                            1,
                            CURRENT_TIMESTAMP);
                ELSE
                    -- Update existing counter
                    UPDATE ddl_operations_stats
                    SET operation_count = operation_count + 1,
                        last_executed   = CURRENT_TIMESTAMP
                    WHERE user_name = current_user_name
                      AND command_tag = tg_tag;
                END IF;
            END LOOP;
    END IF;
END;
$$;

-- 2. Create an event trigger named `ddl_counter_trigger` that will:
--    - Execute on ddl_command_end events (after successful DDL execution)
--    - Call the `count_ddl_operations` function
--    - Track statistics for reporting

CREATE EVENT TRIGGER ddl_counter_trigger
    ON ddl_command_end
EXECUTE FUNCTION count_ddl_operations();
