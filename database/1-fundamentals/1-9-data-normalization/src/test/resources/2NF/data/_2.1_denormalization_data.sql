-- Script to generate 100,000 customer records for customers_1nf table
-- This script uses PostgreSQL-specific functions for data generation

DROP TABLE IF EXISTS customers_1nf;

CREATE TABLE IF NOT EXISTS customers_1nf
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

-- Script to generate approximately 100,000 customer records for customers_1nf table
-- This script uses a simple approach to ensure no duplicate (ssn, contact_type) combinations

-- First, truncate the table if it already has data
TRUNCATE TABLE customers_1nf;

-- Modify table definition if needed to ensure we have the right primary key
-- If your table definition is different, adjust this accordingly
ALTER TABLE customers_1nf DROP CONSTRAINT IF EXISTS customers_1nf_pkey;
ALTER TABLE customers_1nf ADD PRIMARY KEY (ssn, contact_type);

-- Define our contact types
CREATE TEMPORARY TABLE contact_types(
                                        type_id SERIAL PRIMARY KEY,
                                        contact_type VARCHAR(30)
);

INSERT INTO contact_types(contact_type) VALUES
                                            ('Personal Phone'),
                                            ('Work Phone'),
                                            ('Personal Email'),
                                            ('Work Email'),
                                            ('Fax'),
                                            ('Emergency Contact'),
                                            ('Home Address'),
                                            ('Work Address'),
                                            ('Social Media'),
                                            ('Skype ID'),
                                            ('WhatsApp'),
                                            ('Telegram');

-- Generate SSNs for our customers
CREATE TEMPORARY TABLE customer_ssns AS
SELECT
    300000000 + n AS ssn
FROM
    generate_series(1, 50000) AS n;

-- Generate names for our reliability scores
CREATE TEMPORARY TABLE reliability_types AS
SELECT
    score_value,
    reliability_score
FROM (
         VALUES
             (1, 'vip'),
             (2, 'most reliable'),
             (3, 'reliable'),
             (4, 'probably reliable'),
             (5, 'not reliable')
     ) AS t(score_value, reliability_score);

-- Generate a table that assigns a name and reliability score to each SSN
CREATE TEMPORARY TABLE customers AS
SELECT
    c.ssn,
    'Customer ' || c.ssn AS customer_name,
    CASE
        WHEN random() < 0.1 THEN 'vip'
        WHEN random() < 0.3 THEN 'most reliable'
        WHEN random() < 0.6 THEN 'reliable'
        WHEN random() < 0.8 THEN 'probably reliable'
        ELSE 'not reliable'
        END AS reliability_score
FROM
    customer_ssns c;

-- Generate a distribution of contacts for each customer
-- We'll directly generate all the combinations we need
INSERT INTO customers_1nf (ssn, customer_name, contact_type, contact_value, reliability_score)
SELECT
    c.ssn,
    c.customer_name,
    t.contact_type,
    CASE
        WHEN t.contact_type LIKE '%Phone%' OR t.contact_type = 'Fax' OR t.contact_type = 'WhatsApp' OR t.contact_type = 'Emergency Contact'
            THEN '+1' || (floor(random() * 900) + 100)::TEXT || (floor(random() * 900) + 100)::TEXT || (floor(random() * 9000) + 1000)::TEXT
        WHEN t.contact_type LIKE '%Email%'
            THEN 'user' || c.ssn % 10000 || '@' ||
                 (ARRAY['gmail.com', 'yahoo.com', 'hotmail.com', 'outlook.com', 'protonmail.com'])[1 + (c.ssn % 5)]
        WHEN t.contact_type LIKE '%Address%'
            THEN (c.ssn % 9999 + 1)::TEXT || ' ' ||
                 (ARRAY['Main', 'Oak', 'Maple', 'Pine', 'Cedar'])[1 + (c.ssn % 5)] || ' ' ||
                 (ARRAY['St', 'Ave', 'Blvd', 'Rd', 'Ln'])[1 + (c.ssn % 5)]
        WHEN t.contact_type = 'Social Media'
            THEN '@user' || (c.ssn % 10000)
        WHEN t.contact_type = 'Skype ID'
            THEN 'live:user' || (c.ssn % 10000)
        WHEN t.contact_type = 'Telegram'
            THEN '@t_user' || (c.ssn % 10000)
        ELSE 'Value for ' || t.contact_type
        END AS contact_value,
    c.reliability_score
FROM
    customers c
        CROSS JOIN
    contact_types t
WHERE
  -- This ensures each customer has a random but reasonable number of contacts
  -- We pick contacts based on the customer's SSN to ensure consistent randomness
    (c.ssn % 12) + 1 <= t.type_id AND t.type_id <= (c.ssn % 6) + 7;

-- Verify count
SELECT COUNT(*) AS total_records FROM customers_1nf;

-- Clean up temporary tables
DROP TABLE IF EXISTS contact_types;
DROP TABLE IF EXISTS customer_ssns;
DROP TABLE IF EXISTS reliability_types;
DROP TABLE IF EXISTS customers;

-- Output some statistics about the generated data
SELECT
    reliability_score,
    COUNT(*) AS count,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM customers_1nf), 1) AS percentage
FROM
    customers_1nf
GROUP BY
    reliability_score
ORDER BY
    CASE reliability_score
        WHEN 'vip' THEN 1
        WHEN 'most reliable' THEN 2
        WHEN 'reliable' THEN 3
        WHEN 'probably reliable' THEN 4
        WHEN 'not reliable' THEN 5
        END;


-- Add 'Petro Cherpak'
INSERT INTO customers_1nf (ssn, customer_name, contact_type, contact_value, reliability_score)
VALUES (100000001, 'Petro Cherpak', 'Personal Phone', '+2708291611', 'vip');

INSERT INTO customers_1nf (ssn, customer_name, contact_type, contact_value, reliability_score)
VALUES (123456789, 'Ivan Kozhumyaka', 'Work Phone', '+49382916132', 'vip'),
       (123456789, 'Ivan Kozhumyaka', 'Personal Phone', '+27082916132', 'vip');

INSERT INTO customers_1nf (ssn, customer_name, contact_type, contact_value, reliability_score)
VALUES (200000002, 'Ivan Kozhumyaka', 'Personal Email', 'vanyook@gmail.com', 'reliable'),
       (200000002, 'Ivan Kozhumyaka', 'Work Email', 'ivan_kozhumyaka@google.com', 'reliable'),
       (200000002, 'Ivan Kozhumyaka', 'Work Phone', '+380931244433', 'reliable'),
       (200000002, 'Ivan Kozhumyaka', 'Personal Phone', '+380931111111', 'reliable');


