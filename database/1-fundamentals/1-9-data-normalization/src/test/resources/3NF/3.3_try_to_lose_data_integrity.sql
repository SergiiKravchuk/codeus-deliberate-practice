-- ====================================================================================================================
-- TASK 3.3: Update the `reliability_score_num` of Ivan Kozhumyaka
-- ====================================================================================================================
-- STORY: Story is the same 'Ivan Kozhumyaka' came to the bank and provides the paper that shows that he's got a new
-- job and according to his paper we have measured and now can change his `reliability_score_num` from '7' to '10'.
-- ---------------------------------------------------------------------------
-- | ssn       | customer_name   | reliability_score_num |
-- |-----------|-----------------|-----------------------|
-- | 200000002 | Ivan Kozhumyaka | 7                     |
-- ---------------------------------------------------------------------------
-- ---------------------------------------------
-- | reliability_score_num | reliability_score |
-- |-----------------------|-------------------|
-- | 7                     | reliable          |
-- ---------------------------------------------

-- TODO: Write the query that will update `reliability_score_num` from 7 to 10 for 'Ivan Kozhumyaka' with ssn 200000002
UPDATE customers
SET reliability_score_num = 10
WHERE ssn = 200000002;

-- TODO: Write the query that will select the data from columns below of 'Ivan Kozhumyaka' with ssn 200000002:
-- `ssn`
-- `customer_name`
-- `reliability_score_num`
-- `reliability_score`
SELECT c.ssn, c.customer_name, c.reliability_score_num, rs.reliability_score
FROM customers c
INNER JOIN reliability_scores rs ON c.reliability_score_num = rs.reliability_score_num
WHERE c.ssn = 200000002;

