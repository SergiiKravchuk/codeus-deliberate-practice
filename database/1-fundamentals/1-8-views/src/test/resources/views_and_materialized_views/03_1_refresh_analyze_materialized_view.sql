------------------------------------------------------------------------------------------------------------------------
-- TASK 3: Refresh and Analyze Performance Improvements of Materialized Views
------------------------------------------------------------------------------------------------------------------------
-- Description: In this task, you will learn how to refresh a materialized view and analyze the
-- performance improvements offered by using materialized views versus executing the original query.
--

-- Recreate the materialized view in case it was dropped
CREATE MATERIALIZED VIEW IF NOT EXISTS transaction_daily_summary AS
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


-- Part 1: Refresh the materialized view we created earlier
-- TODO: Refresh the materialized view:
REFRESH MATERIALIZED VIEW transaction_daily_summary;

-- Part 2: Analyze performance with EXPLAIN ANALYZE
-- Compare the performance of the materialized view versus the original query
--
-- First, analyze the original query:
EXPLAIN ANALYZE
SELECT
    CAST(transaction_date AS DATE) AS transaction_date,
    transaction_type,
    COUNT(*) AS transaction_count,
    SUM(amount) AS total_amount,
    AVG(amount) AS average_amount
FROM transactions
GROUP BY CAST(transaction_date AS DATE), transaction_type
ORDER BY transaction_date, transaction_type;

-- Next, analyze the materialized view query:
EXPLAIN ANALYZE
SELECT * FROM transaction_daily_summary
ORDER BY transaction_date, transaction_type;

-- Part 3: Insert new test transactions to demonstrate view refresh
-- Add two new transactions to the database
INSERT INTO transactions (account_id, transaction_type, amount, transaction_date)
VALUES
    (1, 'deposit', 750.00, CURRENT_TIMESTAMP),
    (5, 'withdrawal', 250.00, CURRENT_TIMESTAMP);

-- Verify that the materialized view does not yet reflect these new transactions
SELECT * FROM transaction_daily_summary
WHERE transaction_date = CURRENT_DATE
ORDER BY transaction_type;

-- TODO: Refresh the materialized view again:
REFRESH MATERIALIZED VIEW transaction_daily_summary;

-- Verify the materialized view now includes the new transactions
SELECT * FROM transaction_daily_summary
WHERE transaction_date = CURRENT_DATE
ORDER BY transaction_type;

-- OBSERVATIONS:
--
-- 1. Performance Differences:
--    - The original query performs multiple expensive operations: GROUP BY, aggregations (COUNT, SUM, AVG),
--      and sorting. The EXPLAIN ANALYZE would typically show multiple steps including sequential scans,
--      aggregation, and sorting operations.
--    - The materialized view query is much simpler - it's just reading pre-computed results from what is
--      essentially a table, often with a simple sequential scan or index scan.
--    - Execution time for the materialized view query is significantly faster, often by orders of magnitude,
--      especially for larger transaction datasets.
--
-- 2. Resource Usage:
--    - The original query requires more CPU for calculation and more memory for grouping operations.
--    - The materialized view query requires less CPU and memory since it's just retrieving stored results.
--    - However, materialized views consume disk space to store their data.
--
-- 3. When to Choose Each Approach:
--    - Use materialized views when:
--      * The underlying data doesn't change frequently
--      * The query is complex and resource-intensive
--      * The same query is executed repeatedly
--      * Query performance is critical (e.g., dashboards, real-time reporting)
--    - Use the original query when:
--      * The underlying data changes very frequently and real-time results are required
--      * Disk space is limited
--      * The query is simple and doesn't benefit much from materialization
--      * The query is rarely executed
--
-- 4. Refresh Considerations:
--    - As demonstrated, materialized views need explicit refreshing to show new data
--    - When data freshness is critical, you must implement an appropriate refresh strategy
--    - The full refresh we performed can be resource-intensive on large datasets