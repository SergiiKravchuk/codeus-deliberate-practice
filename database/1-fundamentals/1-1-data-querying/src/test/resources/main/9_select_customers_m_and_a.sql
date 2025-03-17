--Find customers whose names begin with the letter 'M' (case-insensitive) and also contain the letter 'а' (case-insensitive) anywhere in their name.
SELECT *
FROM Customers
WHERE LOWER(customer_name) LIKE 'm%' AND LOWER(customer_name) LIKE '%a%';