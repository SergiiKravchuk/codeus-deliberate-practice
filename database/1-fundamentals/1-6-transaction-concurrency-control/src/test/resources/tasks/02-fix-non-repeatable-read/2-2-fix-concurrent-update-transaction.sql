--Create a stored procedure that demonstrates isolation levels and returns results
BEGIN;
    SET TRANSACTION ISOLATION LEVEL REPEATABLE READ;
    UPDATE accounts
    SET balance = balance + 10
    WHERE account_id = 1;
END;
