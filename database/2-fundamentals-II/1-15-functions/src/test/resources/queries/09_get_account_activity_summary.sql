-- ====================================================================================================================
-- OPTIONAL TASK 9: Get Account Activity Summary
-- File: 09_get_account_activity_summary.sql
-- ====================================================================================================================
-- Objective: Create a function that uses a loop to iterate over recent transactions
--            for an account and builds a text summary.
--
-- Function Signature to implement:
--   Name: get_account_activity_summary
--   Input Parameters: p_account_id INT, p_transaction_limit INT
--   Return Type: TEXT
--
-- Logic:
-- 1. Declare a BOOLEAN variable `v_account_exists`. Check if `p_account_id` exists.
--    If not, return 'Account not found.'.
-- 2. Declare a TEXT variable `v_summary` initialized to an empty string.
-- 3. Declare a RECORD variable `v_transaction_row` to hold data for each transaction.
-- 4. Declare an INTEGER variable `v_tx_count` initialized to 0, to count processed transactions.
-- 5. Use a `FOR ... LOOP` to iterate over a query that selects `transaction_id` (aliased from t.id),
--    `transaction_type`, `amount`, and `transaction_date` from `transactions` (aliased as t)
--    for `p_account_id`, ordered by `transaction_date` DESC, and limited by `p_transaction_limit`.
-- 6. Inside the loop:
--    a. Append a formatted string for each transaction to `v_summary`.
--       Example format: 'TxID: [id], Type: [type], Amount: [amount], Date: [YYYY-MM-DD HH24:MI]; '
--       Use `to_char()` for date formatting.
--       Use `::TEXT` for casting numeric types for concatenation.
--       Use `COALESCE` to handle potential NULL values in transaction data, displaying 'N/A' if NULL.
--    b. Increment `v_tx_count`.
-- 7. After the loop, if `v_tx_count` is 0 (meaning no transactions were found and processed),
--    set `v_summary` to 'No transactions found for this account.'.
-- 8. Return `v_summary`.
--
-- Example Usage (after creation, for understanding):
--   SELECT get_account_activity_summary(1, 3);
-- ====================================================================================================================

-- TODO: Implement the complete function definition for 'get_account_activity_summary' below.

CREATE OR REPLACE FUNCTION get_account_activity_summary(p_account_id INT, p_transaction_limit INT)
RETURNS TEXT AS $$
DECLARE
    v_account_exists BOOLEAN;
    v_summary TEXT := '';
    v_transaction_row RECORD;
    v_tx_count INT := 0;
BEGIN
    SELECT EXISTS (SELECT 1 FROM accounts WHERE id = p_account_id)
    INTO v_account_exists;

    IF NOT v_account_exists THEN
        RETURN 'Account not found.';
    END IF;

    FOR v_transaction_row IN
        SELECT
            t.id AS transaction_id,
            t.transaction_type,
            t.amount,
            t.transaction_date
        FROM transactions t
        WHERE t.account_id = p_account_id
        ORDER BY t.transaction_date DESC
        LIMIT p_transaction_limit
    LOOP
        v_summary := v_summary ||
                     'TxID: ' || COALESCE(v_transaction_row.transaction_id::TEXT, 'N/A') ||
                     ', Type: ' || COALESCE(v_transaction_row.transaction_type, 'N/A') ||
                     ', Amount: ' || COALESCE(v_transaction_row.amount::TEXT, 'N/A') ||
                     ', Date: ' || COALESCE(to_char(v_transaction_row.transaction_date, 'YYYY-MM-DD HH24:MI'), 'N/A') ||
                     '; ';
        v_tx_count := v_tx_count + 1;
    END LOOP;

    IF v_tx_count = 0 THEN
        v_summary := 'No transactions found for this account.';
    END IF;

    RETURN v_summary;
END;
$$ LANGUAGE plpgsql;
