-- check the schema for customers table in 'resources/schema.sql'
-- check the test-data for customers table in 'resources/test-data.sql'
-- delete the row with non-existed id and notice there are no fails
DELETE FROM customers WHERE id = 87628;