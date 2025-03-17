--Retrieve a list of customers who have savings accounts and live in either 'Kyiv' or 'Lviv', ordered by city and then customer name.
SELECT customer_name
FROM Customers
WHERE customer_id IN (SELECT DISTINCT customer_id FROM Accounts WHERE account_type = 'savings')
  AND (city = 'Kyiv' OR city = 'Lviv')
ORDER BY city ASC, customer_name ASC;