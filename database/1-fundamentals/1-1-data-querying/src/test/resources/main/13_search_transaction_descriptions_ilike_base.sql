--Find transactions with `transaction_type` similar to 'deposit' (case-insensitive).
SELECT *
FROM Transactions
WHERE transaction_type ILIKE 'deposit%';