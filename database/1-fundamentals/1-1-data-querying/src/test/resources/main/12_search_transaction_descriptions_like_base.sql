--Find transactions with `transaction_type` similar to 'deposit' (case-sensitive).
SELECT *
FROM Transactions
WHERE transaction_type LIKE 'deposit%';