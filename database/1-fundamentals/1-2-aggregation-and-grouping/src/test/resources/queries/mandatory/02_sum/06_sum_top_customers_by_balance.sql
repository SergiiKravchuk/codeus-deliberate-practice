-- 6. Find the top 3 customers with the highest total balance.
SELECT customer_id, SUM(balance) AS total_balance
FROM accounts
GROUP BY customer_id
ORDER BY total_balance DESC
LIMIT 3;