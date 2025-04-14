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
-- TODO

-- Create a unique index to allow concurrent refreshes (optional)
-- TODO of want ot repeat

-- Part 3: Create a function to refresh the materialized view and update the log
-- TODO
-- Part 4: Insert some test transactions
INSERT INTO transactions (account_id, transaction_type, amount, transaction_date)
VALUES
    (2, 'deposit', 1200.00, CURRENT_TIMESTAMP),
    (3, 'withdrawal', 800.00, CURRENT_TIMESTAMP);

-- Part 5: Call the refresh function
-- TODO
-- Part 6: Check results in the materialized view order by transaction_type;
-- TODO