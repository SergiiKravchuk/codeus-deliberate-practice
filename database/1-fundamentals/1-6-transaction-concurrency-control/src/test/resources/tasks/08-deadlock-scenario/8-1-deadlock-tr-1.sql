-- Transaction 1: Creates a deadlock by locking resources in order 1->2
BEGIN;
----------------------------------
--WORKING AREA

    -- First, lock account with id 1 using FOR UPDATE

    --WAIT_HERE

    -- Now try to lock account 2, which Transaction 2 has already locked
    -- This will cause a deadlock

    -- Try to update both accounts withdrowing -500 from account 1 and depositing +500 to account 2

--WORKING AREA
----------------------------------
COMMIT;
