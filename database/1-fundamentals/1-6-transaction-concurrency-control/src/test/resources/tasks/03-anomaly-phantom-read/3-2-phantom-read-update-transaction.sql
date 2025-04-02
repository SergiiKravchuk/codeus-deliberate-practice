--Create a stored procedure that demonstrates isolation levels and returns results
BEGIN;
    SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
    UPDATE accounts
    SET balance = balance + 10000
    WHERE account_id = 4;
END;
