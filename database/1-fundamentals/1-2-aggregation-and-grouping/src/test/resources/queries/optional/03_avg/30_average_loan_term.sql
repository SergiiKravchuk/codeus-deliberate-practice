-- 30: Calculate the average loan term (in months) for closed loans
SELECT AVG(term_months) AS avg_loan_term
FROM loans
WHERE status = 'closed';