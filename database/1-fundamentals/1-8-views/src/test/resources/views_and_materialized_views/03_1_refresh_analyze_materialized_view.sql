------------------------------------------------------------------------------------------------------------------------
-- TASK 3: Refresh and Analyze Performance Improvements of Materialized Views
------------------------------------------------------------------------------------------------------------------------
-- Description: In this task, you will learn how to refresh a materialized view and analyze the
-- performance improvements offered by using materialized views versus executing the original query.
--

-- Recreate the materialized view in case it was dropped (already did in task 2)
-- TODO

-- Part 1: Refresh the materialized view we created earlier
-- TODO: Refresh the materialized view:


-- Part 2: Analyze performance with EXPLAIN ANALYZE
-- Compare the performance of the materialized view versus the original query
-- TODO: Firstly, analyze the original query:


-- TODO: Next, analyze the materialized view query:


-- Part 3: Insert new test transactions to demonstrate view refresh
-- TODO: Add two new transactions to the database


-- TODO: Verify that the materialized view does not yet reflect these new transactions


-- TODO: Refresh the materialized view again:


-- TODO: Verify the materialized view now includes the new transactions





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