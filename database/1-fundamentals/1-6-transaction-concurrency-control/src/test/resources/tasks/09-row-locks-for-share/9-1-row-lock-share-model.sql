-- Transaction 1: Demonstrates SELECT FOR SHARE lock mode\
-- This transaction will acquire a SHARE lock on the row with customer_id = 1
-- and then attempt to update the balance.
-- The update will block if another transaction has a FOR UPDATE lock on the same row.
-- The transaction will be committed at the end.
-- The purpose of this transaction is to demonstrate the behavior of the SHARE lock mode
BEGIN;
----------------------------------
--WORKING AREA

    -- Acquire a SHARE lock on a specific row, for account where customer id = 1

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

    -- Read the row data again for customer with id 1(should still be accessible)

    -- Try to update the quantity, withdraw - 100 for customer with id 1
    -- (this would block if another transaction has FOR UPDATE lock)


--WORKING AREA
----------------------------------
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