-- 12: Find the minimum and maximum account balance for checking accounts only
SELECT MIN(balance) AS lowest_balance,
       MAX(balance) AS highest_balance
FROM accounts
WHERE account_type = 'checking';