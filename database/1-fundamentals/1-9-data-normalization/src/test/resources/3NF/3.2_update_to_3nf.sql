-- ====================================================================================================================
-- TASK 3.2: Update `customers` table to 3NF
-- ====================================================================================================================
-- RULE: Every NON-KEY attribute in a table should depend on the key, the WHOLE key, and NOTHING but the key + be in 2NF!
--
-- What the dependencies we should have:
-- `ssn` -> `customer_name`
-- `ssn` -> `reliability_score_num`
-- `reliability_score_num` -> `reliability_score`
-- To achieve this, we need to move the `reliability_score_num` and `reliability_score` to a new table.

-- TODO: Create a new the table:
-- name: `reliability_scores`
-- columns:
-- - `reliability_score_num`, SMALLINT, PRIMARY KEY
-- - `reliability_score`, VARCHAR(30), NOT NULL


-- TODO: Fulfill the `reliability_scores` with required data:
-- (1, 'not reliable'),
-- (2, 'not reliable'),
-- (3, 'probably reliable'),
-- (4, 'probably reliable'),
-- (5, 'probably reliable'),
-- (6, 'reliable'),
-- (7, 'reliable'),
-- (8, 'reliable'),
-- (9, 'reliable'),
-- (10, 'most reliable'),
-- (11, 'most reliable'),
-- (12, 'most reliable'),
-- (13, 'vip'),
-- (14, 'vip');



-- TODO: Add FK to `reliability_scores`
-- table: `customers`
-- FK name: `fk_reliability_scores`
-- `reliability_score_num` -> `reliability_scores.reliability_score_num`
-- hint: https://tableplus.com/blog/2018/08/postgresql-how-to-add-a-foreign-key.html



-- TODO: Drop the `reliability_score` column in the `customers`

