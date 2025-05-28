-- ====================================================================================================================
-- TASK 2: Create an event trigger that prevents DROP operations for security
-- ====================================================================================================================
-- TODO:
-- 1. Create a function named `prevent_dangerous_drops` that will:
--    - Check if the command tag starts with 'DROP' (use: tg_tag LIKE 'DROP %')
--    - Raise an exception to prevent the operation from completing
--    - Include appropriate error messages for different types of DROP operations
--    - Use RAISE EXCEPTION 'message' to stop the operation


-- 2. Create an event trigger named `security_drop_prevention` that will:
--    - Execute on ddl_command_start events (IMPORTANT: use START to prevent the operation before it begins)
--    - Call the `prevent_dangerous_drops` function

