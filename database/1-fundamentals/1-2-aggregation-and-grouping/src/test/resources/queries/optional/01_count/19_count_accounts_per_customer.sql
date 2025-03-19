-- 19. Find the number of accounts per customer.
SELECT customer_id, COUNT(*) AS account_count
FROM accounts
GROUP BY customer_id;