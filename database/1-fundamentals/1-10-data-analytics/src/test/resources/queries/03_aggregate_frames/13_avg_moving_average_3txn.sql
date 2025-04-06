-- Task 13: Calculate the 3-transaction moving average for account 1001, ordered by date. The moving average for a row is the average amount of that transaction and the two immediately preceding it.
-- Goal: Smooth out short-term spending fluctuations to see trends.
-- Display: transaction_id, transaction_date, amount, moving_average_3txn.

SELECT
    transaction_id,
    transaction_date,
    amount,
    AVG(amount) OVER (PARTITION BY account_id ORDER BY transaction_date ASC ROWS BETWEEN 2 PRECEDING AND CURRENT ROW) as moving_average_3txn
FROM transactions
WHERE account_id = 1001;
