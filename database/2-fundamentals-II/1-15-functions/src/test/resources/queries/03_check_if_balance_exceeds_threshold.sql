-- ====================================================================================================================
-- TASK 3: Check if Balance Exceeds Threshold
-- File: 03_check_if_balance_exceeds_threshold.sql
-- ====================================================================================================================
-- Objective: Boolean return type, simple database query with a condition.
--
-- Function Signature to implement:
--   Name: check_if_balance_exceeds_threshold
--   Input Parameters: p_account_id INT, p_threshold NUMERIC
--   Return Type: BOOLEAN
--
-- Logic:
-- 1. Declare a NUMERIC variable `v_balance`.
-- 2. Retrieve the `balance` for `p_account_id` from the `accounts` table into `v_balance`.
-- 3. Error Handling: If account not found (check `FOUND`), raise 'Account not found: %'.
-- 4. Return TRUE if `v_balance > p_threshold`, otherwise FALSE.
--
-- Example Usage (after creation, for understanding):
--   SELECT check_if_balance_exceeds_threshold(1, 500.00);
-- ====================================================================================================================

-- TODO: Implement the complete function definition for 'check_if_balance_exceeds_threshold' below.

CREATE OR REPLACE FUNCTION check_if_balance_exceeds_threshold(p_account_id INT, p_threshold NUMERIC)
RETURNS BOOLEAN AS $$
DECLARE
    v_balance NUMERIC;
BEGIN
    SELECT balance
    INTO v_balance
    FROM accounts
    WHERE id = p_account_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Account not found: %', p_account_id;
    END IF;

    RETURN v_balance > p_threshold;
END;
$$ LANGUAGE plpgsql;
