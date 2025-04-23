-- ===================================================================================================
-- PROBLEM 2
-- ===================================================================================================
--Description:
-- Multiple parts of the application fetch customer details together with their total account balance.
-- Each time, a join and aggregation are performed, causing repetition and performance impact.
--
--TODO:
-- Propose a clean and performant way to simplify repeated access.
--
--Query:
-- SELECT c.id           AS customer_id,
--        c.first_name,
--        c.last_name,
--        SUM(a.balance) AS total_balance
-- FROM customers c
-- JOIN accounts a ON c.id = a.customer_id
-- GROUP BY c.id, c.first_name, c.last_name
-- HAVING SUM(a.balance) > 2000;
-- ===================================================================================================
-- WORKING AREA
CREATE VIEW v_customer_total_balances AS
SELECT c.id           AS customer_id,
       c.first_name,
       c.last_name,
       SUM(a.balance) AS total_balance
FROM customers c
JOIN accounts a ON c.id = a.customer_id
GROUP BY c.id, c.first_name, c.last_name
HAVING SUM(a.balance) > 2000;

