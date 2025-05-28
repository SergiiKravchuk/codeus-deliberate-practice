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


-- 2. Create an event trigger named `ddl_counter_trigger` that will:
--    - Execute on ddl_command_end events (after successful DDL execution)
--    - Call the `count_ddl_operations` function
--    - Track statistics for reporting

