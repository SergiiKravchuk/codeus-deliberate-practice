--Retrieve all transactions with an amount greater than $10,000, sorted chronologically by transaction date.
SELECT *
FROM Transactions
WHERE amount > 10000
ORDER BY transaction_date ASC;