-- 44. Find customers who have loans but no accounts.
SELECT customer_id
FROM loans
GROUP BY customer_id
HAVING customer_id NOT IN (SELECT DISTINCT customer_id FROM accounts);