-- check the schema for customers table in 'resources/schema.sql'
-- insert a new value and return first_name and last_name
INSERT INTO customers
VALUES (1009, 'Yevhen', 'Yermolenko', 'e@example.com', '911', 'Vinnytsia')
RETURNING first_name, last_name;