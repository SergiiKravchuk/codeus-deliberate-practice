-- Task 16 (Optional): Divide all accounts into 4 quartiles based on their balance.
-- Display account_id, balance, balance_quartile.

SELECT
    account_id,
    balance,
    NTILE(4) OVER (ORDER BY balance ASC) as balance_quartile
FROM accounts;
