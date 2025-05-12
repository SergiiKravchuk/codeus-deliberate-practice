-- ====================================================================================================================
-- TASK 3: Create a trigger that logs customer updates (FOR EACH ROW)
-- ====================================================================================================================
-- TODO:
-- 1. Create a function named `log_customer_update`.
--    This function should:
--    - Insert a record into the audit_logs table when a customer is updated.
--    - Include the customer ID and which field was updated.


-- 2. Create a trigger that executes this function AFTER UPDATE on customers.
