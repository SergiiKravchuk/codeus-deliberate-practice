--Retrieve all transactions that occurred in September 2023 and have an amount greater than $8,000. Display the transaction date, account number, transaction type, and amount. Order the results by transaction date, then by amount in descending order.
SELECT *
FROM Transactions
WHERE transaction_date >= '2023-09-01' AND transaction_date <= '2023-09-30' AND amount > 8000
ORDER BY transaction_date ASC, amount DESC;