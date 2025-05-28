-- ====================================================================================================================
-- TASK 3: Simple DDL Access Control
-- ====================================================================================================================
-- TODO:
-- 1. Create a function named `control_ddl_access` that will:
--    - Block operations for users with 'guest' in their name
--    - Block operations during working hours (10 AM to 5 PM) for non-admin users
--    - Allow operations for users with 'admin' or 'dba' in their name
--    - Log blocked attempts to ddl_audit_log table


-- 2. Create an event trigger named `ddl_access_control` that will:
--    - Execute on ddl_command_start events (to block operations before they execute)
--    - Call the `control_ddl_access` function
