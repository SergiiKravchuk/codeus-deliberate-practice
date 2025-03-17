--Retrieve a list of customers who do not live in the city 'Kharkiv'.
SELECT customer_name
FROM Customers
WHERE city != 'Kharkiv';