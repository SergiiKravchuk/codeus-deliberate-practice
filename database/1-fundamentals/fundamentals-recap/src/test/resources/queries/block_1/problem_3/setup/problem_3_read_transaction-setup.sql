BEGIN;

--SOLUTION

INSERT INTO isolation_level_test (first_read, second_read)
SELECT (SELECT COUNT(*) FROM first_read),
       (SELECT COUNT(*) FROM second_read);

COMMIT;