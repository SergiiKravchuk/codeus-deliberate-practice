-- Task 6: For each transaction in account 1001, show the amount of the *next* transaction for that same account. If it's the last transaction, show NULL.
-- Order by transaction_date. Display transaction_id, transaction_date, amount, next_transaction_amount.

SELECT
    transaction_id,
    transaction_date,
    amount,
    LEAD(amount) OVER (PARTITION BY account_id ORDER BY transaction_date ASC) as next_transaction_amount
FROM transactions
WHERE account_id = 1001;
