------------------------------------------------------------------------------------------------------------------------
-- TASK 2: Create a Materialized View for Transaction Summaries
------------------------------------------------------------------------------------------------------------------------
-- Description: Create a materialized view named 'transaction_daily_summary' that summarizes transaction
-- data by date and type. This will be useful for reporting and analytics purposes without having to
-- recalculate the summaries each time.
--
-- The materialized view should include:
-- - Transaction date (just the date part, not time)
-- - Transaction type (deposit, withdrawal, transfer)
-- - Total number of transactions for that date and type
-- - Total amount of transactions for that date and type
-- - Average transaction amount for that date and type
--
-- View Name: transaction_daily_summary
--
-- Base this materialized view on the transactions table.
-- Group by transaction date (cast to DATE) and transaction type.
--
-- Query to test the view:
-- SELECT * FROM transaction_daily_summary ORDER BY transaction_date, transaction_type;
--
-- TODO: Create Materialized View:

CREATE MATERIALIZED VIEW transaction_daily_summary AS
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

-- EXPLANATION:
-- This SQL creates a materialized view that summarizes transaction data by date and type.
--
-- Key differences between regular views and materialized views:
-- 1. Regular views are virtual tables that execute their query every time they're accessed.
-- 2. Materialized views store the query results physically, like a table, improving performance.
--
-- Components of the query:
-- - CAST(transaction_date AS DATE): Extracts just the date part from the timestamp.
-- - COUNT(*): Counts the number of transactions for each date and type.
-- - SUM(amount): Calculates the total transaction amount for each date and type.
-- - AVG(amount): Calculates the average transaction amount for each date and type.
-- - GROUP BY: Groups the results by date and transaction type.
-- - ORDER BY: Orders the results by date and transaction type.
--
-- Benefits of using a materialized view for this query:
-- 1. Performance: Aggregation operations (COUNT, SUM, AVG) can be expensive, especially on large tables.
--    The materialized view computes these once and stores the results.
-- 2. Reporting: Transaction summaries are common in financial reporting and dashboards.
-- 3. Reduced load: Repeated execution of complex aggregation queries can put load on the database.
--
-- Note: Materialized views need to be refreshed manually or on a schedule to incorporate new data.