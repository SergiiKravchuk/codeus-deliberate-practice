--Create a stored procedure that demonstrates isolation levels and returns results
BEGIN;
    SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
    UPDATE accounts
    SET balance = balance + 10
    WHERE account_id = 1;
END;
