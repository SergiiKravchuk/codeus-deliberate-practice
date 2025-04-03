------------------------------------------------------------------------------------------------------------------------
-- TASK 9: Improve Performance for LIKE Query on Customers Email
------------------------------------------------------------------------------------------------------------------------
-- Description:
-- A B-tree index has been created on the "email" column of the "customers" table.
--
-- Index name: idx_customers_email
--
-- Given the current index:
CREATE INDEX idx_customers_email ON customers(email);
--
-- And the following query:
SELECT * FROM customers WHERE email LIKE 'sarah.williams%';
--
-- Problem:
-- In some PostgreSQL configurations, the default collation may prevent the efficient
-- use of the B-tree index for LIKE queries with a prefix pattern.
--
-- Your task:
-- 1. Analyze the query using EXPLAIN ANALYZE and observe whether the index is used.
-- 2. If the index is not used effectively, drop the current index.
-- 3. Create a new B-tree index on "email" using the "text_pattern_ops" operator class,
--    which improves the performance of prefix LIKE queries.
-- 4. Re-run the query using EXPLAIN ANALYZE to verify that the new index is utilized.
--
-- TODO: Drop index:

-- TODO: Create index:

--

