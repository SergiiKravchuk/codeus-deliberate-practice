-- ============================================
-- Exercise 03: Logical Replication
-- Setup and Configuration Queries
-- ============================================

-- ============================================
-- Task 1: Verify Starting State
-- ============================================

-- Query 1.1a: Check if publications exist (Run on Master - Port 5435)
-- If this returns no rows, publications weren't created properly
SELECT pubname
FROM pg_publication;

-- Query 1.1b: Create publications if missing (Run on Master - Port 5435 ONLY if 1.1a returned no rows)
-- Create the publications manually if they don't exist
CREATE PUBLICATION lab_publication FOR TABLE users, orders;
CREATE PUBLICATION products_publication FOR TABLE products;

-- Query 1.1c: Verify publications created (Run on Master - Port 5435)
-- Expected: lab_publication, products_publication
-- All operation types should be 'true'
SELECT pubname      as publication_name,
       puballtables as includes_all_tables,
       pubinsert    as replicates_insert,
       pubupdate    as replicates_update,
       pubdelete    as replicates_delete
FROM pg_publication;

-- Query 1.2: Verify subscriber is empty (Run on Logical Subscriber - Port 5437)
-- Expected: All tables showing 0 records (or possibly some records from previous exercises)
-- Key point: logical replication hasn't started yet because no subscriptions exist
SELECT 'users' as table_name, COUNT(*) as records
FROM users
UNION ALL
SELECT 'orders' as table_name, COUNT(*) as records
FROM orders
UNION ALL
SELECT 'products' as table_name, COUNT(*) as records
FROM products
ORDER BY table_name;

-- ============================================
-- Task 2: Create First Subscription
-- ============================================

-- Query 2.1: Create subscription for users and orders (Run on Logical Subscriber - Port 5437)
-- Expected: "NOTICE: created replication slot..." and "CREATE SUBSCRIPTION"
-- copy_data=false prevents primary key conflicts with existing data from previous exercises
CREATE SUBSCRIPTION lab_subscription
    CONNECTION 'host=postgres-master port=5432 user=postgres dbname=replication_lab password=password'
    PUBLICATION lab_publication
    WITH (copy_data = false);

-- Query 2.2: Check subscription status (Run on Logical Subscriber - Port 5437)
-- Expected: Initially 'Data is being copied', then 'Ready (normal replication)'
-- Example result:
--   lab_subscription,orders,Ready (normal replication)
--   lab_subscription,users,Data is being copied
-- Explanation: Tables sync independently and may complete at different times.
-- This depends on table structure, indexes, and current database load.
-- PostgreSQL processes each table's initial copy as separate operation.
-- Wait 30 seconds and run again - both should show 'Ready (normal replication)'
SELECT subname as subscription_name,
       relname as table_name,
       CASE srsubstate
           WHEN 'd' THEN 'Data is being copied'
           WHEN 's' THEN 'Synchronized'
           WHEN 'r' THEN 'Ready (normal replication)'
           ELSE 'Unknown'
           END as sync_status
FROM pg_subscription_rel psr
         JOIN pg_subscription ps ON psr.srsubid = ps.oid
         JOIN pg_class pc ON psr.srrelid = pc.oid
ORDER BY table_name;

-- Query 2.3: Verify data was copied (Run on Logical Subscriber - Port 5437 after 30 seconds)
-- Expected: users: 1, orders: 0, products: 0
-- products: 0 because it's in different publication (not subscribed yet)
SELECT 'users' as table_name, COUNT(*) as records
FROM users
UNION ALL
SELECT 'orders' as table_name, COUNT(*) as records
FROM orders
UNION ALL
SELECT 'products' as table_name, COUNT(*) as records
FROM products
ORDER BY table_name;

-- ============================================
-- Task 3: Test Live Replication
-- ============================================

-- Query 3.1a: Add new user (Run on Master - Port 5435)
-- Expected: "INSERT 0 1"
INSERT INTO users (name, email, department)
VALUES ('Logical Test User', 'logical@test.com', 'QA');

-- Query 3.1b: Add new order (Run on Master - Port 5435)
-- Expected: "INSERT 0 1"
INSERT INTO orders (user_id, product_name, amount, status)
VALUES (1, 'Replication Test Product', 199.99, 'testing');

