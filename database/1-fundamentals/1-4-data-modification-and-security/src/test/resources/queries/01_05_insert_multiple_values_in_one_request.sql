-- check the schema for customers table in 'resources/schema.sql'
-- insert three customers using only one INSERT statement
INSERT INTO customers (first_name, last_name, email, phone, address)
VALUES ('Yevhen', 'Yermolenko', 'e@example.com', '911', 'Vinnytsia'),
       ('Taras', 'Shevchenko', 't@e.com', 'n/a', 'Moryntsi'),
       ('Pes', 'Patron', 'pes@e.com', '112', 'Lviv');
