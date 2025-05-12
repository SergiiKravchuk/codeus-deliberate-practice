-- ====================================================================================================================
-- TASK 2: Create a trigger that logs an attempt to insert data into the customers table (FOR EACH STATEMENT)
-- ====================================================================================================================
-- TODO:
-- 1. Create a function named `log_customer_insert_attempt_statement`.
--    This function should:
--    - Be suitable for a statement-level trigger.
--    - Insert a new record into the `audit_logs` table.
--      - `operation_type` should be the type of operation that fired the trigger (Hint: use TG_OP). https://www.postgresql.org/docs/current/plpgsql-trigger.html
--      - `table_name` should be the name of the table on which the trigger fired (Hint: use TG_TABLE_NAME or hardcode `customers`). https://www.postgresql.org/docs/current/plpgsql-trigger.html
--      - `query_text` should be the text of the query that caused the trigger to fire (Hint: use the `current_query()` function). https://www.postgresql.org/docs/current/functions-info.html
--    - Return NULL (the return value for a BEFORE statement trigger is ignored, but the function must return type TRIGGER).


-- 2. Create a trigger named `trg_before_customer_insert_statement_log`.
--    This trigger should:
--    - Execute BEFORE any INSERT operation on the `customers` table.
--    - Fire FOR EACH STATEMENT.
--    - Call the `log_customer_insert_attempt_statement` function.
