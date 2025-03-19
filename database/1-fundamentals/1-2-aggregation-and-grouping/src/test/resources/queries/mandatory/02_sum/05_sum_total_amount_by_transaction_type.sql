-- 5. Find the total amount transacted for each transaction type.
SELECT transaction_type, SUM(amount) AS total_amount
FROM transactions
GROUP BY transaction_type;