-- Task 8: Calculate the number of days between consecutive transactions for account 1003.
-- Order by transaction_date. Display transaction_id, transaction_date, days_since_last_transaction. Hint: Use LAG() on the date.

select
    transaction_id,
    transaction_date,
    transaction_date::date - lag(transaction_date::date, 1) OVER (PARTITION BY account_id ORDER BY transaction_date ASC) as days_since_last_transaction
FROM transactions
WHERE account_id = 1003;
