-- check the schema for customers table in 'resources/schema.sql'
-- insert a new value using columns names in a query explicitly
INSERT INTO customers (first_name, last_name, email, phone, address)
VALUES ('Yevhen', 'Yermolenko', 'email@e.com', '911', 'Vinnytsia');