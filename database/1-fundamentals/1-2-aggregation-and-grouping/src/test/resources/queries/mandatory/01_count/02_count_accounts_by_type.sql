-- 2. Count the number of accounts for each account type.
SELECT account_type, COUNT(*) AS total_accounts
FROM accounts
GROUP BY account_type;