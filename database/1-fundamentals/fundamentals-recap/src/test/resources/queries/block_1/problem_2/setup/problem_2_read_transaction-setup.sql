BEGIN;

--SOLUTION

INSERT INTO isolation_level_test (account_id, first_read, second_read)
    SELECT
        fr.account_id,
        fr.balance,
        sr.balance
    FROM
        first_read fr
            JOIN
    second_read sr ON fr.account_id = sr.account_id;
COMMIT;