-- Script to generate 100,000 customer records for customers and customer_contacts tables
-- This script uses PostgreSQL-specific functions for data generation

--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--


DROP TABLE IF EXISTS customer_contacts;

DROP TABLE IF EXISTS customers;

CREATE TABLE IF NOT EXISTS customers
(
    ssn               INT PRIMARY KEY,
    customer_name     VARCHAR(100) NOT NULL,
    reliability_score VARCHAR(30) NOT NULL,
    CONSTRAINT customers_reliability_score_check CHECK (
        reliability_score IN ('vip', 'most reliable', 'reliable', 'probably reliable', 'not reliable')
        )
);

CREATE TABLE IF NOT EXISTS customer_contacts
(
    ssn           INT,
    contact_type  VARCHAR(20),
    contact_value VARCHAR(30),
    PRIMARY KEY (ssn, contact_type),
    FOREIGN KEY (ssn) REFERENCES customers (ssn)
);


-- First, truncate the tables if they already have data (in the correct order due to foreign key)
TRUNCATE TABLE customer_contacts CASCADE;
TRUNCATE TABLE customers CASCADE;

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

-- Generate a table that assigns a name and reliability score to each SSN
CREATE TEMPORARY TABLE temp_customers AS
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

-- Insert data into the customers table
INSERT INTO customers (ssn, customer_name, reliability_score)
SELECT ssn, customer_name, reliability_score
FROM temp_customers;

-- Generate a distribution of contacts for each customer
-- Creating a temporary table to hold all contacts
CREATE TEMPORARY TABLE temp_contacts AS
SELECT
    c.ssn,
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
        END AS contact_value
FROM
    temp_customers c
        CROSS JOIN
    contact_types t
WHERE
  -- This ensures each customer has a random but reasonable number of contacts
  -- We pick contacts based on the customer's SSN to ensure consistent randomness
    (c.ssn % 12) + 1 <= t.type_id AND t.type_id <= (c.ssn % 6) + 7;

-- Insert data into the customer_contacts table
INSERT INTO customer_contacts (ssn, contact_type, contact_value)
SELECT ssn, contact_type, contact_value
FROM temp_contacts;

-- Clean up temporary tables
DROP TABLE IF EXISTS contact_types;
DROP TABLE IF EXISTS customer_ssns;
DROP TABLE IF EXISTS temp_customers;
DROP TABLE IF EXISTS temp_contacts;

-- Add some special customers for testing
-- Add 'Petro Cherpak'
INSERT INTO customers (ssn, customer_name, reliability_score)
VALUES (100000001, 'Petro Cherpak', 'vip');

INSERT INTO customer_contacts (ssn, contact_type, contact_value)
VALUES (100000001, 'Personal Phone', '+2708291611');

-- Add first 'Ivan Kozhumyaka'
INSERT INTO customers (ssn, customer_name, reliability_score)
VALUES (123456789, 'Ivan Kozhumyaka', 'vip');

INSERT INTO customer_contacts (ssn, contact_type, contact_value)
VALUES
    (123456789, 'Work Phone', '+49382916132'),
    (123456789, 'Personal Phone', '+27082916132');

-- Add second 'Ivan Kozhumyaka'
INSERT INTO customers (ssn, customer_name, reliability_score)
VALUES (200000002, 'Ivan Kozhumyaka', 'reliable');

INSERT INTO customer_contacts (ssn, contact_type, contact_value)
VALUES
    (200000002, 'Personal Email', 'vanyook@gmail.com'),
    (200000002, 'Work Email', 'ivan_kozhumyaka@google.com'),
    (200000002, 'Work Phone', '+380931244433'),
    (200000002, 'Personal Phone', '+380931111111');
