-- Task 17 (Optional): For each branch, find the account with the highest balance using FIRST_VALUE.
-- Display branch_id, branch_name, account_id_highest_balance, highest_balance. Hint: Use a subquery or CTE.

WITH RankedAccounts AS (
    SELECT
        a.branch_id,
        b.branch_name,
        a.account_id,
        a.balance,
        FIRST_VALUE(a.account_id) OVER (PARTITION BY a.branch_id ORDER BY a.balance DESC ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) as account_id_highest_balance,
        FIRST_VALUE(a.balance) OVER (PARTITION BY a.branch_id ORDER BY a.balance DESC ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) as highest_balance,
        ROW_NUMBER() OVER (PARTITION BY a.branch_id ORDER BY a.balance DESC) as rn -- To select only one row per branch later
    FROM accounts a
    JOIN branches b ON a.branch_id = b.branch_id
)
SELECT
    branch_id,
    branch_name,
    account_id_highest_balance,
    highest_balance
FROM RankedAccounts
WHERE rn = 1;
