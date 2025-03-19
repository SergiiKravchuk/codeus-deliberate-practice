-- 13. Find customers who have both a checking and a savings account.
SELECT customer_id
FROM accounts
GROUP BY customer_id
HAVING COUNT(DISTINCT account_type) = 2;