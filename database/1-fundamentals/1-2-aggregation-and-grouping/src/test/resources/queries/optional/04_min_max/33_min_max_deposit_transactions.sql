-- 33: Find the smallest and largest deposit transactions
SELECT MIN(amount) AS smallest_deposit,
       MAX(amount) AS largest_deposit
FROM transactions
WHERE transaction_type = 'deposit';