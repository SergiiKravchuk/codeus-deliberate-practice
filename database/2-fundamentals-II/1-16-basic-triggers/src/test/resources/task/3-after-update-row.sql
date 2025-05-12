-- ====================================================================================================================
-- TASK 3: Create a trigger that logs customer updates (FOR EACH ROW)
-- ====================================================================================================================
-- TODO:
-- 1. Create a function named `log_customer_update`.
--    This function should:
--    - Insert a record into the audit_logs table when a customer is updated.
--    - Include the customer ID and which field was updated.
CREATE OR REPLACE FUNCTION log_customer_update()
    RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO audit_logs (operation_type, table_name, query_text)
    VALUES (TG_OP, TG_TABLE_NAME, current_query());
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- 2. Create a trigger that executes this function AFTER UPDATE on customers.
CREATE TRIGGER trg_after_customer_update
    AFTER UPDATE ON customers
    FOR EACH ROW
EXECUTE FUNCTION log_customer_update();