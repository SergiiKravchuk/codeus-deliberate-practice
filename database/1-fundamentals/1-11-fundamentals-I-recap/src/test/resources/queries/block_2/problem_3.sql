-- ===================================================================================================
-- PROBLEM 3
-- ===================================================================================================
--Description:
-- Your team regularly runs the following query to audit recent transactions.

--TODO:
-- Analyze the query and suggest an optimization that handles both filtering conditions efficiently.
--
--Query:
-- SELECT account_id, amount, transaction_date
-- FROM transactions
-- WHERE amount > 200 AND transaction_date > NOW() - INTERVAL '15 days';
-- ===================================================================================================
-- WORKING AREA