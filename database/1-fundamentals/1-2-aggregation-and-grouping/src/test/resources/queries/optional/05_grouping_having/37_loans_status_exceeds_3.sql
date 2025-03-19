-- 37. Find loan statuses where the total number of loans exceeds 3.
SELECT status, COUNT(*) AS loan_count
FROM loans
GROUP BY status
HAVING COUNT(*) > 3;