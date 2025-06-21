-- ===================================================================================================
-- TASK 9: Transform Table to Use Multi-Level Partitioning (By Year and Transaction Type)
-- ===================================================================================================
--
-- DESCRIPTION:
-- You currently store transaction data in a single large table (`transactions_raw`) without partitions.
-- Over time, queries filtering by transaction date and type have become slower due to the table's size.
--
-- To improve performance, you will:
-- - Migrate the schema to use multi-level partitioning:
--    - First level: `RANGE` partitioning by `transaction_date` (by year)
--    - Second level: `LIST` subpartitioning by `transaction_type` (e.g., deposit, withdrawal)
-- - Compare execution plans before and after the migration
--
-- GOAL:
-- - Create an unpartitioned table `transactions_raw`
-- - Analyze a query plan
-- - Create a partitioned table `transactions_multilevel`
-- - Migrate data
-- - Compare improved plan with partition pruning
--
-- ===================================================================================================
-- STEP 1: Drop Existing Tables (Cleanup)
-- ===================================================================================================

DROP TABLE IF EXISTS transactions_2023_withdrawal CASCADE;
DROP TABLE IF EXISTS transactions_2023_deposit CASCADE;
DROP TABLE IF EXISTS transactions_2023 CASCADE;
DROP TABLE IF EXISTS transactions_multilevel CASCADE;
DROP TABLE IF EXISTS transactions_raw CASCADE;

-- ===================================================================================================
-- STEP 2: Create Unpartitioned Table: transactions_raw
-- ===================================================================================================

CREATE TABLE transactions_raw (
    id                SERIAL PRIMARY KEY,
    account_id        INT NOT NULL,
    transaction_type  VARCHAR(20) NOT NULL,
    amount            DECIMAL(15,2) NOT NULL,
    transaction_date  TIMESTAMP NOT NULL
);

-- ===================================================================================================
-- STEP 3: Insert Sample Data into transactions_raw
-- ===================================================================================================

INSERT INTO transactions_raw(account_id, transaction_type, amount, transaction_date)
SELECT
    (RANDOM() * 100)::INT,
    CASE WHEN i % 2 = 0 THEN 'deposit' ELSE 'withdrawal' END,
    (RANDOM() * 1000)::NUMERIC(15,2),
    '2023-01-01'::DATE + (i % 365)
FROM generate_series(1, 10000) AS s(i);
commit;
-- ===================================================================================================
-- TODO STEP 4: Create Partitioned Table: transactions_multilevel (Range on transaction_date)
-- ===================================================================================================


-- ===================================================================================================
-- TODO STEP 5: Create First-Level Partition: transactions_2023 (Range for 2023)
-- ===================================================================================================


-- ===================================================================================================
-- TODO STEP 6: Create Subpartitions for transaction_type (LIST Partitioning)
-- - transactions_2023_deposit     for 'deposit'
-- - transactions_2023_withdrawal  for 'withdrawal'
-- ===================================================================================================


-- ===================================================================================================
-- TODO STEP 7: Copy Data from transactions_raw → transactions_multilevel
-- ===================================================================================================


-- ===================================================================================================
-- STEP 8: You're ready to compare execution plans now
-- Use the following queries in EXPLAIN ANALYZE (see JUnit test):
--   - SELECT from transactions_raw (non-partitioned)
--   - SELECT from transactions_multilevel (partitioned)
-- ===================================================================================================
