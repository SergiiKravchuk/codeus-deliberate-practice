-- 8. Find the average transaction amount per transaction type.
SELECT transaction_type, AVG(amount) AS avg_transaction
FROM transactions
GROUP BY transaction_type;