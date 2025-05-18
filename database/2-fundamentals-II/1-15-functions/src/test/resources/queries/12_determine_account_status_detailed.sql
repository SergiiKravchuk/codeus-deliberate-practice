-- ====================================================================================================================
-- OPTIONAL TASK 12: Determine Account Status Detailed
-- File: 12_determine_account_status_detailed.sql
-- ====================================================================================================================
-- Objective: Implementing more complex conditional logic based on available account fields.
--
-- Function Signature to implement:
--   Name: determine_account_status_detailed
--   Input Parameter: p_account_id INT
--   Return Type: TEXT
--
-- Logic:
-- 1. Declare a NUMERIC variable `v_balance`.
-- 2. Declare a TEXT variable `v_detailed_status` to build the result.
-- 3. Retrieve `balance` for `p_account_id` from the `accounts` table into `v_balance`.
--    (Schema Adaptation: The `accounts` table in `schema.sql` does not have a 'status' column.
--     The logic will be based solely on `v_balance`.)
-- 4. Error Handling: If account not found (check `FOUND`), raise an exception 'Account not found: %'.
-- 5. Determine `v_detailed_status` based purely on `v_balance`:
--    - If `v_balance IS NULL`, set `v_detailed_status` to 'Status Unknown (Balance is NULL)'.
--    - Else if `v_balance < 0.00`, set `v_detailed_status` to 'Overdrawn'.
--    - Else if `v_balance = 0.00`, set `v_detailed_status` to 'Empty'.
--    - Else (`v_balance > 0.00`), set `v_detailed_status` to 'Active with Positive Balance'.
-- 6. Return `v_detailed_status`.
--
-- Example Usage (after creation, for understanding):
--   SELECT determine_account_status_detailed(1);
-- ====================================================================================================================

-- TODO: Implement the complete function definition for 'determine_account_status_detailed' below.

CREATE OR REPLACE FUNCTION determine_account_status_detailed(p_account_id INT)
RETURNS TEXT AS $$
DECLARE
    v_balance NUMERIC;
    v_detailed_status TEXT;
BEGIN
    SELECT balance
    INTO v_balance
    FROM accounts
    WHERE id = p_account_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Account not found: %', p_account_id;
    END IF;

    IF v_balance IS NULL THEN
         v_detailed_status := 'Status Unknown (Balance is NULL)';
    ELSIF v_balance < 0.00 THEN
        v_detailed_status := 'Overdrawn';
    ELSIF v_balance = 0.00 THEN
        v_detailed_status := 'Empty';
    ELSE
        v_detailed_status := 'Active with Positive Balance';
    END IF;

    RETURN v_detailed_status;
END;
$$ LANGUAGE plpgsql;
