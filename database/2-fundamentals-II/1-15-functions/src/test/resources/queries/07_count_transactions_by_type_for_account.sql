-- ====================================================================================================================
-- TASK 7: Count Transactions by Type for Account
-- File: 07_count_transactions_by_type_for_account.sql
-- ====================================================================================================================
-- Objective: Using COUNT(*) aggregate with multiple WHERE conditions.
--
-- Function Signature to implement:
--   Name: count_transactions_by_type_for_account
--   Input Parameters: p_account_id INT, p_transaction_type TEXT
--   Return Type: INT
--
-- Logic:
-- 1. Declare an INT variable `v_transaction_count`.
-- 2. Declare a BOOLEAN variable `v_account_exists`.
-- 3. Check if the account `p_account_id` exists. Store result in `v_account_exists`.
-- 4. If `v_account_exists` is false, return 0.
-- 5. Query the `transactions` table to `COUNT(*)` into `v_transaction_count` where `account_id` matches `p_account_id`
--    AND `transaction_type` matches `p_transaction_type`.
-- 6. Return `v_transaction_count`.
--
-- Example Usage (after creation, for understanding):
--   SELECT count_transactions_by_type_for_account(1, 'Deposit');
-- ====================================================================================================================

-- TODO: Implement the complete function definition for 'count_transactions_by_type_for_account' below.

CREATE OR REPLACE FUNCTION count_transactions_by_type_for_account(p_account_id INT, p_transaction_type TEXT)
RETURNS INT AS $$
DECLARE
    v_transaction_count INT;
    v_account_exists BOOLEAN;
BEGIN
    SELECT EXISTS (SELECT 1 FROM accounts WHERE id = p_account_id)
    INTO v_account_exists;

    IF NOT v_account_exists THEN
        RETURN 0;
    END IF;

    SELECT COUNT(*)
    INTO v_transaction_count
    FROM transactions
    WHERE account_id = p_account_id
      AND transaction_type = p_transaction_type;

    RETURN v_transaction_count;
END;
$$ LANGUAGE plpgsql;
