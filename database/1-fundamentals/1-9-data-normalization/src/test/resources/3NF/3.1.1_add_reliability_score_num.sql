-- ====================================================================================================================
-- TASK 3.1.1: Refactor the `customers`, add numerical representation of the `reliability_score`
-- ====================================================================================================================
-- STORY: The bank agents want now to see not only the `reliability_score` as 'reliable' or 'not reliable'. They want
-- to see some scores, like:
-- ---------------------------------------------------------------------------
-- | ssn       | customer_name   | reliability_score | reliability_score_num |
-- |-----------|-----------------|-------------------|-----------------------|
-- | 100000001 | Petro Cherpak   | vip               | 13                    |
-- | 200000002 | Ivan Kozhumyaka | reliable          | 7                     |
-- ---------------------------------------------------------------------------
-- They said it will help them twith some 'risk management'.
-- The `reliability_score_num` will have such representation:
-- 1-2 - 'not reliable'
-- 2-5 - 'probably reliable'
-- 6-9 - 'reliable'
-- 10-12 - 'most reliable'
-- 13-14 - 'vip'


-- TODO: Write the query to add the new column:
-- table: `customers`
-- column name: `reliability_score_num` SMALLINT
-- hint: https://www.w3schools.com/postgresql/postgresql_add_column.php
ALTER TABLE customers
    ADD COLUMN reliability_score_num SMALLINT;

-- TODO: Add query that will assign all the `reliability_score_num` in relation with `reliability_score`
-- if 'vip' then 13
-- if 'most reliable' then 10
-- if 'reliable' then 7
-- if 'probably reliable' then 3
-- if 'not reliable' then 1
-- hint: https://www.ibm.com/docs/en/informix-servers/12.10.0?topic=rows-case-expression-update-column
UPDATE customers
SET reliability_score_num =
        CASE reliability_score
            WHEN 'vip' THEN 13
            WHEN 'most reliable' THEN 10
            WHEN 'reliable' THEN 7
            WHEN 'probably reliable' THEN 3
            WHEN 'not reliable' THEN 1
            END;

-- TODO: Alter the `reliability_score_num`, add the NOT NULL constraint
-- jint: https://www.tutorialsteacher.com/postgresql/add-constraint
ALTER TABLE customers
    ALTER COLUMN reliability_score_num SET NOT NULL;