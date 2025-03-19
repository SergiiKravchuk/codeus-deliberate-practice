-- 28. Find the average balance per customer.
SELECT customer_id, AVG(balance) AS avg_balance
FROM accounts
GROUP BY customer_id;