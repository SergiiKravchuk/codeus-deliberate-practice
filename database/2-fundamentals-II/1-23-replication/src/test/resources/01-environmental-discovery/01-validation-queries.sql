-- ============================================
-- Exercise 01: Environment Discovery
-- Validation and Test Queries
-- ============================================

-- ============================================
-- Task 1: Database Role Identification
-- Run on ALL three databases (Master, Streaming Slave, Logical Subscriber)
-- ============================================

-- Query 1.1: Check server role and recovery status
SELECT inet_server_port()  as internal_port,
       current_database()  as database,
       pg_is_in_recovery() as is_standby_server,
       CASE
           WHEN pg_is_in_recovery() THEN 'STANDBY (Read-Only)'
           ELSE 'PRIMARY (Read-Write)'
           END             as server_role,
       version()           as postgresql_version;

-- ============================================
-- Task 2: Data Inventory
-- Run on ALL three databases
-- ============================================

-- Query 2.1: Count records in each table
SELECT 'users' as table_name, COUNT(*) as record_count
FROM users
UNION ALL
SELECT 'orders' as table_name, COUNT(*) as record_count
FROM orders
UNION ALL
SELECT 'products' as table_name, COUNT(*) as record_count
FROM products
ORDER BY table_name;

-- Query 2.2: View sample data from users table
SELECT id, name, email, department, created_at
FROM users
ORDER BY id;

-- Query 2.3: View sample data from orders table
SELECT id, user_id, product_name, amount, status, created_at
FROM orders
ORDER BY id;

-- ============================================
-- Task 3: Write Operation Tests
-- ============================================

-- Query 3.1: INSERT to Master (Port 5435) - Should SUCCEED
INSERT INTO users (name, email, department)
VALUES ('Lab Student', 'student@lab.com', 'IT');

-- Query 3.2: INSERT to Streaming Slave (Port 5436) - Should FAIL
-- Expected Result: ERROR "cannot execute INSERT in a read-only transaction"
-- Explanation: Streaming slaves are Hot Standby (read-only) servers
INSERT INTO users (name, email, department)
VALUES ('Should Fail', 'fail@lab.com', 'IT');

-- Query 3.3: INSERT to Logical Subscriber (Port 5437) - Should SUCCEED
-- Explanation: Logical subscribers remain read-write capable (independent databases)
INSERT INTO users (name, email, department)
VALUES ('Local User', 'local@lab.com', 'IT');

-- ============================================
-- Task 4: Replication Verification
-- ============================================

-- Query 4.1: Check for new user on Streaming Slave (after Master INSERT)
-- Run on Streaming Slave (Port 5436)
-- Expected Result: Should show the "Lab Student" record within 1-2 seconds
-- Explanation: Streaming replication automatically copies data from Master to Slave
SELECT name, email, department, created_at
FROM users
WHERE email = 'student@lab.com';

-- Query 4.2: Check logical replication status on Subscriber
-- Run on Logical Subscriber (Port 5437)
-- Expected Result: 0 (no subscriptions configured yet)
-- Note: Logical replication requires manual subscription setup
SELECT COUNT(*) as subscription_count
FROM pg_subscription;

-- Query 4.3: Verify Master data is NOT on Logical Subscriber
-- Run on Logical Subscriber (Port 5437)
-- Expected Result: No rows (empty result)
-- Explanation: Without subscriptions, no data flows from Master to Logical Subscriber
SELECT name, email, department
FROM users
WHERE email = 'student@lab.com';

-- ============================================
-- Bonus: Advanced Verification Queries
-- ============================================

-- Query B.1: Check current WAL position (Master only)
-- Run on Master (Port 5435)
SELECT pg_current_wal_lsn() as current_wal_position;

-- Query B.2: Check last replayed WAL position (Streaming Slave only)
-- Run on Streaming Slave (Port 5436)
SELECT pg_last_wal_replay_lsn() as last_replayed_wal_position;

-- Query B.3: Check publications available (Master only)
-- Run on Master (Port 5435)
-- Expected result example:
--   lab_publication,false,true,true,true
--   products_publication,false,true,true,true
-- Explanation:
--   pubname: Name of publication
--   puballtables: false = specific tables only (not all tables)
--   pubinsert/pubupdate/pubdelete: true = all DML operations are replicated
-- This shows what data Master is willing to share via logical replication
SELECT pubname, puballtables, pubinsert, pubupdate, pubdelete
FROM pg_publication;

-- Query B.4: Check replication connections (Master only)
-- Run on Master (Port 5435)
-- Expected result example: 172.20.0.4,streaming,0/304A2B0,0/304A2B0,0/304A2B0,0/304A2B0
-- Explanation:
--   client_addr: IP of streaming slave container (172.x.x.x)
--   state: 'streaming' = active replication
--   sent_lsn: WAL position sent to slave
--   write_lsn: WAL position written by slave
--   flush_lsn: WAL position flushed to disk by slave
--   replay_lsn: WAL position replayed by slave
-- When all LSN values are equal = no lag, perfect sync
SELECT client_addr, state, sent_lsn, write_lsn, flush_lsn, replay_lsn
FROM pg_stat_replication;