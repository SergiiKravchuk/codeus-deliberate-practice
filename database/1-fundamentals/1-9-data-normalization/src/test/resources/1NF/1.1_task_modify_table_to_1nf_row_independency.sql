-- DO NOT DELETE THIS TABLE!
CREATE TABLE customers_denormalized_order
(
    customer_name    VARCHAR(100) NOT NULL,
    customer_contact VARCHAR(500)
);

-- ====================================================================================================================
-- TASK 1.1: Create a table that doesn't depend on the ROW order for reliability_score
-- ====================================================================================================================
-- STORY: Petro Cherpak came to the bank and wants to open the checking account. To do so, the bank manager firstly needs
-- to check the `reliability_score` of him. A bank manager loads all the customers from the table and checks these points:
-- If customer in first 10% of customers in the list - its 'vip' (score 100)
-- In ranges 10%-30% - its 'most reliable' (score 80)
-- In ranges 30%-60% - its 'reliable' (score 60)
-- In ranges 60%-80%  - its 'probably reliable' (score 30)
-- In ranges 80%-100% - its 'not reliable' (score 10)
--
-- Absurdly wrong measurement, you would say (if we sort it, or by some purpose the DB returns it in some another order).
--
-- PROBLEM: In the denormalized approach, reliability_score depends on row order which is not guaranteed by SQL without ORDER BY.
-- This violates 1NF which requires that the order of rows is insignificant.

-- TODO:
-- Create a new table, that will have the reliability characteristic of customers:
-- ['vip', 'most reliable', 'reliable', 'probably reliable', 'not reliable']
-- The table name:
-- `customers_normalized_order`
-- new field: `reliability_score`: varchar (0) and it is not null
-- don't forget to add check that `reliability_score` has such values ['vip', 'most reliable', 'reliable', 'probably reliable', 'not reliable']

