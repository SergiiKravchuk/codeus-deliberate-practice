------------------------------------------------------------------------------------------------------------------------
-- TASK 3: Log Deposits Above Threshold
------------------------------------------------------------------------------------------------------------------------
-- DESCRIPTION:
-- This procedure scans all transactions of type 'deposit' and logs any deposit above a given threshold
-- into a separate table named `large_deposit_audit`. This can be used for compliance or fraud detection.
-- The audit table must store the transaction ID, account ID, amount, and timestamp.
--
-- PROCEDURE NAME: log_large_deposits
-- INPUT PARAMETERS:
--   - p_threshold (DECIMAL): The minimum deposit amount to trigger logging.
-- OUTPUT PARAMETERS:
--   - none (uses RAISE NOTICE for number of rows inserted)
-- TIPS:
--   - Use INSERT INTO ... SELECT
--   - Avoid duplicates in the audit table (assume transaction_id is unique in audit table)
------------------------------------------------------------------------------------------------------------------------

-- Create audit table if not exists (needed for testing/development)
CREATE TABLE IF NOT EXISTS large_deposit_audit (
    transaction_id INT PRIMARY KEY,
    account_id INT NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);