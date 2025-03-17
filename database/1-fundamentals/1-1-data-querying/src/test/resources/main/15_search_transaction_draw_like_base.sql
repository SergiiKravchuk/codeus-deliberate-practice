--Find transactions where `transaction_type` contains 'draw' (case-sensitive).
SELECT *
FROM Transactions
WHERE transaction_type LIKE '%draw%';