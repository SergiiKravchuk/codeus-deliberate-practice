-- Task 11: For each transaction of account 1001, calculate the cumulative sum of amounts up to that point (including the current transaction), ordered by date. This shows the account balance after each transaction.
-- Display: transaction_id, transaction_date, amount, running_total_balance.

SELECT
    transaction_id,
    transaction_date,
    amount,
    SUM(amount) OVER (PARTITION BY account_id ORDER BY transaction_date ASC ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) as running_total_balance
FROM transactions
WHERE account_id = 1001;
