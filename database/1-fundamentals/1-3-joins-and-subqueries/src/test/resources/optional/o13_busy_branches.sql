--Task: Identify Branches with Above-Average Transaction Activity
--
--Write a query to find branches that handle more transactions than the average.
--Requirements:
--- Display branch name and total transaction count
--- Calculate average transactions per account across all branches
--- Only include branches with transaction count above this average
--- Use appropriate joins to connect branches, accounts, and transactions
--- Use subquery to determine the average transaction count
--- Consider all transactions for the comparison
--
--Expected columns: branch_name, transaction_count
SELECT
    b.branch_name,
    COUNT(t.transaction_id) as transaction_count
FROM branches b
JOIN accounts a ON b.branch_id = a.branch_id
JOIN transactions t ON a.account_id = t.account_id
GROUP BY b.branch_id, b.branch_name
HAVING COUNT(t.transaction_id) > (
    SELECT AVG(tx_count)
    FROM (
        SELECT COUNT(transaction_id) as tx_count
        FROM transactions
        GROUP BY account_id
    ) sub
);