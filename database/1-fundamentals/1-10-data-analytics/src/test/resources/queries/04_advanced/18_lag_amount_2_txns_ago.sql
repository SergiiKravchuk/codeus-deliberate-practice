-- Task 18 (Optional): For each transaction for account 1001, show the transaction amount from 2 transactions ago. If not available, show NULL.
-- Order by transaction_date. Display transaction_id, transaction_date, amount, amount_2_txns_ago.

SELECT
    transaction_id,
    transaction_date,
    amount,
    LAG(amount, 2) OVER (PARTITION BY account_id ORDER BY transaction_date ASC) as amount_2_txns_ago
FROM transactions
WHERE account_id = 1001;
