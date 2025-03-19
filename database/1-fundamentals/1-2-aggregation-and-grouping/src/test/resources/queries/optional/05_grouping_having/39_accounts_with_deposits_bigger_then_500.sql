-- 39. Find accounts that have received deposits totaling more than $500.
SELECT account_id, SUM(amount) AS total_deposits
FROM transactions
WHERE transaction_type = 'deposit'
GROUP BY account_id
HAVING SUM(amount) > 500;