-- Transaction 2: Creates a deadlock by locking resources in order 2->1
BEGIN;

-- First lock account 2
SELECT * FROM accounts WHERE account_id = 2 FOR UPDATE;

-- Now try to lock account 1, which Transaction 1 has already locked
-- This will create a deadlock
SELECT * FROM accounts WHERE account_id = 1 FOR UPDATE;

-- Try to update both accounts
UPDATE accounts SET balance = balance - 200 WHERE account_id = 2;
UPDATE accounts SET balance = balance + 200 WHERE account_id = 1;

COMMIT;
