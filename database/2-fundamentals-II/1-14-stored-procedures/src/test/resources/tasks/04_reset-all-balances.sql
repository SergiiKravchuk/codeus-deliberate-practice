------------------------------------------------------------------------------------------------------------------------
-- TASK 4: Reset All Balances for Testing
------------------------------------------------------------------------------------------------------------------------
-- DESCRIPTION:
-- For testing, QA, or maintenance purposes, this procedure resets all account balances to a specified default value.
-- It updates every account in the system and returns a notice with the number of accounts affected.
--
-- PROCEDURE NAME: reset_balances
-- INPUT PARAMETERS:
--   - p_default_balance (DECIMAL): The new balance to set for all accounts.
-- OUTPUT PARAMETERS:
--   - none (uses RAISE NOTICE to report how many rows were updated)
-- TIPS:
--   - Use UPDATE with no WHERE clause (but be careful in production).
--   - Use GET DIAGNOSTICS to count affected rows.
--   - Consider placing a sanity check (e.g. don’t allow negative default balance).
------------------------------------------------------------------------------------------------------------------------

CREATE OR REPLACE PROCEDURE reset_balances(
    IN p_default_balance DECIMAL(15,2)
)
 LANGUAGE plpgsql
 AS $$
 DECLARE
    v_updated_count INT;
BEGIN
    IF p_default_balance < 0 THEN
        RAISE EXCEPTION 'Default balance cannot be negative: %', p_default_balance;
    END IF;

    UPDATE accounts
    SET balance = p_default_balance;

    GET DIAGNOSTICS v_updated_count = ROW_COUNT;

    RAISE NOTICE 'Reset % account balances to %.', v_updated_count, p_default_balance;
END;
$$;
