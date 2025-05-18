-- ====================================================================================================================
-- OPTIONAL TASK 13: Get Account Details with OUT Parameters
-- File: 13_get_account_details_with_out_params.sql
-- ====================================================================================================================
-- Objective: Using `OUT` parameters to return multiple values from a function.
--
-- Function Signature to implement:
--   Name: get_account_details_with_out_params
--   Input Parameter: p_account_id INT
--   Output Parameters:
--     o_account_number TEXT,    -- Schema Adaptation: This will be the account's 'id' cast to TEXT.
--     o_account_type TEXT,
--     o_balance NUMERIC,
--     o_is_active BOOLEAN        -- Schema Adaptation: This will be defaulted to TRUE if account is found,
--                               -- as 'accounts.status' column is not in the schema.
--   (Note: No explicit RETURNS clause is used when OUT parameters define the return structure for this style of function)
--
-- Logic:
-- 1. Declare a RECORD variable `v_account_record` to temporarily hold fetched account data.
-- 2. Query the `accounts` table for the given `p_account_id` to retrieve `id`, `account_type`, and `balance`.
--    Store these into `v_account_record`.
-- 3. Error Handling: If no account is found (check `FOUND` after `SELECT INTO`),
--    raise an exception: 'Account not found: %'.
-- 4. Assign values from `v_account_record` to the respective `OUT` parameters:
--    - `o_account_number`: Assign `v_account_record.id::TEXT`. (Schema Adaptation: Using 'id' as 'accounts' table lacks 'account_number').
--    - `o_account_type`: Assign `v_account_record.account_type`.
--    - `o_balance`: Assign `v_account_record.balance`.
--    - `o_is_active`: Assign `TRUE`. (Schema Adaptation: Defaulting to TRUE as 'accounts' table lacks a 'status' column to determine actual active status).
--
-- Example Usage (after creation, for understanding):
--   SELECT * FROM get_account_details_with_out_params(1);
--   -- This will show columns: o_account_number, o_account_type, o_balance, o_is_active
-- ====================================================================================================================

-- TODO: Implement the complete function definition for 'get_account_details_with_out_params' below.
-- This includes the CREATE OR REPLACE FUNCTION statement, IN and OUT parameter definitions,
-- AS $$ DECLARE ... BEGIN ... END; $$ LANGUAGE plpgsql; structure.
-- No explicit RETURN statement is needed when using OUT parameters this way.

CREATE OR REPLACE FUNCTION get_account_details_with_out_params(
    p_account_id INT,
    OUT o_account_number TEXT,
    OUT o_account_type TEXT,
    OUT o_balance NUMERIC,
    OUT o_is_active BOOLEAN
)
AS $$
DECLARE
    v_account_record RECORD;
BEGIN
    SELECT
        a.id,
        a.account_type,
        a.balance
    INTO v_account_record
    FROM accounts a
    WHERE a.id = p_account_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Account not found: %', p_account_id;
    END IF;

    o_account_number := v_account_record.id::TEXT;
    o_account_type := v_account_record.account_type;
    o_balance := v_account_record.balance;
    o_is_active := TRUE;

END;
$$ LANGUAGE plpgsql;
