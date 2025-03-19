-- 20. Find the number of transactions per account.
SELECT account_id, COUNT(*) AS transaction_count
FROM transactions
GROUP BY account_id;