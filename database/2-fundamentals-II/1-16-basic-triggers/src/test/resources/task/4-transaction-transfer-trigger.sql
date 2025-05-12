-- ====================================================================================================================
-- TASK 4: Create triggers to manage money transfers between accounts
-- ====================================================================================================================
-- TODO:
-- 1. Create a function named `verify_sufficient_balance_for_transfer` that will:
--    - Check if the `transaction_type` is 'transfer'
--    - If it is a transfer, verify that the source account has sufficient balance (balance - amount >= 0)
--    - If there's insufficient balance, raise an exception with appropriate message
--    - If balance is sufficient or not a transfer, allow the operation to proceed
--    - Return NEW


-- 2. Create a trigger named `trg_before_transfer_check_balance` that will:
--    - Execute BEFORE INSERT on the `transactions` table
--    - Fire FOR EACH ROW
--    - Call the `verify_sufficient_balance_for_transfer` function


-- 3. Create a function named `process_account_changes_after_transfer` that will:
--    - Check if the `transaction_type` is 'transfer'
--    - If it is a transfer:
--      1. Subtract the transfer amount from the source account's balance
--      2. Add the transfer amount to the target account's balance
--    - Return NEW


-- 4. Create a trigger named `trg_after_transfer_update_balances` that will:
--    - Execute AFTER INSERT on the `transactions` table
--    - Fire FOR EACH ROW
--    - Call the `process_account_changes_after_transfer` function
