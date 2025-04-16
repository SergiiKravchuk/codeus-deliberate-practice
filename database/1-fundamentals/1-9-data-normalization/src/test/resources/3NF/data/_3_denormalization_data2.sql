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

DROP TABLE IF EXISTS reliability_scores;

CREATE TABLE reliability_scores
(
    reliability_score_num SMALLINT PRIMARY KEY,
    reliability_score     VARCHAR(30) NOT NULL
);
INSERT INTO reliability_scores (reliability_score_num, reliability_score)
VALUES (1, 'not reliable'),
       (2, 'not reliable'),
       (3, 'probably reliable'),
       (4, 'probably reliable'),
       (5, 'probably reliable'),
       (6, 'reliable'),
       (7, 'reliable'),
       (8, 'reliable'),
       (9, 'reliable'),
       (10, 'most reliable'),
       (11, 'most reliable'),
       (12, 'most reliable'),
       (13, 'vip'),
       (14, 'vip');
ALTER TABLE customers
    ADD CONSTRAINT fk_reliability_scores
        FOREIGN KEY (reliability_score_num) REFERENCES reliability_scores(reliability_score_num);
ALTER TABLE customers
    DROP COLUMN reliability_score;