-- Query 3.2a: Check new user replicated (Run on Logical Subscriber - Port 5437 after 5 seconds)
-- Expected: Shows the new user record
SELECT name, email, department
FROM users
WHERE email = 'logical@test.com';

-- Query 3.2b: Check new order replicated (Run on Logical Subscriber - Port 5437)
-- Expected: Shows the new order record
SELECT user_id, product_name, amount, status
FROM orders
WHERE product_name = 'Replication Test Product';

-- Query 3.3a: Test UPDATE operation (Run on Master - Port 5435)
-- Expected: "UPDATE 1"
UPDATE users
SET department = 'Quality Assurance',
    updated_at = NOW()
WHERE email = 'logical@test.com';

-- Query 3.3b: Verify UPDATE replicated (Run on Logical Subscriber - Port 5437 after 5 seconds)
-- Expected: department = 'Quality Assurance' with recent updated_at
SELECT name, email, department, updated_at
FROM users
WHERE email = 'logical@test.com';

-- ============================================
-- Task 4: Explore Limitations
-- ============================================

-- Query 4.1a: Test DDL - add column (Run on Master - Port 5435)
-- Expected: "ALTER TABLE" success
-- DDL changes do NOT replicate automatically
ALTER TABLE users
    ADD COLUMN phone VARCHAR(20);

-- Query 4.1b: Check if column exists on subscriber (Run on Logical Subscriber - Port 5437)
-- Expected: NO 'phone' column (DDL didn't replicate)
-- Master will show phone column, Subscriber will not
SELECT column_name, data_type
FROM information_schema.columns
WHERE table_name = 'users'
ORDER BY ordinal_position;

-- Query 4.2a: Check sequence values on Master (Run on Master - Port 5435)
-- Expected: Higher values from all previous INSERTs
SELECT 'users_id_seq' as sequence_name, last_value
FROM users_id_seq
UNION ALL
SELECT 'orders_id_seq' as sequence_name, last_value
FROM orders_id_seq;

-- Query 4.2b: Check sequence values on Subscriber (Run on Logical Subscriber - Port 5437)
-- Expected: Different (usually lower) values - sequences are independent
SELECT 'users_id_seq' as sequence_name, last_value
FROM users_id_seq
UNION ALL
SELECT 'orders_id_seq' as sequence_name, last_value
FROM orders_id_seq;

-- Query 4.3a: Test independent write on Subscriber (Run on Logical Subscriber - Port 5437)
-- Expected: "INSERT 0 1" - subscribers can accept their own writes
INSERT INTO users (name, email, department)
VALUES ('Local Subscriber User', 'local@subscriber.com', 'Local Ops');

-- Query 4.3b: Verify this data does NOT appear on Master (Run on Master - Port 5435)
-- Expected: No rows returned - logical replication is unidirectional
SELECT *
FROM users
WHERE email = 'local@subscriber.com';

-- ============================================
-- Task 5: Add Second Subscription
-- ============================================

-- Query 5.1: Create products subscription (Run on Logical Subscriber - Port 5437)
-- Expected: "CREATE SUBSCRIPTION"
-- This demonstrates multiple subscriptions per subscriber
CREATE SUBSCRIPTION products_subscription
    CONNECTION 'host=postgres-master port=5432 user=postgres dbname=replication_lab password=password'
    PUBLICATION products_publication
    WITH (copy_data = true);

-- Query 5.2: Verify products data copied (Run on Logical Subscriber - Port 5437 after 30 seconds)
-- Expected: 5 products
-- Shows products table now has data from separate subscription
SELECT name, price, category
FROM products;

-- ============================================
-- Bonus: Monitoring Queries
-- ============================================

-- Query B.1: Check all subscriptions (Run on Logical Subscriber - Port 5437)
-- Expected: 2 subscriptions (lab_subscription, products_subscription)
SELECT subname     as subscription_name,
       subenabled  as enabled,
       subconninfo as connection_info
FROM pg_subscription;

-- Query B.2: Check replication slots on Master (Run on Master - Port 5435)
-- Expected: Shows slots created by subscriptions
SELECT slot_name,
       plugin,
       slot_type,
       database,
       active
FROM pg_replication_slots
WHERE slot_type = 'logical';