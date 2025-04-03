-- Transaction 2: Demonstrates concurrent SHARE lock acquisition
BEGIN;
----------------------------------
--WORKING AREA

    -- Acquire a SHARE lock on the same row, for account with customer id = 1, this should not block

    -- Record the lock acquisition
    INSERT INTO lock_test_results
    (transaction_id, lock_type, resource_id, lock_mode)
    VALUES (
               2,
               'ROW_LEVEL',
               1,
               'FOR SHARE'
           );

    -- Try to read the row account data (should succeed) for customer with id 1;
    SELECT * FROM accounts WHERE customer_id = 1;

--WORKING AREA
----------------------------------
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