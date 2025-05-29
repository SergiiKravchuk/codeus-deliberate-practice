-- ====================================================================================================================
-- TASK 2: Create an event trigger that prevents DROP operations for security
-- ====================================================================================================================
-- TODO:
-- 1. Create a function named `prevent_dangerous_drops` that will:
--    - Use `tg_tag` to check if the current command is a DROP operation (hint: use `LIKE 'DROP %'`)
--    - THEN:
--      - Use IF-ELSIF-ELSE structure to handle specific DROP commands:
--        - For `DROP TABLE`: Use `RAISE EXCEPTION` with message 'DROP TABLE is forbidden! Table deletion not allowed for security reasons.'
--        - For `DROP INDEX`: Use `RAISE EXCEPTION` with message 'DROP INDEX is forbidden! Index deletion not allowed for security reasons.'
--        - For `DROP VIEW`: Use `RAISE EXCEPTION` with message 'DROP VIEW is forbidden! View deletion not allowed for security reasons.'
--        - For `DROP FUNCTION`: Use `RAISE EXCEPTION` with message 'DROP FUNCTION is forbidden! Function deletion not allowed for security reasons.'
--        - For `DROP DATABASE`: Use `RAISE EXCEPTION` with message 'DROP DATABASE is forbidden! Database deletion strictly prohibited!'
--        - For `DROP SCHEMA`: Use `RAISE EXCEPTION` with message 'DROP SCHEMA is forbidden! Schema deletion not allowed for security reasons.'
--        - For any other DROP operation: Use `RAISE EXCEPTION` with dynamic message 'Operation % is forbidden for security reasons!' where % is `tg_tag`


-- 2. Create an event trigger named `security_drop_prevention` that will:
--    - Execute on ddl_command_start events (use START to intercept before execution)
--    - Call the `prevent_dangerous_drops` function
