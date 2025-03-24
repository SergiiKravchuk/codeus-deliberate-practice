--Task: List All Customers and Their Loans (if any)
--Requirements:
--- Show all customers
--- Include loan details if they exist
--- Show customers without loans
--- Basic LEFT JOIN example
--
--Expected columns: first_name, last_name, loan_type, amount
SELECT
    c.first_name,
    c.last_name,
    l.loan_type,
    l.amount
FROM customers c
LEFT JOIN loans l ON c.customer_id = l.customer_id;



