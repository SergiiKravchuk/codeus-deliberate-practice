--Task: Create Customer Transaction Activity Report
--
--Write a query to analyze customer transaction patterns using CTEs.
--Requirements:
--- Show customer's first and last name
--- Calculate total amount of deposits
--- Calculate total amount of withdrawals
--- Count total number of transactions
--- Consider all transaction types ('deposit' and 'withdrawal')
--- Use appropriate conditional logic for transaction types
--- Sort results by transaction count in descending order
--- Use CTE for better query organization
--
--Expected columns: first_name, last_name, total_deposits, total_withdrawals, transaction_count
WITH customer_transactions AS (
    SELECT
        c.customer_id,
        SUM(CASE WHEN t.transaction_type = 'deposit' THEN t.amount ELSE 0 END) as total_deposits,
        SUM(CASE WHEN t.transaction_type = 'withdrawal' THEN t.amount ELSE 0 END) as total_withdrawals,
        COUNT(t.transaction_id) as transaction_count
    FROM customers c
    JOIN accounts a ON c.customer_id = a.customer_id
    JOIN transactions t ON a.account_id = t.account_id
    GROUP BY c.customer_id
)
SELECT
    c.first_name,
    c.last_name,
    ct.total_deposits,
    ct.total_withdrawals,
    ct.transaction_count
FROM customers c
JOIN customer_transactions ct ON c.customer_id = ct.customer_id
ORDER BY ct.transaction_count DESC;