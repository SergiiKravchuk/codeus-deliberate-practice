--------------------------------------------------------------------------------
-- Task 1: Create a Hash Index on a Phone Column
--------------------------------------------------------------------------------
-- Description:
-- Create a hash index on the phone column in the customers table.
-- This task targets equality searches on a column that may not be very selective
-- but demonstrates hash index creation.
--
-- EXPLAIN ANALYZE QUERY:
SELECT * FROM customers WHERE phone = '1234567890';
--
-- TODO: Create index:
--
-- Index name: idx_customers_phone_hash
--
CREATE INDEX idx_customers_phone_hash ON customers USING hash (phone);
-- Run EXPLAIN ANALYZE again:
SELECT * FROM customers WHERE phone = '1234567890';
--