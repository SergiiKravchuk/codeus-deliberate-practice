--Retrieve a list of all customers who live in either 'Kyiv' or 'Lviv', showing their names
SELECT customer_name
FROM Customers
WHERE city = 'Kyiv' OR city = 'Lviv';