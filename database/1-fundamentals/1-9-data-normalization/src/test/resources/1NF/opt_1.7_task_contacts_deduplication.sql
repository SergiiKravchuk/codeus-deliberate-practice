-- ====================================================================================================================
-- OPTIONAL TASK 1.7: Make it impossible to add email or phone number if it already exist in system
-- ====================================================================================================================
-- STORY: You have got an email from the <ruzzkiy_hacker@yandex.ru> that:
-- <Hello, I'm the 'Ruslan Zelizyaka' and I need to add my Personal number 4307891643 and Personal email rusik_iron@gmail.com to the system.
-- Please add them ASAP into the system because some villains are trying to register it all the banking services.>
--
-- As you can see, there's been no good story since our system is allowed to add duplicates of contact values.

-- TODO: Add the check that won't allow to add duplicated email or phone number
-- Add constraint that won't allow to add duplicated `contact_value` into `customers_1nf`.
-- hint: https://www.techonthenet.com/postgresql/unique.php







-- DO NOT CHANGE OR DELETE BELOW SCRIPT
-- ====================================================================================================================
-- For denormalized table `customers_normalized_order_with_pk` we can do this via trigger `before`
CREATE OR REPLACE FUNCTION check_duplicate_contacts() RETURNS TRIGGER AS $$
DECLARE
    extracted_emails TEXT[];
    extracted_phones TEXT[];
    email_exists BOOLEAN;
    phone_exists BOOLEAN;
BEGIN
    -- Extract emails using a more compatible pattern
    SELECT ARRAY(
                   SELECT TRIM(SUBSTRING(part FROM '([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,})'))
                   FROM REGEXP_SPLIT_TO_TABLE(NEW.customer_contact, ',') AS part
                   WHERE part LIKE '%@%'
           ) INTO extracted_emails;

    -- Extract phone numbers using compatible pattern
    SELECT ARRAY(
                   SELECT TRIM(REGEXP_REPLACE(
                           SUBSTRING(part FROM '[Pp]hone:?\s*([^,]+)'),
                           '[^0-9]', '', 'g'))
                   FROM REGEXP_SPLIT_TO_TABLE(NEW.customer_contact, ',') AS part
                   WHERE part LIKE '%Phone%' OR part LIKE '%phone%'
           ) INTO extracted_phones;

    -- Check for each email
    IF array_length(extracted_emails, 1) > 0 THEN
        FOR i IN 1..array_length(extracted_emails, 1) LOOP
                -- Skip empty emails
                IF extracted_emails[i] IS NOT NULL AND LENGTH(extracted_emails[i]) > 0 THEN
                    SELECT EXISTS (
                        SELECT 1 FROM customers_normalized_order_with_pk
                        WHERE (NEW.ssn IS NULL OR ssn != NEW.ssn)
                          AND customer_contact LIKE '%' || extracted_emails[i] || '%'
                    ) INTO email_exists;

                    IF email_exists THEN
                        RAISE EXCEPTION 'Email % already exists in the system', extracted_emails[i];
                    END IF;
                END IF;
            END LOOP;
    END IF;

    -- Check for each phone
    IF array_length(extracted_phones, 1) > 0 THEN
        FOR i IN 1..array_length(extracted_phones, 1) LOOP
                -- Ensure we have a valid phone number (at least 10 digits)
                IF extracted_phones[i] IS NOT NULL AND LENGTH(extracted_phones[i]) >= 10 THEN
                    SELECT EXISTS (
                        SELECT 1 FROM customers_normalized_order_with_pk
                        WHERE (NEW.ssn IS NULL OR ssn != NEW.ssn)
                          AND customer_contact LIKE '%' || extracted_phones[i] || '%'
                    ) INTO phone_exists;

                    IF phone_exists THEN
                        RAISE EXCEPTION 'Phone number % already exists in the system', extracted_phones[i];
                    END IF;
                END IF;
            END LOOP;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER prevent_duplicate_contacts
    BEFORE INSERT OR UPDATE ON customers_normalized_order_with_pk
    FOR EACH ROW
EXECUTE FUNCTION check_duplicate_contacts();