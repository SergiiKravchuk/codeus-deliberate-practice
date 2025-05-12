-- ====================================================================================================================
-- TASK 4: Create triggers to manage money transfers between accounts
-- ====================================================================================================================
-- TODO:
-- 1. Create a function named `verify_sufficient_balance_for_transfer` that will:
--    - Check if the `transaction_type` is 'transfer'
--    - If it is a transfer, verify that the source account has sufficient balance (balance - amount >= 0)
--    - If there's insufficient balance, raise an exception with appropriate message
--    - If balance is sufficient or not a transfer, allow the operation to proceed
CREATE OR REPLACE FUNCTION verify_sufficient_balance_for_transfer()
    RETURNS TRIGGER AS $$
DECLARE
    current_balance DECIMAL(15, 2);
BEGIN
    IF NEW.transaction_type = 'transfer' THEN
        SELECT balance INTO current_balance
        FROM accounts
        WHERE id = NEW.account_id;

        IF current_balance - NEW.amount < 0 THEN
            RAISE EXCEPTION 'Insufficient funds for transfer. Current balance: %, Transfer amount: %',
                current_balance, NEW.amount;
        END IF;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 2. Create a trigger named `trg_before_transfer_check_balance` that will:
--    - Execute BEFORE INSERT on the `transactions` table
--    - Fire FOR EACH ROW
--    - Call the `verify_sufficient_balance_for_transfer` function
CREATE TRIGGER trg_before_transfer_check_balance
    BEFORE INSERT ON transactions
    FOR EACH ROW
EXECUTE FUNCTION verify_sufficient_balance_for_transfer();


-- 3. Create a function named `process_account_changes_after_transfer` that will:
--    - Check if the `transaction_type` is 'transfer'
--    - If it is a transfer:
--      1. Subtract the transfer amount from the source account's balance
--      2. Add the transfer amount to the target account's balance
--    - Return NEW
CREATE OR REPLACE FUNCTION process_account_changes_after_transfer()
    RETURNS TRIGGER AS $$
BEGIN
    IF NEW.transaction_type = 'transfer' THEN
        UPDATE accounts
        SET balance = balance - NEW.amount
        WHERE id = NEW.account_id;

        UPDATE accounts
        SET balance = balance + NEW.amount
        WHERE id = NEW.target_account_id;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


-- 4. Create a trigger named `trg_after_transfer_update_balances` that will:
--    - Execute AFTER INSERT on the `transactions` table
--    - Fire FOR EACH ROW
--    - Call the `process_account_changes_after_transfer` function
CREATE TRIGGER trg_after_transfer_update_balances
    AFTER INSERT ON transactions
    FOR EACH ROW
EXECUTE FUNCTION process_account_changes_after_transfer();