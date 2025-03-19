-- 3. Find the total number of customers with at least one loan.
SELECT COUNT(DISTINCT customer_id) AS customers_with_loans
FROM loans;