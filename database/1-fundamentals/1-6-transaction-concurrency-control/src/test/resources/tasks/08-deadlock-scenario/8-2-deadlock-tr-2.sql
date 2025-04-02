-- Transaction 2: Creates a deadlock by locking resources in order 2->1
BEGIN;
----------------------------------
--WORKING AREA

-- First lock account with id 2 using FOR UPDATE

-- Now try to lock account 1, which Transaction 1 has already locked
-- This will create a deadlock

-- Try to update both accounts withdrowing -200 from account 2 and depositing +200 to account 1

--WORKING AREA
----------------------------------
COMMIT;
