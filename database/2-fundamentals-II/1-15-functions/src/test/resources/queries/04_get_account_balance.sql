-- ====================================================================================================================
-- TASK 4: Get Account Balance 
-- File: 04_get_account_balance.sql
-- ====================================================================================================================
-- Objective: Create a function to retrieve the current balance of a specific account.
--            Demonstrates SELECT INTO and the FOUND special variable.
--
-- Function Signature to implement:
--   Name: get_account_balance
--   Input Parameter: p_account_id INT
--   Return Type: NUMERIC
--
-- Logic:
-- 1. Declare a NUMERIC variable `v_balance`.
-- 2. Query the `accounts` table to retrieve the `balance` for the given `p_account_id`
--    and store it in `v_balance` using `SELECT INTO`.
-- 3. Error Handling: If no account is found for `p_account_id` (check `FOUND`),
--    raise an exception with the message 'Account not found: %'.
-- 4. Return `v_balance`.
--
-- Example Usage (after creation, for understanding):
--   SELECT get_account_balance(1);
-- ====================================================================================================================

-- TODO: Implement the complete function definition for 'get_account_balance' below.

CREATE OR REPLACE FUNCTION get_account_balance(p_account_id INT)
RETURNS NUMERIC AS $$
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

    RETURN v_balance;
END;
$$ LANGUAGE plpgsql;
