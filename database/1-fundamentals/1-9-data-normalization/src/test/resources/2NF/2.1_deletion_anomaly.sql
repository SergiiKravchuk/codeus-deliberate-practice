-- ====================================================================================================================
-- TASK 2.1: Delete the `reliability_score` for 'Petro Cherpak'
-- ====================================================================================================================
-- STORY: Petro Cherpak came to the bank with requirement to delete his `reliability_score` in the system.
-- Firstly, we thought to tell him 'suck the butt', but after `select * from customers_1nf where ssn = 000000001` we see:

-- | ssn       | customer_name | contact_type   | contact_value | reliability_score |
-- |-----------|---------------|----------------|---------------|-------------------|
-- | 100000001 | Petro Cherpak | Personal Phone | +2708291611   | vip               |

-- VIP... Okay. In such a case, we need to delete it.


-- TODO:  Write the query that will delete the `reliability_score` of Petro Cherpak

-- TODO:  Write the query that will select all the data of Petro Cherpak to check if was updated


-- Next instructions will be provided when you run the test!