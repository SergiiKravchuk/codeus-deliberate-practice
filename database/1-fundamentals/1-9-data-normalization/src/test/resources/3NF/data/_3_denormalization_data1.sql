--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--
--

ALTER TABLE customers
    ADD COLUMN reliability_score_num SMALLINT;

UPDATE customers
SET reliability_score_num =
        CASE reliability_score
            WHEN 'vip' THEN 13
            WHEN 'most reliable' THEN 10
            WHEN 'reliable' THEN 7
            WHEN 'probably reliable' THEN 3
            WHEN 'not reliable' THEN 1
            END;

ALTER TABLE customers
    ALTER COLUMN reliability_score_num SET NOT NULL;