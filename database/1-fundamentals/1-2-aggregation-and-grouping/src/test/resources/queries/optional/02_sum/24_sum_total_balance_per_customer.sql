-- 24. Find the total balance for each customer.
SELECT customer_id, SUM(balance) AS total_balance
FROM accounts
GROUP BY customer_id;