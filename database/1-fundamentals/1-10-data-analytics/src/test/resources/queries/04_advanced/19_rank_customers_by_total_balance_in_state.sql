-- Task 19 (Optional): Rank customers within each state based on their total balance across all their accounts.
-- Display state, customer_id, first_name, last_name, total_balance, rank_in_state.

WITH CustomerTotalBalance AS (
    -- Calculate total balance across all accounts for each customer
    SELECT
        c.customer_id,
        c.first_name,
        c.last_name,
        SUM(a.balance) as total_balance
    FROM customers c
    JOIN accounts a ON c.customer_id = a.customer_id
    GROUP BY c.customer_id, c.first_name, c.last_name
),
CustomerStates AS (
    -- Determine the distinct states each customer has accounts in
    SELECT DISTINCT
        a.customer_id,
        b.state
    FROM accounts a
    JOIN branches b ON a.branch_id = b.branch_id
)
-- Join the total balance with the states and apply ranking
SELECT
    cs.state,
    ctb.customer_id,
    ctb.first_name,
    ctb.last_name,
    ctb.total_balance, -- Using the customer's overall total balance for ranking within the state
    RANK() OVER (PARTITION BY cs.state ORDER BY ctb.total_balance DESC) as rank_in_state
FROM CustomerTotalBalance ctb
JOIN CustomerStates cs ON ctb.customer_id = cs.customer_id;
