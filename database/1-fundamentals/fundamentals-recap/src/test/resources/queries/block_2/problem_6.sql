-- ===================================================================================================
-- PROBLEM 6
-- ===================================================================================================
--Description:
-- A query frequently selects recent accounts created in the last 10 days by account_type.
-- An index exists on account_type, but the performance can be better for frequent access and fetching of multiple fields.
--
--TODO:
-- Analyze and optimize the query to gain maximum performance.
--
--Query:
-- SELECT id, balance, created_at
-- FROM accounts
-- WHERE account_type = 'checking'
--   AND created_at > NOW() - INTERVAL '10 days';
-- ===================================================================================================
-- WORKING AREA
CREATE INDEX idx_accounts_type_date_cover ON accounts(account_type, created_at) INCLUDE (id, balance);

