-- 16. Count the number of transactions per transaction type.
SELECT transaction_type, COUNT(*) AS transaction_count
FROM transactions
GROUP BY transaction_type;