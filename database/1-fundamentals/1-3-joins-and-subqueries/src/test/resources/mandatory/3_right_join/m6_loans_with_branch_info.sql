--Task: Display All Loans with Branch Information
--Requirements:
--- Show all loans
--- Include branch details where loan was issued
--- Display loan amount and branch name
--- Basic RIGHT JOIN example
--
--Expected columns: loan_type, amount, branch_name
SELECT
    l.loan_type,
    l.amount,
    b.branch_name
FROM branches b
RIGHT JOIN loans l ON b.branch_id = l.branch_id;