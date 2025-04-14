-- TASK 4: Implement an Incremental Refresh Strategy for Transaction Data Marts (using MATERIALIZED VIEW + REFRESH)

-- Part 1: Create a tracking table to monitor last refresh time
CREATE TABLE IF NOT EXISTS materialized_view_refresh_log (
    view_name VARCHAR(100) PRIMARY KEY,
    last_refresh_timestamp TIMESTAMP NOT NULL
);

-- Initialize or update the refresh log for our transaction summary view
INSERT INTO materialized_view_refresh_log (view_name, last_refresh_timestamp)
VALUES ('transaction_daily_summary_advanced', '2000-01-01 00:00:00'::TIMESTAMP)
ON CONFLICT (view_name)
DO UPDATE SET last_refresh_timestamp = '2000-01-01 00:00:00'::TIMESTAMP;

-- Part 2: Create the materialized view (for daily aggregated transactions) ORDER BY transaction_date, transaction_type;
CREATE MATERIALIZED VIEW IF NOT EXISTS transaction_daily_summary_advanced AS
SELECT
    CAST(transaction_date AS DATE) AS transaction_date,
    transaction_type,
    COUNT(*) AS transaction_count,
    SUM(amount) AS total_amount,
    AVG(amount) AS average_amount
FROM
    transactions
GROUP BY
    CAST(transaction_date AS DATE),
    transaction_type
ORDER BY
    transaction_date,
    transaction_type;

-- Create a unique index to allow concurrent refreshes (optional)
CREATE UNIQUE INDEX IF NOT EXISTS idx_transaction_daily_summary_advanced
    ON transaction_daily_summary_advanced (transaction_date, transaction_type);

-- Part 3: Create a function to refresh the materialized view and update the log
CREATE OR REPLACE FUNCTION refresh_transaction_daily_summary()
RETURNS VOID AS $$
DECLARE
    current_refresh TIMESTAMP := NOW();
BEGIN
    -- Refresh the materialized view
    REFRESH MATERIALIZED VIEW CONCURRENTLY transaction_daily_summary_advanced;

    -- Update the refresh timestamp in the log
    UPDATE materialized_view_refresh_log
    SET last_refresh_timestamp = current_refresh
    WHERE view_name = 'transaction_daily_summary_advanced';
END;
$$ LANGUAGE plpgsql;

-- Part 4: Insert some test transactions
INSERT INTO transactions (account_id, transaction_type, amount, transaction_date)
VALUES
    (2, 'deposit', 1200.00, CURRENT_TIMESTAMP),
    (3, 'withdrawal', 800.00, CURRENT_TIMESTAMP);

-- Part 5: Call the refresh function
SELECT refresh_transaction_daily_summary();

-- Part 6: Check results in the materialized view order by transaction_type;
SELECT * FROM transaction_daily_summary_advanced
WHERE transaction_date = CURRENT_DATE
ORDER BY transaction_type;

-- OBSERVATION:
-- This approach maintains correctness and performance while ensuring that materialized view data is current.