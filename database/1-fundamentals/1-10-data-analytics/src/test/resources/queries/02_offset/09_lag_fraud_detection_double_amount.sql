-- Task 9 (Potential Fraud Detection): Identify transactions where the amount is more than double the amount of the previous transaction for the *same account* on the *same day*.
-- Display account_id, transaction_id, transaction_date, amount, previous_transaction_amount.

WITH TransactionComparison AS (
    SELECT
        account_id,
        transaction_id,
        transaction_date,
        amount,
        LAG(amount) OVER (PARTITION BY account_id, DATE(transaction_date) ORDER BY transaction_date ASC) as previous_transaction_amount_same_day
    FROM transactions
)
SELECT
    account_id,
    transaction_id,
    transaction_date,
    amount,
    previous_transaction_amount_same_day
FROM TransactionComparison
WHERE ABS(amount) > ABS(previous_transaction_amount_same_day * 2);
