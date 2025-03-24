-- check the schema for customers table in 'resources/schema.sql'
-- insert a new value with email 'email@example.com' and notice this value is not unique
-- write insert sql with conflict resolving. Insert another email in this case, use ON CONFLICT
INSERT INTO customers
VALUES (1009, 'Yevhen', 'Yermolenko', 'email@example.com', '911', 'Vinnytsia')
ON CONFLICT (email)
DO UPDATE SET email = 'another@example.com';