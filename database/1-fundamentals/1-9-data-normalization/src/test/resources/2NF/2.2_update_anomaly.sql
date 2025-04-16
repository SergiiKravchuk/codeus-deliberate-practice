-- ====================================================================================================================
-- TASK 2.2: Update `reliability_score` of 'Ivan Kozhumyaka' to 'most reliable' by Personal Phone
-- ====================================================================================================================
-- STORY: 'Ivan Kozhumyaka' came to the bank and provides the paper that shows that he's got a new job and according to
-- this paper we can change his `reliability_score` to 'most reliable'.
-- Nowadays, we don't need to provide our SSN everywhere. Usually the phone number is enough for authentication.
-- The Personal Phone number of the 'Ivan Kozhumyaka' is '+380931111111'

-- TODO:  Write the query that will update the `reliability_score` of Ivan Kozhumyaka to 'most reliable'
-- Table: `customers_1nf`
-- `customer_name`: 'Ivan Kozhumyaka'
-- `contact_type`: 'Personal Phone'
-- `contact_value`: '+380931111111'
-- `reliability_score` should be set to the 'most reliable'
update customers_1nf set reliability_score = 'most reliable'
                     where customer_name = 'Ivan Kozhumyaka'
                       and contact_type = 'Personal Phone'
                       and contact_value = '+380931111111';

-- TODO:  Write the query that will select all the data of Ivan Kozhumyaka to check if was updated
select * from customers_1nf where customer_name = 'Ivan Kozhumyaka';