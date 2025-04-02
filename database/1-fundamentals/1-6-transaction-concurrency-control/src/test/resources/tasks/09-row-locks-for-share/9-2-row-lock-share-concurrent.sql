-- Transaction 2: Demonstrates concurrent SHARE lock acquisition
BEGIN;

-- Acquire a SHARE lock on the same row - this should not block
SELECT * FROM accounts WHERE customer_id = 1 FOR SHARE;

-- Record the lock acquisition
INSERT INTO lock_test_results
(transaction_id, lock_type, resource_id, lock_mode)
VALUES (
           2,
           'ROW_LEVEL',
           1,
           'FOR SHARE'
       );

-- Try to read the data (should succeed)
SELECT * FROM accounts WHERE customer_id = 1;

-- Record the read operation
INSERT INTO lock_test_results
(transaction_id, lock_type, resource_id, operation, success)
VALUES (
           2,
           'ROW_LEVEL',
           1,
           'READ',
           true
       );

COMMIT;