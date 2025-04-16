-- DO NOT CHANGE OR DELETE THESE INSERTS, WITH IT WE INITIATE THE DATA FOR TESTING
-- ====================================================================================================================
INSERT INTO customers_normalized_order_with_pk (ssn, customer_name, customer_contact, reliability_score)
VALUES (000000001,
        'Petro Cherpak',
        'Personal Email: petya.cherpachok@gmail.com, Work Email: petro.cherpak@abc.com, Personal Phone: +2708291611',
        'reliable');

INSERT INTO customers_1nf (ssn, customer_name, contact_type, contact_value, reliability_score)
VALUES (000000001, 'Petro Cherpak', 'Personal Email', 'petya.cherpachok@gmail.com', 'reliable'),
       (000000001, 'Petro Cherpak', 'Work Email', 'petro.cherpak@abc.com', 'reliable'),
       (000000001, 'Petro Cherpak', 'Personal Phone', '+2708291611', 'reliable');
-- ====================================================================================================================


-- ====================================================================================================================
-- OPTIONAL TASK 1.4: Write SELECT queries to `customers_normalized_order_with_pk` and `customers_1nf`
-- ====================================================================================================================
-- STORY: You have got the email from <petya.cherpachok@gmail.com> with some questions about his bank account.
-- In the process, you have understood that you need to call the customer for some clarification.
-- Now you need to find the customers phone number.

-- TODO: Find 'Personal Phone' of the 'petya.cherpachok@gmail.com' in the `customers_normalized_order_with_pk` AS personal_phone
-- hint: use SUBSTRING method https://neon.tech/postgresql/postgresql-string-functions/postgresql-substring#extracting-substring-matching-posix-regular-expression
SELECT SUBSTRING(customer_contact FROM 'Personal Phone: ([^,]+)') AS personal_phone
FROM customers_normalized_order_with_pk
WHERE customer_contact like '%petya.cherpachok@gmail.com%';

-- TODO: Find 'Personal Phone' of the 'petya.cherpachok@gmail.com' in the `customers_1nf`
SELECT contact_value
FROM customers_1nf
WHERE ssn = (SELECT ssn FROM customers_1nf WHERE contact_value = 'petya.cherpachok@gmail.com')
  AND contact_type = 'Personal Phone';