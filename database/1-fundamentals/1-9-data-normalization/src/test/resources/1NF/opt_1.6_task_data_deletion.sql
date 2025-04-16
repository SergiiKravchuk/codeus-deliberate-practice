-- DO NOT CHANGE OR DELETE THESE INSERTS, WITH IT WE INITIATE THE DATA FOR TESTING
-- ====================================================================================================================
INSERT INTO customers_normalized_order_with_pk (ssn, customer_name, customer_contact, reliability_score)
VALUES (000000003, 'Ruslan Zalizyaka',
        'Personal Email: rusik_iron@gmail.com, Work Email: ruslan_zalizyaka@abc.com, Personal Phone: 4308441611',
        'reliable');

INSERT INTO customers_1nf (ssn, customer_name, contact_type, contact_value, reliability_score)
VALUES (000000003, 'Ruslan Zalizyaka', 'Personal Email', 'rusik_iron@gmail.com', 'reliable'),
       (000000003, 'Ruslan Zalizyaka', 'Work Email', 'ruslan_zalizyaka@abc.com', 'reliable'),
       (000000003, 'Ruslan Zalizyaka', 'Personal Phone', '4308441611', 'reliable');

-- ====================================================================================================================
-- OPTIONAL TASK 1.6: Write queries to delete 'Work email' from `customers_normalized_order_with_pk` and `customers_1nf`
-- ====================================================================================================================
-- STORY: The 'Ruslan Zalizyaka' came to bank and informed you that his work email is not actual anymore.
-- You need to delete the Work email of the 'Ruslan Zalizyaka'.

-- TODO: DELETE 'Work Email' 'Ruslan Zalizyaka' to ruslan_zalizyaka@codeus.com in `customers_normalized_order_with_pk`
-- hint: use REGEXP_REPLACE method https://neon.tech/postgresql/postgresql-string-functions/regexp_replace


-- TODO: DELETE 'Work Email' 'Ruslan Zalizyaka' to ruslan_zalizyaka@codeus.com in `customers_1nf`
