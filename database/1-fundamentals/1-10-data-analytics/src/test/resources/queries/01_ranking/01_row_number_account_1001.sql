-- Task 1: Assign a unique row number to each transaction for account 1001, ordered by transaction date (earliest first).
-- Select transaction_id, transaction_date, amount, and the row number.

SELECT
    transaction_id,
    transaction_date,
    amount,
    ROW_NUMBER() OVER (ORDER BY transaction_date ASC) as rn
FROM transactions
WHERE account_id = 1001;
