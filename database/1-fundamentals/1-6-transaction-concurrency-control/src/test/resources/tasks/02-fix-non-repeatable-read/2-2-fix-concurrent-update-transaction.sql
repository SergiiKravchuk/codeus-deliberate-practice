--This is this second transaction that will be
--executed concurrently in the middle of first transaction execution.
BEGIN;
    -- Set the isolation level to REPEATABLE READ

    -- Update the balance of the account with id 1 adding 10 to current balance
END;
