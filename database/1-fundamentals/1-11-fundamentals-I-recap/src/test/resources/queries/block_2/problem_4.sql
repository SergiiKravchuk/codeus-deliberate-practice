-- ===================================================================================================
-- PROBLEM 4
-- ===================================================================================================
--Description:
-- The following query joins the accounts and transactions tables to produce, for each account,
-- the total number of transactions and the sum of amounts over the last 30 days.
--
--TODO:
-- Analyze the query plan and optimize it for frequent read access.
--
--Query:
-- SELECT
--     a.id            AS account_id,
--     a.account_type,
--     COUNT(t.id)     AS transaction_count,
--     SUM(t.amount)   AS total_amount
-- FROM
--     accounts a
-- JOIN
--     transactions t
--     ON a.id = t.account_id
-- WHERE
--     t.transaction_date > NOW() - INTERVAL '30 days'
-- GROUP BY
--     a.id, a.account_type;
-- ===================================================================================================
-- WORKING AREA
CREATE MATERIALIZED VIEW mv_account_monthly_summary AS
SELECT
    a.id            AS account_id,
    a.account_type,
    COUNT(t.id)     AS transaction_count,
    SUM(t.amount)   AS total_amount
FROM
    accounts a
JOIN
    transactions t
    ON a.id = t.account_id
WHERE
    t.transaction_date > NOW() - INTERVAL '30 days'
GROUP BY
    a.id, a.account_type;