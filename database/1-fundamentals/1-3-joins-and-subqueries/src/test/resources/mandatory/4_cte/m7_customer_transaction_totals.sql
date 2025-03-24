--Task: Calculate Customer Transaction Totals Using CTE
--Requirements:
--- Use CTE to calculate transaction totals per customer
--- Show customer name and their total transaction amount
--- Basic CTE example
--- Sort by total amount
--
--Expected columns: first_name, last_name, total_amount
WITH customer_totals AS (
    SELECT
        customer_id,
        SUM(amount) as total_amount
    FROM accounts a
    JOIN transactions t ON a.account_id = t.account_id
    GROUP BY customer_id
)
SELECT
    c.first_name,
    c.last_name,
    ct.total_amount
FROM customers c
JOIN customer_totals ct ON c.customer_id = ct.customer_id
ORDER BY ct.total_amount DESC;