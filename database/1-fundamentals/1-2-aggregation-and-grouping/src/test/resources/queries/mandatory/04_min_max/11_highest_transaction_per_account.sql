-- 11. Find the highest transaction amount per account.
SELECT account_id, MAX(amount) AS max_transaction
FROM transactions
GROUP BY account_id;