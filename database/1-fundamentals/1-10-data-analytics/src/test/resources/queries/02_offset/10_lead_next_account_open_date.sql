-- Task 10: For each customer, find the date of their first account opening and the date of their next account opening (if any).
-- Display customer_id, account_id, open_date, next_account_open_date. Order by customer_id, open_date.

SELECT
    customer_id,
    account_id,
    open_date,
    LEAD(open_date) OVER (PARTITION BY customer_id ORDER BY open_date ASC, account_id ASC) as next_account_open_date -- Added account_id for tie-breaking
FROM accounts
ORDER BY customer_id, open_date;
