------------------------------------------------------------------------------------------------------------------------
-- TASK 1: Create a View for Active Loans with Outstanding Balances
------------------------------------------------------------------------------------------------------------------------
-- Description: Create a view named 'active_loans_view' that shows all active loans with their customer information.
-- The view should include:
-- - Customer's first and last name
-- - Customer's email
-- - Loan amount
-- - Interest rate
-- - Term in months
-- - Status (should only show 'active' loans)
-- - Monthly payment amount (calculated as: amount * (1 + interest_rate/100) / term_months)
--
-- View Name: active_loans_view
--
-- Base this view on the customers and loans tables.
-- Ensure that only active loans are included in the view.
--
-- Query to test the view:
-- SELECT * FROM active_loans_view
--
-- TODO: Create View:

CREATE VIEW active_loans_view AS
SELECT
    c.first_name,
    c.last_name,
    c.email,
    l.amount,
    l.interest_rate,
    l.term_months,
    l.status,
    (l.amount * (1 + l.interest_rate/100) / l.term_months) AS monthly_payment
FROM
    customers c
JOIN
    loans l ON c.id = l.customer_id
WHERE
    l.status = 'active';

-- EXPLANATION:
-- This SQL creates a view that joins the customers and loans tables.
-- Select customer details (first_name, last_name, email) from the customers table.
-- We select loan details (amount, interest_rate, term_months, status) from the loans table.
-- Calculate monthly_payment as: amount * (1 + interest_rate/100) / term_months.
-- This formula distributes the principal plus interest evenly across the loan term.
-- The WHERE clause ensures we only include loans with 'active' status.
--
-- Views provide several benefits:
-- 1. Simplify complex queries by encapsulating the JOIN logic
-- 2. Provide a security layer (users can access the view without direct table access)
-- 3. Create a consistent interface for applications to query loan data
-- 4. Hide the implementation details of how the data is stored and structured