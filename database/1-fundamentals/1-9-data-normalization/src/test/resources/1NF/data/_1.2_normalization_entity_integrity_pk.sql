-- Migration script to transfer data from customers_normalized_order to customers_normalized_order_with_pk
-- Generates random unique SSNs for each customer

-- First, ensure the destination table is empty
TRUNCATE TABLE customers_normalized_order_with_pk;

-- Function to generate random SSN in valid format (between 100000000 and 999999999)
CREATE OR REPLACE FUNCTION random_ssn()
    RETURNS BIGINT AS $$
BEGIN
    -- Generate 9-digit number (SSNs are 9 digits)
    -- Avoid starting with 000, 666, and 900-999 to match real SSN rules
    -- First 3 digits (area number)
    RETURN (
        -- First digit: 0-8 (avoiding 9)
        (floor(random() * 9)::INT * 100000000) +
            -- Second digit: 0-9
        (floor(random() * 10)::INT * 10000000) +
            -- Third digit: 0-9
        (floor(random() * 10)::INT * 1000000) +
            -- Group number (2 digits): 01-99
        (floor(random() * 99 + 1)::INT * 10000) +
            -- Serial number (4 digits): 0001-9999
        (floor(random() * 9999 + 1)::INT)
        );
END;
$$ LANGUAGE plpgsql;

-- Create a temporary table with unique SSNs
CREATE TEMPORARY TABLE temp_ssn_mapping AS
WITH customer_count AS (
    SELECT COUNT(*) as count FROM customers_normalized_order
),
     random_ssns AS (
         SELECT
                     ROW_NUMBER() OVER () AS row_num,
                     random_ssn() AS ssn
         FROM
             generate_series(1, (SELECT count FROM customer_count) * 2) -- Generate twice as many SSNs as needed to handle potential duplicates
     ),
     unique_ssns AS (
         SELECT DISTINCT ssn FROM random_ssns
     ),
     limited_ssns AS (
         SELECT ssn, ROW_NUMBER() OVER () AS row_num
         FROM unique_ssns
         LIMIT (SELECT count FROM customer_count)
     ),
     customer_rows AS (
         SELECT
                     ROW_NUMBER() OVER () AS row_num,
                     customer_name,
                     customer_contact,
                     reliability_score
         FROM
             customers_normalized_order
     )
SELECT
    c.customer_name,
    c.customer_contact,
    c.reliability_score,
    s.ssn
FROM
    customer_rows c
        JOIN
    limited_ssns s ON c.row_num = s.row_num;

-- Now insert the data with unique SSNs
INSERT INTO customers_normalized_order_with_pk (ssn, customer_name, customer_contact, reliability_score)
SELECT
    ssn,
    customer_name,
    customer_contact,
    reliability_score
FROM
    temp_ssn_mapping;

-- Clean up
DROP FUNCTION IF EXISTS random_ssn();
DROP TABLE IF EXISTS temp_ssn_mapping;
