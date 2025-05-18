-- ====================================================================================================================
-- TASK 6: Get Customer's Total Balance
-- File: 06_get_customer_total_balance.sql
-- ====================================================================================================================
-- Objective: Create a function to calculate the sum of balances from all accounts
--            belonging to a specific customer. Demonstrates SELECT EXISTS and COALESCE.
--
-- Function Signature to implement:
--   Name: get_customer_total_balance
--   Input Parameter: p_customer_id INT
--   Return Type: NUMERIC
--
-- Logic:
-- 1. Declare a BOOLEAN variable `v_customer_exists`.
-- 2. Declare a NUMERIC variable `v_total_balance`.
-- 3. Check if the customer exists using `SELECT EXISTS (...) INTO v_customer_exists`.
-- 4. If `v_customer_exists` is false, raise an exception: 'Customer not found: %'.
-- 5. Query `accounts` to `SUM(balance)` for `p_customer_id` into `v_total_balance`.
--    Use `COALESCE(SUM(balance), 0.00)` to handle cases where the customer has no accounts.
-- 6. Return `v_total_balance`.
--
-- Example Usage (after creation, for understanding):
--   SELECT get_customer_total_balance(1);
-- ====================================================================================================================

-- TODO: Implement the complete function definition for 'get_customer_total_balance' below.

CREATE OR REPLACE FUNCTION get_customer_total_balance(p_customer_id INT)
RETURNS NUMERIC AS $$
DECLARE
    v_customer_exists BOOLEAN;
    v_total_balance NUMERIC;
BEGIN
    SELECT EXISTS (SELECT 1 FROM customers WHERE id = p_customer_id)
    INTO v_customer_exists;

    IF NOT v_customer_exists THEN
        RAISE EXCEPTION 'Customer not found: %', p_customer_id;
    END IF;

    SELECT COALESCE(SUM(balance), 0.00)
    INTO v_total_balance
    FROM accounts
    WHERE customer_id = p_customer_id;

    RETURN v_total_balance;
END;
$$ LANGUAGE plpgsql;
