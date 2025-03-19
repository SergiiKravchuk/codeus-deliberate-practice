-- 32: Find the lowest and highest loan amounts for active loans with customer IDs in the range of 5 to 10
SELECT MIN(amount) AS smallest_loan,
       MAX(amount) AS largest_loan
FROM loans
WHERE status = 'active' AND customer_id BETWEEN 5 AND 10;