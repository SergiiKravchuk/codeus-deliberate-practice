BEGIN;

--SOLUTION

INSERT INTO isolation_level_test (first_read, second_read)
SELECT (SELECT balance FROM first_read),
       (SELECT balance FROM second_read);
COMMIT;