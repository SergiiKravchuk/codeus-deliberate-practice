------------------------------------------------------------------------------------------------------------------------
-- TASK 11: Optimize a Multi-Table Aggregation Query with Indexes
------------------------------------------------------------------------------------------------------------------------
--
-- DESCRIPTION:
-- Analytical query joining customers, accounts, and transactions.
-- Currently suffers from Seq Scans on joins and customer filtering.
--
-- PROBLEM STATEMENT:
-- The query suffers from performance issues due to:
--  1. Missing indexes on foreign key columns used in JOINs.
--  2. Inefficient scans on the `customers` table due to filter conditions on
--    `first_name` and `last_name`.
--
------------------------------------------------------------------------------------------------------------------------
-- STEP 1: Base Query with Filtering by Customer Name
-- Goal: Observe slow performance due to Seq Scans
------------------------------------------------------------------------------------------------------------------------
SELECT
    c.id AS customer_id,
    c.first_name,
    c.last_name,
    COUNT(t.id) AS transaction_count,
    SUM(t.amount) AS total_amount
 FROM customers c
 JOIN accounts a ON c.id = a.customer_id
 JOIN transactions t ON a.id = t.account_id
 WHERE c.first_name in ('John', 'Jane')
  AND c.last_name in ('Johnson', 'Brown')
 GROUP BY c.id, c.first_name, c.last_name
 ORDER BY total_amount DESC;
------------------------------------------------------------------------------------------------------------------------
-- STEP 2:
-- TODO: Create Index on customers (first_name, last_name)
--
-- Index name: idx_customers_name
--
-- Goal: Speed up filtering by customer name
------------------------------------------------------------------------------------------------------------------------
CREATE INDEX idx_customers_name ON customers(first_name, last_name);
------------------------------------------------------------------------------------------------------------------------
-- STEP 3:
-- TODO: Create Index on accounts.customer_id
--
-- Index name: idx_accounts_customer_id
--
-- Goal: Speed up JOIN between customers and accounts
------------------------------------------------------------------------------------------------------------------------
CREATE INDEX idx_accounts_customer_id ON accounts(customer_id);
------------------------------------------------------------------------------------------------------------------------
-- STEP 4:
-- TODO: Create Index on transactions.account_id
--
-- Index name: idx_transactions_account_id
--
-- Goal: Speed up JOIN between accounts and transactions
------------------------------------------------------------------------------------------------------------------------
CREATE INDEX idx_transactions_account_id ON transactions(account_id);
------------------------------------------------------------------------------------------------------------------------
-- STEP 5: Re-run the Query and Check Plan
-- Goal: Validate use of indexes and improved performance
------------------------------------------------------------------------------------------------------------------------
SELECT
    c.id AS customer_id,
    c.first_name,
    c.last_name,
    COUNT(t.id) AS transaction_count,
    SUM(t.amount) AS total_amount
 FROM customers c
 JOIN accounts a ON c.id = a.customer_id
 JOIN transactions t ON a.id = t.account_id
 WHERE c.first_name in ('John', 'Jane')
  AND c.last_name in ('Johnson', 'Brown')
 GROUP BY c.id, c.first_name, c.last_name
 ORDER BY total_amount DESC;
