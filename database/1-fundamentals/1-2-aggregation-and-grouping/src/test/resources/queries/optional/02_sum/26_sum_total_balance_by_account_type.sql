-- 26. Find the total balance of checking and savings accounts separately.
SELECT account_type, SUM(balance) AS total_balance
FROM accounts
GROUP BY account_type;