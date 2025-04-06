-- Task 5: Find the top 3 highest transactions (based on absolute amount, regardless of type) for each account.
-- Display account_id, transaction_id, transaction_date, amount, and the rank using RANK() or DENSE_RANK().

WITH RankedTransactions AS (
    SELECT
        account_id,
        transaction_id,
        transaction_date,
        amount,
        RANK() OVER (PARTITION BY account_id ORDER BY ABS(amount) DESC) as transaction_rank
    FROM transactions
)
SELECT
    account_id,
    transaction_id,
    transaction_date,
    amount,
    transaction_rank
FROM RankedTransactions
WHERE transaction_rank <= 3;
