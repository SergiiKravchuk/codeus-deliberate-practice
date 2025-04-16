-- Migration script to transfer data from customers_normalized_order_with_pk to customers_remove_groups
-- Extracts contact information from the customer_contact field into separate columns

-- First, ensure the destination table is empty
TRUNCATE TABLE customers_remove_groups;

-- Create helper functions to extract specific contact types from the contact string
CREATE OR REPLACE FUNCTION extract_personal_phone(contact_str VARCHAR)
    RETURNS VARCHAR AS $$
DECLARE
    personal_phone_pattern VARCHAR := 'Personal phone: \+1[0-9]{10}';
    extracted_text VARCHAR;
BEGIN
    IF contact_str IS NULL THEN
        RETURN NULL;
    END IF;

    -- Extract the text matching the pattern
    extracted_text := (SELECT substring(contact_str FROM personal_phone_pattern));

    -- If we found a match, extract just the phone number portion
    IF extracted_text IS NOT NULL THEN
        RETURN substring(extracted_text FROM '\+1[0-9]{10}');
    ELSE
        RETURN NULL;
    END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION extract_work_phone(contact_str VARCHAR)
    RETURNS VARCHAR AS $$
DECLARE
    work_phone_pattern VARCHAR := 'Work phone: \+1[0-9]{10}';
    extracted_text VARCHAR;
BEGIN
    IF contact_str IS NULL THEN
        RETURN NULL;
    END IF;

    -- Extract the text matching the pattern
    extracted_text := (SELECT substring(contact_str FROM work_phone_pattern));

    -- If we found a match, extract just the phone number portion
    IF extracted_text IS NOT NULL THEN
        RETURN substring(extracted_text FROM '\+1[0-9]{10}');
    ELSE
        RETURN NULL;
    END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION extract_personal_email(contact_str VARCHAR)
    RETURNS VARCHAR AS $$
DECLARE
    personal_email_pattern VARCHAR := 'Personal email: [a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}';
    extracted_text VARCHAR;
BEGIN
    IF contact_str IS NULL THEN
        RETURN NULL;
    END IF;

    -- Extract the text matching the pattern
    extracted_text := (SELECT substring(contact_str FROM personal_email_pattern));

    -- If we found a match, extract just the email portion
    IF extracted_text IS NOT NULL THEN
        RETURN substring(extracted_text FROM '[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}');
    ELSE
        RETURN NULL;
    END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION extract_work_email(contact_str VARCHAR)
    RETURNS VARCHAR AS $$
DECLARE
    work_email_pattern VARCHAR := 'Work email: [a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}';
    extracted_text VARCHAR;
BEGIN
    IF contact_str IS NULL THEN
        RETURN NULL;
    END IF;

    -- Extract the text matching the pattern
    extracted_text := (SELECT substring(contact_str FROM work_email_pattern));

    -- If we found a match, extract just the email portion
    IF extracted_text IS NOT NULL THEN
        RETURN substring(extracted_text FROM '[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}');
    ELSE
        RETURN NULL;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- Perform the migration with contact information extraction
INSERT INTO customers_remove_groups (
    ssn,
    customer_name,
    personal_phone,
    work_phone,
    personal_email,
    work_email,
    reliability_score
)
SELECT
    ssn::INT, -- Cast to INT as per the target table definition
    customer_name,
    extract_personal_phone(customer_contact),
    extract_work_phone(customer_contact),
    extract_personal_email(customer_contact),
    extract_work_email(customer_contact),
    reliability_score
FROM
    customers_normalized_order_with_pk;

-- Clean up the helper functions
DROP FUNCTION IF EXISTS extract_personal_phone(VARCHAR);
DROP FUNCTION IF EXISTS extract_work_phone(VARCHAR);
DROP FUNCTION IF EXISTS extract_personal_email(VARCHAR);
DROP FUNCTION IF EXISTS extract_work_email(VARCHAR);
