-- Task 2: Rank customers based on the number of accounts they hold. Customers with more accounts should have a lower rank (rank 1).
-- Display customer_id, first_name, last_name, account_count, and the rank. Use RANK(). Handle ties appropriately.

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
    RANK() OVER (ORDER BY account_count DESC) as customer_rank
FROM CustomerAccountCounts;
