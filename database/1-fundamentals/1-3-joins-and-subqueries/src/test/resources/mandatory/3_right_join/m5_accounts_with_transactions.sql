--Task: Show All Transactions with Account Details
--Requirements:
--- List all transactions
--- Include account information
--- Show transaction amount and type
--- Basic RIGHT JOIN example
--
--Expected columns: account_type, transaction_type, amount
SELECT
    a.account_type,
    t.transaction_type,
    t.amount
FROM accounts a
RIGHT JOIN transactions t ON a.account_id = t.account_id;