-- 17. Count the number of loans for each loan status.
SELECT status, COUNT(*) AS total_loans
FROM loans
GROUP BY status;