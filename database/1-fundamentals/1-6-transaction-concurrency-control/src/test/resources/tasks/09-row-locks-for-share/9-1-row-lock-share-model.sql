-- Transaction 1: Demonstrates SELECT FOR SHARE lock mode
BEGIN;
-- Acquire a SHARE lock on a specific row
SELECT * FROM accounts WHERE customer_id = 1 FOR SHARE;

-- Record the lock acquisition
INSERT INTO lock_test_results
(transaction_id, lock_type, resource_id, lock_mode)
VALUES (
           1,
           'ROW_LEVEL',
           1,
           'FOR SHARE'
       );

--WAIT_HERE

-- Read the row data again (should still be accessible)
SELECT * FROM accounts WHERE customer_id = 1;

-- Try to update the quantity (this would block if another transaction has FOR UPDATE lock)
UPDATE accounts SET balance = balance - 100 WHERE customer_id = 1;

-- Record the completion
INSERT INTO lock_test_results
(transaction_id, lock_type, resource_id, operation, success)
VALUES (
           1,
           'ROW_LEVEL',
           1,
           'UPDATE',
           true
);
COMMIT;