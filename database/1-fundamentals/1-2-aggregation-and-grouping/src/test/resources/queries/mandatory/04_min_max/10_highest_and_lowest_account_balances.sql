-- 10. Find the highest and lowest account balances.
SELECT MAX(balance) AS max_balance, MIN(balance) AS min_balance
FROM accounts;