-- 22. Find the total amount of loans per loan status.
SELECT status, SUM(amount) AS total_loan_amount
FROM loans
GROUP BY status;