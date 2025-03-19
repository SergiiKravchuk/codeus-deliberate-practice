-- 41. Find loan statuses where the average interest rate is above 5%.
SELECT status, AVG(interest_rate) AS avg_interest_rate
FROM loans
GROUP BY status
HAVING AVG(interest_rate) > 5;