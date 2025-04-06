-- Task 4: Rank accounts within each branch based on their current balance in descending order (highest balance gets rank 1).
-- Display branch_id, account_id, balance, and the rank using RANK().

SELECT
    branch_id,
    account_id,
    balance,
    RANK() OVER (PARTITION BY branch_id ORDER BY balance DESC) as rank_in_branch
FROM accounts;
