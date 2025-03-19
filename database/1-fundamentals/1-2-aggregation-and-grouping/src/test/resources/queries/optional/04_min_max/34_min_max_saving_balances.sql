-- 34: Find the minimum and maximum balances for savings accounts with balances over $5,000
SELECT MIN(balance) AS min_savings,
       MAX(balance) AS max_savings
FROM accounts
WHERE account_type = 'savings' AND balance > 5000;