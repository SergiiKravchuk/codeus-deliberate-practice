-- 9. Find the average interest rate per loan status.
SELECT status, AVG(interest_rate) AS avg_interest
FROM loans
GROUP BY status;