-- 29. Finds the average balance for accounts that have more than 2000 in them.
SELECT AVG(balance) as avg_balance
FROM accounts
WHERE balance > 2000;