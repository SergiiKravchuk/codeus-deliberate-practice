-- ====================================================================================================================
-- TASK 1.3.2: Create a table that properly normalizes contact information and removes repeating groups
-- ====================================================================================================================
-- STORY: The bank decided to add new types of contact information like 'Fax' or customer's relatives:
-- `Sister Personal number`, `Mother work number`, etc. However, the managers discovered that with the current
-- `customers_remove_groups` table, they cannot add new contact types without altering the table structure.
--
-- PROBLEM: With the `customers_remove_groups` design:
-- 1. Adding new contact types requires adding new columns (altering the schema)
-- 2. The table structure becomes increasingly complex as contact types grow
-- 3. We still have repeating groups in conceptual terms (all contact types are a group)
--
-- SOLUTION: Refactor the table to use a proper 1NF design with two columns: `contact_type` and `contact_value`
-- This approach:
-- - Allows adding new contact types without schema changes
-- - Simplifies the data structure
-- - Properly removes all repeating groups
-- - Makes data queries and updates more consistent
--
-- Note: For full flexibility, the primary key should actually include ssn and contact type.

-- TODO:
-- Create a new table with proper 1NF structure for contact information
-- Copy the `customers_normalized_order_with_pk`
-- New table name: `customers_1nf`
-- Replace the `customer_contact` with 4 new columns:
-- - contact_type VARCHAR(20)
-- - contact_value VARCHAR(30)
-- - Create a composite primary key of ssn and contact_type

CREATE TABLE customers_1nf
(
    ssn               INT,
    customer_name     VARCHAR(100) NOT NULL,
    contact_type      VARCHAR(20),
    contact_value     VARCHAR(30),
    reliability_score VARCHAR(30) CHECK (
        reliability_score IN ('vip', 'most reliable', 'reliable', 'probably reliable', 'not reliable')
        )                          NOT NULL,
    PRIMARY KEY (ssn, contact_type)
);