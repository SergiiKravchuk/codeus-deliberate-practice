--Task: Generate Branch Loan Performance Report
--
--Create a query to analyze loan distribution across branches using CTEs.
--Requirements:
--- Display branch name
--- Calculate total number of loans per branch
--- Count approved loans separately
--- Calculate average loan amount
--- Include branches with no loans
--- Round average loan amount to 2 decimal places
--- Sort by total loans in descending order
--- Use CTE for intermediate calculations
--
--Expected columns: branch_name, total_loans, approved_loans, average_loan_amount
WITH branch_loans AS (
    SELECT
        b.branch_id,
        COUNT(l.loan_id) as total_loans,
        COUNT(CASE WHEN l.status = 'approved' THEN 1 END) as approved_loans,
        AVG(l.amount) as avg_loan_amount
    FROM branches b
    LEFT JOIN loans l ON b.branch_id = l.branch_id
    GROUP BY b.branch_id
)
SELECT
    b.branch_name,
    bl.total_loans,
    bl.approved_loans,
    ROUND(bl.avg_loan_amount, 2) as average_loan_amount
FROM branches b
JOIN branch_loans bl ON b.branch_id = bl.branch_id
ORDER BY bl.total_loans DESC;