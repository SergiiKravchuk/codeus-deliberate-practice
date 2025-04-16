INSERT INTO customers (ssn, customer_name, reliability_score)
SELECT DISTINCT ssn, customer_name, reliability_score
FROM customers_1nf;

-- Then, insert contact information into the contacts table
INSERT INTO customer_contacts (ssn, contact_type, contact_value)
SELECT ssn, contact_type, contact_value
FROM customers_1nf;