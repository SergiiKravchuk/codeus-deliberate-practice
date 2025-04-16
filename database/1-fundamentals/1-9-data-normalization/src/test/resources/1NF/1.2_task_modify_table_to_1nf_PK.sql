-- ====================================================================================================================
-- TASK 1.2: Create a table that will allow customer_name duplications but guarantee customer uniqueness with SSN
-- ====================================================================================================================
-- STORY: The bank manager wants to add Petro Cherpak to the system, but first checks if this customer already exists.
-- They find another customer with the same name but different contact information. After investigation, it's confirmed
-- these are two different people who happen to share the same name.
--
-- PROBLEM: We need to add customers with identical names but must ensure each record represents a unique person.
-- A UNIQUE constraint on name would prevent this legitimate scenario. Additionally, we need:
-- 1. A guaranteed way to identify each customer (entity integrity)
-- 2. An efficient lookup method for customer records
-- 3. A stable reference point for relationships with other banking tables (referential integrity)
--
-- SOLUTION: SSN (Social Security Number) provides a natural primary key that:
-- - Guarantees each record represents a unique real-world entity
-- - Establishes the foundation for referential integrity across the database
-- - Provides an efficient, indexed lookup method that doesn't depend on potentially duplicate fields
--
-- This satisfies the 1NF requirement that every table must have a primary key that uniquely identifies each row.


-- TODO:
-- Create a new table that uses SSN as the primary key for identifying customers.
-- Based on the `customers_normalized_order` structure but with these changes:
-- Copy the `customers_normalized_order`
-- New table name:  `customers_normalized_order_with_pk`
-- New field to add: `ssn`: int (9-digit number) and it is the primary key to access the customers

CREATE TABLE customers_normalized_order_with_pk
(
    ssn               INT PRIMARY KEY,
    customer_name     VARCHAR(100) NOT NULL,
    customer_contact  VARCHAR(500),
    reliability_score VARCHAR(30) CHECK (
        reliability_score IN ('vip', 'most reliable', 'reliable', 'probably reliable', 'not reliable')
        )                          NOT NULL
);