-- ====================================================================================================================
-- TASK 3: Create a trigger that logs customer updates (FOR EACH ROW)
-- ====================================================================================================================
-- TODO:
-- 1. Create a function named `log_customer_update`.
--    This function should:
--    - Insert a record into the audit_logs table when a customer is updated.
--    - Hint: the same logic as in previous task but triggers after update


-- 2. Create a trigger that executes this function AFTER UPDATE on customers.
