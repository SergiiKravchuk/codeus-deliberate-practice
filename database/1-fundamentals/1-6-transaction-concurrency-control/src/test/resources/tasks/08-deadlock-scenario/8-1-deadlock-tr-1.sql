-- Transaction 1: Creates a deadlock by locking resources in order 1->2
BEGIN;

-- First lock account 1
SELECT * FROM accounts WHERE account_id = 1 FOR UPDATE;

--WAIT_HERE

-- Now try to lock account 2, which Transaction 2 has already locked
-- This will cause a deadlock
SELECT * FROM accounts WHERE account_id = 2 FOR UPDATE;

-- Try to update both accounts
UPDATE accounts SET balance = balance - 500 WHERE account_id = 1;
UPDATE accounts SET balance = balance + 500 WHERE account_id = 2;

COMMIT;
