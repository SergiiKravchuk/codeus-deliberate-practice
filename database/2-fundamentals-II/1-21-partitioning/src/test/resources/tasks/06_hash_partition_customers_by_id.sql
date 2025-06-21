-- ===================================================================================================
-- TASK 6: Hash Partitioning on Customer ID
-- ===================================================================================================
--
-- DESCRIPTION:
-- In high-traffic systems with frequent inserts and updates, row-level contention can become a bottleneck.
-- To improve horizontal scalability and evenly distribute load, we will partition the `customers` table
-- using HASH partitioning on the `id` column.
--
-- PostgreSQL's HASH partitioning is ideal when the partitioning column has uniformly distributed values
-- and queries do not frequently filter by ranges.
--
-- In this task, you will:
-- - Recreate a hash-partitioned version of the `customers` table
-- - Create 4 hash partitions using MODULUS 4
-- - Insert a customer and verify that PostgreSQL routes it to the correct partition
--
-- GOAL:
-- - Partition `customers` using HASH on `id`
-- - Create 4 partitions for uniform distribution
-- - Insert sample data and confirm correct partition routing
--
-- ===================================================================================================
-- STEP 1: Drop Existing Partitioned Table (if any)
-- ===================================================================================================

DROP TABLE IF EXISTS customers_part_0 CASCADE;
DROP TABLE IF EXISTS customers_part_1 CASCADE;
DROP TABLE IF EXISTS customers_part_2 CASCADE;
DROP TABLE IF EXISTS customers_part_3 CASCADE;
DROP TABLE IF EXISTS customers_partitioned CASCADE;

-- ===================================================================================================
-- STEP 2: Create Partitioned Table
-- ===================================================================================================

CREATE TABLE customers_partitioned (
    id         INT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name  VARCHAR(50) NOT NULL,
    email      VARCHAR(100) NOT NULL,
    phone      VARCHAR(20) NOT NULL,
    address    TEXT NOT NULL
) PARTITION BY HASH (id);

-- ===================================================================================================
-- TODO STEP 3: Create 4 hash partitions using MODULUS 4
-- Table name pattern: customers_part_{modulus remainder} ({value} - value placeholder)
-- example: customers_part_0 where {modulus remainder} -> 4 % 4 = 0
-- ===================================================================================================

CREATE TABLE customers_part_0 PARTITION OF customers_partitioned
    FOR VALUES WITH (MODULUS 4, REMAINDER 0);

CREATE TABLE customers_part_1 PARTITION OF customers_partitioned
    FOR VALUES WITH (MODULUS 4, REMAINDER 1);

CREATE TABLE customers_part_2 PARTITION OF customers_partitioned
    FOR VALUES WITH (MODULUS 4, REMAINDER 2);

CREATE TABLE customers_part_3 PARTITION OF customers_partitioned
    FOR VALUES WITH (MODULUS 4, REMAINDER 3);

-- ===================================================================================================
-- STEP 4: Insert a Sample Row for Verification
-- ===================================================================================================

-- 101 % 4 = 1 → Should land in customers_part_1:
INSERT INTO customers_partitioned(id, first_name, last_name, email, phone, address) VALUES (101, 'Anna', 'Nowak', 'anna@example.com', '111-222', 'Main St');
