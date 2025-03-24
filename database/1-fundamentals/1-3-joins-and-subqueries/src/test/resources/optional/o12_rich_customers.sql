--Task: Identify Above-Average Balance Customers
--
--Write a query to find customers whose total account balance exceeds the bank's average.
--Requirements:
--- Show customer's first and last name
--- Calculate total balance across all accounts for each customer
--- Compare against the average balance across ALL accounts
--- Only include customers whose total balance is above average
--- Use subquery to determine the average balance
--- Ensure accurate balance aggregation per customer
--
--Expected columns: first_name, last_name, total_balance
SELECT
    c.first_name,
    c.last_name,
    SUM(a.balance) as total_balance
FROM customers c
JOIN accounts a ON c.customer_id = a.customer_id
GROUP BY c.customer_id, c.first_name, c.last_name
HAVING SUM(a.balance) > (
    SELECT AVG(balance) FROM accounts
);