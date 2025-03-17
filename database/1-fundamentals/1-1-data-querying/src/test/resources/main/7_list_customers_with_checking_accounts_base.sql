--Retrieve a list of all customers who have at least one checking account.
SELECT customer_name
FROM Customers
WHERE customer_id IN (SELECT DISTINCT customer_id FROM Accounts WHERE account_type = 'checking');