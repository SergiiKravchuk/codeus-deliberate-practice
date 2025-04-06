-- Task 20 (Optional - More Complex Fraud Detection): Identify accounts where a withdrawal occurs within 1 hour after a deposit on the same day.
-- Display account_id, deposit_transaction_id, deposit_time, withdrawal_transaction_id, withdrawal_time. Hint: Use LAG/LEAD partitioned by account and date, potentially filtering on transaction type.

-- Note: Timestamp difference calculation varies. This uses PostgreSQL interval syntax.
WITH TransactionTimeLag AS (
    SELECT
        transaction_id,
        account_id,
        transaction_date,
        transaction_type,
        amount,
        LAG(transaction_date) OVER (PARTITION BY account_id, DATE(transaction_date) ORDER BY transaction_date ASC) as prev_txn_time,
        LAG(transaction_type) OVER (PARTITION BY account_id, DATE(transaction_date) ORDER BY transaction_date ASC) as prev_txn_type,
        LAG(transaction_id) OVER (PARTITION BY account_id, DATE(transaction_date) ORDER BY transaction_date ASC) as prev_txn_id
    FROM transactions
)
SELECT
    account_id,
    prev_txn_id as deposit_transaction_id,
    prev_txn_time as deposit_time,
    transaction_id as withdrawal_transaction_id,
    transaction_date as withdrawal_time
FROM TransactionTimeLag
WHERE
    transaction_type = 'Withdrawal'
    AND prev_txn_type = 'Deposit'
    AND transaction_date <= prev_txn_time + INTERVAL '1 hour';
