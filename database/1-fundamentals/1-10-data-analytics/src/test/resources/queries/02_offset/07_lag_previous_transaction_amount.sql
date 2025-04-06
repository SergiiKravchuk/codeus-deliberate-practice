-- Task 7: For each transaction in account 1001, show the amount of the *previous* transaction for that same account. If it's the first transaction, show 0.00 as the default.
-- Order by transaction_date. Display transaction_id, transaction_date, amount, previous_transaction_amount.

SELECT
    transaction_id,
    transaction_date,
    amount,
    LAG(amount, 1, 0.00) OVER (PARTITION BY account_id ORDER BY transaction_date ASC) as previous_transaction_amount
FROM transactions
WHERE account_id = 1001;
