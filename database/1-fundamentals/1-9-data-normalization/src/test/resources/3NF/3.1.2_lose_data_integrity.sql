-- ====================================================================================================================
-- TASK 3.1.2: Update the `reliability_score_num` of Ivan Kozhumyaka
-- ====================================================================================================================
-- STORY: 'Ivan Kozhumyaka' came to the bank and provides the paper that shows that he's got a new job and according to
-- this paper we have measured and now can change his `reliability_score_num` from '7' to '10'.
-- ---------------------------------------------------------------------------
-- | ssn       | customer_name   | reliability_score | reliability_score_num |
-- |-----------|-----------------|-------------------|-----------------------|
-- | 200000002 | Ivan Kozhumyaka | reliable          | 7                     |
-- ---------------------------------------------------------------------------

-- TODO: Write the query that will update `reliability_score_num` from 7 to 10 for 'Ivan Kozhumyaka' with ssn 200000002
UPDATE customers
SET reliability_score_num = 10
WHERE ssn = 200000002;
