--Retrieve the names of all customers who live in 'Kyiv', ordered alphabetically by their name.
SELECT customer_name
FROM Customers
WHERE city = 'Kyiv'
ORDER BY customer_name ASC;