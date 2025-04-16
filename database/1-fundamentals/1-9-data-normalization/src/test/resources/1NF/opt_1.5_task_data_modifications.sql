-- DO NOT CHANGE OR DELETE THESE INSERTS, WITH IT WE INITIATE THE DATA FOR TESTING
-- ====================================================================================================================
INSERT INTO customers_normalized_order_with_pk (ssn, customer_name, customer_contact, reliability_score)
VALUES (000000002,
        'Ivan Kozhumyaka',
        'Personal Email: vanyook@gmail.com, Work Email: ivan_kozhumyaka@abc.com, Personal Phone: +4308291331',
        'reliable');

INSERT INTO customers_1nf (ssn, customer_name, contact_type, contact_value, reliability_score)
VALUES (000000002, 'Ivan Kozhumyaka', 'Personal Email', 'vanyook@gmail.com', 'reliable'),
       (000000002, 'Ivan Kozhumyaka', 'Work Email', 'ivan_kozhumyaka@abc.com', 'reliable'),
       (000000002, 'Ivan Kozhumyaka', 'Personal Phone', '+4308291331', 'reliable');

-- ====================================================================================================================
-- OPTIONAL TASK 1.5: Write UPDATE queries to `customers_normalized_order_with_pk` and `customers_1nf`
-- ====================================================================================================================
-- STORY: You have got the email from <vanyook@gmail.com> with information that he has changed the company and now wants
-- to add his new work email to profile. The new work email is 'ivan_kozhumyaka@codeus.com' (old is ivan_kozhumyaka@abc.com)

-- TODO: Update 'Work Email' of 'vanyook@gmail.com' to 'ivan_kozhumyaka@codeus.com' in `customers_normalized_order_with_pk`
-- hint: use REGEXP_REPLACE method https://neon.tech/postgresql/postgresql-string-functions/regexp_replace


-- TODO: Update 'Work Email' of 'vanyook@gmail.com' to 'ivan_kozhumyaka@codeus.com' in `customers_1nf`
