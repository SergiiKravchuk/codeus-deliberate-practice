--Task: Generate Customer Product Holdings Report
--
--Create a query to summarize all banking products held by each customer.
--Requirements:
--- Display customer's first and last name
--- Count unique accounts owned by each customer
--- Count unique loans taken by each customer
--- Include all customers even if they don't have any products
--- Ensure no duplicate counting of accounts or loans
--- Include customers with zero products
--
--Expected columns: first_name, last_name, account_count, loan_count
SELECT
    c.first_name,
    c.last_name,
    COUNT(DISTINCT a.account_id) as account_count,
    COUNT(DISTINCT l.loan_id) as loan_count
FROM customers c
LEFT JOIN accounts a ON c.customer_id = a.customer_id
LEFT JOIN loans l ON c.customer_id = l.customer_id
GROUP BY c.customer_id, c.first_name, c.last_name;