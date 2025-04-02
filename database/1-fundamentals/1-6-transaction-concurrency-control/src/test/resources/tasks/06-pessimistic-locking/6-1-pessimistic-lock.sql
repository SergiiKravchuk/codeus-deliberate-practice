-- Pessimistic Locking Example
-- This script demonstrates the use of pessimistic locking in a transaction
-- FOR UPDATE is used to lock the selected rows for update from second(6-2...) transaction
-- First transaction: Transfer $500 from account 1 to account 2 using pessimistic locking
BEGIN;
----------------------------------
--WORKING AREA

    -- Lock the source account with FOR UPDATE to prevent concurrent modifications
    -- for update is used to lock the all selected(*) rows from account with id 1

    -- Record initial balance
    CREATE TEMP TABLE account1_initial AS
    -- Select the balance of account 1

    --WAIT_HERE

    -- Update source account (withdraw)
    --update statement is used to withdraw -500 from balance of account with id 1

    -- Update target account (deposit)
    --update statement is used to deposit +500 to balance of account with id 2



--WORKING AREA
----------------------------------
INSERT INTO lock_test_results
(transaction_id, lock_type, account_id, initial_balance, final_balance, success, execution_time_ms)
VALUES (1,
        'PESSIMISTIC',
        1,
        (SELECT balance FROM account1_initial),
        (SELECT balance FROM accounts WHERE account_id = 1),
        true,
        extract(milliseconds from clock_timestamp() - transaction_timestamp()));
COMMIT;