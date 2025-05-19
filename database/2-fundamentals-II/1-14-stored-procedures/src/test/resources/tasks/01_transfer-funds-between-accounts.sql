-- TASK 1: Transfer Funds Between Accounts
------------------------------------------------------------------------------------------------------------------------
-- DESCRIPTION:
-- In our banking application, users should be able to transfer funds between accounts.
-- This operation must be secure and consistent. It should check that the sender has sufficient balance,
-- deduct the amount from the sender’s account, credit the receiver’s account,
-- and log both transactions in the transactions table.
-- The system should raise an exception if either account is missing or the sender has insufficient funds.
--
-- PROCEDURE NAME: transfer_funds
-- INPUT PARAMETERS:
--   - p_sender_id (INT): The ID of the account sending funds.
--   - p_receiver_id (INT): The ID of the account receiving funds.
--   - p_amount (DECIMAL): The amount to transfer.
-- OUTPUT PARAMETERS:
--   - none (uses RAISE NOTICE to confirm successful transfer)
-- TIPS:
--   - Use CREATE OR REPLACE
--   - Validate that both accounts exist.
--   - Use FOR UPDATE to lock the sender account row.
--   - Ensure the sender has enough balance.
--   - Use transaction logging with target_account_id set for traceability.
------------------------------------------------------------------------------------------------------------------------

CREATE OR REPLACE PROCEDURE transfer_funds(
    IN p_sender_id INT,
    IN p_receiver_id INT,
    IN p_amount NUMERIC(15,2)
)
LANGUAGE plpgsql
 AS $$
 DECLARE
    v_sender_balance NUMERIC(15,2);
 BEGIN
    -- Lock and validate sender account
    SELECT balance INTO v_sender_balance
    FROM accounts
    WHERE id = p_sender_id
    FOR UPDATE;

    IF v_sender_balance IS NULL THEN
        RAISE EXCEPTION 'Sender account % not found.', p_sender_id;
    ELSIF v_sender_balance < p_amount THEN
        RAISE EXCEPTION 'Insufficient funds in sender account %.', p_sender_id;
    END IF;

    -- Validate receiver account exists
    IF NOT EXISTS (SELECT 1 FROM accounts WHERE id = p_receiver_id) THEN
        RAISE EXCEPTION 'Receiver account % not found.', p_receiver_id;
    END IF;

    -- Deduct from sender
    UPDATE accounts
    SET balance = balance - p_amount
    WHERE id = p_sender_id;

    -- Credit to receiver
    UPDATE accounts
    SET balance = balance + p_amount
    WHERE id = p_receiver_id;

    -- Record both transactions
    INSERT INTO transactions(account_id, amount, transaction_type, target_account_id)
    VALUES
        (p_sender_id, -p_amount, 'transfer', p_receiver_id),
        (p_receiver_id, p_amount, 'deposit', p_sender_id);

    RAISE NOTICE 'Transferred % from account % to account %.', p_amount, p_sender_id, p_receiver_id;
END;
$$;

-- Sample call to verify functionality
-- Ensure these account IDs exist and sender has enough balance before running
-- CALL transfer_funds(1, 2, 100.00);

-- View updated balances
-- SELECT * FROM accounts WHERE id IN (1, 2);
-- SELECT * FROM transactions WHERE account_id IN (1, 2) ORDER BY id DESC LIMIT 5;
