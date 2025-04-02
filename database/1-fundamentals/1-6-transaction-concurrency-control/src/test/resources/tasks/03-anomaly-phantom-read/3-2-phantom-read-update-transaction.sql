--This is this second transaction that will be
--executed concurrently in the middle of first transaction execution.
BEGIN;
    -- Set the isolation level to READ COMMITTED

    -- Update the balance of the account with id 4 adding 10000 to current balance
END;
