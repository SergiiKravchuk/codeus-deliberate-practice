--Find transactions where `transaction_type` starts with 'deposit' (case-insensitive).
SELECT *
FROM Transactions
WHERE transaction_type ILIKE 'deposit%';