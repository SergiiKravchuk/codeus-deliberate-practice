--Task: Find All Customers with Their Account Details
--Requirements:
--- Show customer name and their account type
--- Only include customers who have accounts
--- Display account balance
--- Basic INNER JOIN example
--
--Expected columns: first_name, last_name, account_type, balance
SELECT
    c.first_name,
    c.last_name,
    a.account_type,
    a.balance
FROM customers c
INNER JOIN accounts a ON c.customer_id = a.customer_id;