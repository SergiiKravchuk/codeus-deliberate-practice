-- Task 14: For each transaction, calculate the sum of amounts for all transactions belonging to the same account that occurred on the same calendar day.
-- Goal: Understand the total daily activity value for each account on days it was used.
-- Display: account_id, transaction_id, transaction_date, amount, total_daily_amount_for_account.

SELECT
    account_id,
    transaction_id,
    transaction_date,
    amount,
    SUM(amount) OVER (PARTITION BY account_id, DATE(transaction_date)) as total_daily_amount_for_account
FROM transactions;
