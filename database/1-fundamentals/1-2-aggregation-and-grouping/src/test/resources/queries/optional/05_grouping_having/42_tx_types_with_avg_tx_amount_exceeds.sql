-- 42. Find transaction types where the average transaction amount is greater than $250.
SELECT transaction_type, AVG(amount) AS avg_tx_amount
FROM transactions
GROUP BY transaction_type
HAVING AVG(amount) > 250;