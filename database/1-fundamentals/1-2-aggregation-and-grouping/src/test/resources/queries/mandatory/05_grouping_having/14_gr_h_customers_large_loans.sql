-- 14. Find customers who have taken loans totaling more than $15,000.
SELECT customer_id, SUM(amount) AS total_loan
FROM loans
GROUP BY customer_id
HAVING SUM(amount) > 15000;