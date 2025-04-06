-- Task 3: Same as Task 2, but use DENSE_RANK(). Observe the difference in handling ties compared to RANK().
-- Display customer_id, first_name, last_name, account_count, and the dense rank.

WITH CustomerAccountCounts AS (
    SELECT
        c.customer_id,
        c.first_name,
        c.last_name,
        COUNT(a.account_id) as account_count
    FROM customers c
    JOIN accounts a ON c.customer_id = a.customer_id
    GROUP BY c.customer_id, c.first_name, c.last_name
)
SELECT
    customer_id,
    first_name,
    last_name,
    account_count,
    DENSE_RANK() OVER (ORDER BY account_count DESC) as customer_dense_rank
FROM CustomerAccountCounts;
