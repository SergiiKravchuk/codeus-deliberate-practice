-- 40. Find the average balance of customers who have more than one account.
SELECT customer_id, AVG(balance) AS avg_balance
FROM accounts
GROUP BY customer_id
HAVING COUNT(*) > 1;