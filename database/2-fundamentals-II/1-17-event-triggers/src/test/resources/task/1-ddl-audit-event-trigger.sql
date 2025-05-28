-- ====================================================================================================================
-- TASK 1: Create a DDL audit event trigger that logs all DDL operations
-- ====================================================================================================================
-- TODO:
-- 1. Create a function named `audit_ddl_operations` that will:
--    - Insert a record into the `ddl_audit_log` table for each DDL command
--    - Use pg_event_trigger_ddl_commands() to get information about executed commands
--    - Log only the essential fields: command_tag, object_type, object_name, user_name
--    - Use a FOR loop to iterate through all objects returned by pg_event_trigger_ddl_commands()


-- 2. Create an event trigger named `ddl_audit_trigger` that will:
--    - Execute on ddl_command_end events (ability to collect full object information)
--    - Call the `audit_ddl_operations` function
