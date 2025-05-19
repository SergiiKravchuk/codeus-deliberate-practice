------------------------------------------------------------------------------------------------------------------------
-- TASK 2: Reverse a Transaction
------------------------------------------------------------------------------------------------------------------------
-- DESCRIPTION:
-- This procedure reverses a transaction given its ID. It:
-- - Validates the transaction exists and is not already reversed.
-- - Inserts an inverse transaction with opposite amount.
-- - Updates the account balance accordingly.
-- - Raises exceptions for missing or invalid transactions.
-- - Check if transaction already reversed
--
-- PROCEDURE NAME: reverse_transaction
-- INPUT PARAMETERS:
--   - p_transaction_id (INT): The ID of the transaction to reverse.
-- OUTPUT PARAMETERS:
--   - none (uses RAISE NOTICE to confirm reversal)
-- TIPS:
--   - Use CREATE OR REPLACE.
--   - Ensure original transaction exists.
--   - Use negative amount to revert balance.
------------------------------------------------------------------------------------------------------------------------

CREATE OR REPLACE PROCEDURE reverse_transaction(
    IN p_transaction_id INT
)
 LANGUAGE plpgsql
 AS $$
 DECLARE
    v_transaction RECORD;
    v_inverse_amount DECIMAL(15,2);
 BEGIN
    -- Retrieve original transaction
    SELECT * INTO v_transaction
    FROM transactions
    WHERE id = p_transaction_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Transaction % not found.', p_transaction_id;
    END IF;

    -- Check if transaction already reversed
    IF v_transaction.transaction_type = 'reversal' THEN
        RAISE EXCEPTION 'Transaction % is already a reversal.', p_transaction_id;
    END IF;

    -- Calculate inverse amount
    v_inverse_amount := -v_transaction.amount;

    -- Update account balance
    UPDATE accounts
    SET balance = balance + v_inverse_amount
    WHERE id = v_transaction.account_id;

    -- Insert inverse transaction record
    INSERT INTO transactions(account_id, amount, transaction_type, target_account_id)
    VALUES (
        v_transaction.account_id,
        v_inverse_amount,
        'reversal',
        v_transaction.target_account_id
    );

    RAISE NOTICE 'Transaction % reversed successfully.', p_transaction_id;
END;
$$;
