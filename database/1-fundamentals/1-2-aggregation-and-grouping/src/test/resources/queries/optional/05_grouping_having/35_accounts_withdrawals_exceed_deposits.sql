-- 35. Find accounts where total withdrawals exceed total deposits.
SELECT account_id,
       SUM(CASE WHEN transaction_type = 'deposit' THEN amount ELSE 0 END)    AS total_deposits,
       SUM(CASE WHEN transaction_type = 'withdrawal' THEN amount ELSE 0 END) AS total_withdrawals
FROM transactions
GROUP BY account_id
HAVING SUM(CASE WHEN transaction_type = 'withdrawal' THEN amount ELSE 0 END) >
       SUM(CASE WHEN transaction_type = 'deposit' THEN amount ELSE 0 END);