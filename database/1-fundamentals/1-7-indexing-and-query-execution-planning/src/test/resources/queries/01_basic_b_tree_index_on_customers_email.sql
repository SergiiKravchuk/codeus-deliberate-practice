-- TASK 1: Basic Indexing on Customers
-- Analyze the query with EXPLAIN ANALYZE.
-- Create an index on the email column to improve performance.
--
-- Query:
SELECT * FROM customers WHERE email = 'liam.anderson@example.com';

CREATE INDEX idx_customers_email ON customers(email);
-- Solution:
-- CREATE INDEX idx_customers_email ON customers(email);
