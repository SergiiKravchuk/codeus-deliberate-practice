-- Migration script to transfer data from customers_normalized_order_with_pk to customers_1nf
-- This script creates multiple rows per customer (one for each contact type)

-- First, ensure the destination table is empty
TRUNCATE TABLE customers_1nf;

-- First, we need to alter the table to increase the contact_value size
ALTER TABLE customers_1nf ALTER COLUMN contact_value TYPE VARCHAR(300);

-- Create helper functions to extract specific contact values
CREATE OR REPLACE FUNCTION extract_personal_phone(contact_str VARCHAR)
    RETURNS VARCHAR AS $$
DECLARE
    personal_phone_pattern VARCHAR := 'Personal phone: (\+1[0-9]{10})';
BEGIN
    IF contact_str IS NULL THEN
        RETURN NULL;
    END IF;

    -- Try to extract the phone number using a capturing group
    RETURN (SELECT regexp_matches(contact_str, personal_phone_pattern))[1];

EXCEPTION
    WHEN OTHERS THEN
        RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION extract_work_phone(contact_str VARCHAR)
    RETURNS VARCHAR AS $$
DECLARE
    work_phone_pattern VARCHAR := 'Work phone: (\+1[0-9]{10})';
BEGIN
    IF contact_str IS NULL THEN
        RETURN NULL;
    END IF;

    -- Try to extract the phone number using a capturing group
    RETURN (SELECT regexp_matches(contact_str, work_phone_pattern))[1];

EXCEPTION
    WHEN OTHERS THEN
        RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION extract_personal_email(contact_str VARCHAR)
    RETURNS VARCHAR AS $$
DECLARE
    personal_email_pattern VARCHAR := 'Personal email: ([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,})';
BEGIN
    IF contact_str IS NULL THEN
        RETURN NULL;
    END IF;

    -- Try to extract the email using a capturing group
    RETURN (SELECT regexp_matches(contact_str, personal_email_pattern))[1];

EXCEPTION
    WHEN OTHERS THEN
        RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION extract_work_email(contact_str VARCHAR)
    RETURNS VARCHAR AS $$
DECLARE
    work_email_pattern VARCHAR := 'Work email: ([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,})';
BEGIN
    IF contact_str IS NULL THEN
        RETURN NULL;
    END IF;

    -- Try to extract the email using a capturing group
    RETURN (SELECT regexp_matches(contact_str, work_email_pattern))[1];

EXCEPTION
    WHEN OTHERS THEN
        RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Create a temporary table to hold all the extracted contacts
CREATE TEMPORARY TABLE temp_extracted_contacts AS
SELECT
    ssn,
    customer_name,
    reliability_score,
    'Personal Phone' AS contact_type,
    extract_personal_phone(customer_contact) AS contact_value
FROM
    customers_normalized_order_with_pk
WHERE
    extract_personal_phone(customer_contact) IS NOT NULL

UNION ALL

SELECT
    ssn,
    customer_name,
    reliability_score,
    'Work Phone' AS contact_type,
    extract_work_phone(customer_contact) AS contact_value
FROM
    customers_normalized_order_with_pk
WHERE
    extract_work_phone(customer_contact) IS NOT NULL

UNION ALL

SELECT
    ssn,
    customer_name,
    reliability_score,
    'Personal Email' AS contact_type,
    extract_personal_email(customer_contact) AS contact_value
FROM
    customers_normalized_order_with_pk
WHERE
    extract_personal_email(customer_contact) IS NOT NULL

UNION ALL

SELECT
    ssn,
    customer_name,
    reliability_score,
    'Work Email' AS contact_type,
    extract_work_email(customer_contact) AS contact_value
FROM
    customers_normalized_order_with_pk
WHERE
    extract_work_email(customer_contact) IS NOT NULL;

-- Insert the extracted contacts into the destination table
INSERT INTO customers_1nf (ssn, customer_name, contact_type, contact_value, reliability_score)
SELECT ssn, customer_name, contact_type, contact_value, reliability_score
FROM temp_extracted_contacts;

-- Clean up
DROP TABLE IF EXISTS temp_extracted_contacts;
DROP FUNCTION IF EXISTS extract_personal_phone(VARCHAR);
DROP FUNCTION IF EXISTS extract_work_phone(VARCHAR);
DROP FUNCTION IF EXISTS extract_personal_email(VARCHAR);
DROP FUNCTION IF EXISTS extract_work_email(VARCHAR);
