-- ====================================================================================================================
-- TASK 1: Create a DDL audit event trigger that logs all DDL operations
-- ====================================================================================================================
-- TODO:
-- 1. Create a function named `audit_ddl_operations` that will:
--    - Declare `ddl_object RECORD`
--    - Use `pg_event_trigger_ddl_commands()` into `ddl_object` to get information about executed commands
--    - Use a FOR loop to iterate through all objects returned by `pg_event_trigger_ddl_commands()`. Hint: `FOR ddl_object IN SELECT ...`
--    - Insert a record into the `ddl_audit_log` table for each DDL command
--    - Log only the essential fields:
--          - `command_tag` - use `tg_tag` (https://www.postgresql.org/docs/current/pltcl-event-trigger.html)
--          - `object_type` - `ddl_object.object_type`
--          - `object_name` - `ddl_object.object_identity`
--          - `user_name` - `session_user` (it declared by postgres key word that provides current session user name)


-- 2. Create an event trigger named `ddl_audit_trigger` that will:
--    - Execute on ddl_command_end events (ability to collect full object information)
--    - Call the `audit_ddl_operations` function
