------------------------------------------------------------------------------------------------------------------------
-- TASK 7: Function-Based Index on Lower(first_name)
------------------------------------------------------------------------------------------------------------------------
-- Description: Create an expression index on lower(first_name) to support case-insensitive queries.
--
-- Index Name: idx_customers_lower_first_name
--
-- Analyze the query with EXPLAIN ANALYZE.
-- Query:
SELECT first_name FROM customers WHERE lower(first_name) = 'emily';
-- TODO: Create Index:
CREATE INDEX idx_customers_lower_first_name ON customers(lower(first_name));
-- After index creation: analyze the query with EXPLAIN ANALYZE again.
