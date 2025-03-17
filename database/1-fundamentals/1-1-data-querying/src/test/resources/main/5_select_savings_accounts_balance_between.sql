--Find all savings accounts that have a balance between 10000 and 50000 (inclusive).
SELECT *
FROM Accounts
WHERE account_type = 'savings' AND balance BETWEEN 10000 AND 50000;