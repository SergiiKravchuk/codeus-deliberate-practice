--Retrieve all current (checking) accounts that have a balance either less than 1000 or greater than 100000.
SELECT *
FROM Accounts
WHERE account_type = 'checking' AND (balance < 1000 OR balance > 100000);