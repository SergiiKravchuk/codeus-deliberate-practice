-- Task 12: For each 'Deposit' transaction, calculate the total amount deposited into that specific account up to that point in time, ordered by date.
-- Goal: Track the growth of deposits for each account separately.
-- Display: account_id, transaction_id, transaction_date, amount, cumulative_deposits.

SELECT
    account_id,
    transaction_id,
    transaction_date,
    amount,
    SUM(amount) OVER (PARTITION BY account_id ORDER BY transaction_date ASC ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) as cumulative_deposits
FROM transactions
WHERE transaction_type = 'Deposit';
