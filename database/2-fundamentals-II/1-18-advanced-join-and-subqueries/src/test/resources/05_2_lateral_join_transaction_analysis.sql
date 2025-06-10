-- Task 5.2: Transaction Pattern Analysis
-- Analyze transaction patterns for each customer using LATERAL
SELECT
    c.id AS customer_id,
    CONCAT(c.first_name, ' ', c.last_name) AS customer_name,
    trans_summary.account_count,
    trans_summary.total_transactions,
    trans_summary.avg_transaction_amount,
    trans_summary.last_transaction_date,
    trans_summary.transaction_frequency_days,
    CASE
        WHEN trans_summary.transaction_frequency_days <= 10 THEN 'HIGH_FREQUENCY'
        WHEN trans_summary.transaction_frequency_days <= 30 THEN 'MEDIUM_FREQUENCY'
        WHEN trans_summary.transaction_frequency_days <= 90 THEN 'LOW_FREQUENCY'
        ELSE 'VERY_LOW_FREQUENCY'
    END AS transaction_behavior
FROM customers c
LEFT JOIN LATERAL (
    SELECT
        COUNT(DISTINCT a.id) AS account_count,
        COUNT(t.id) AS total_transactions,
        ROUND(AVG(t.amount), 2) AS avg_transaction_amount,
        MAX(t.transaction_date) AS last_transaction_date,
        CASE
            WHEN COUNT(t.id) > 1 THEN
                EXTRACT(DAY FROM (MAX(t.transaction_date) - MIN(t.transaction_date))) / COUNT(t.id)
            ELSE NULL
        END AS transaction_frequency_days
    FROM accounts a
    LEFT JOIN transactions t ON a.id = t.account_id
    WHERE a.customer_id = c.id
) trans_summary ON true
ORDER BY trans_summary.total_transactions DESC;