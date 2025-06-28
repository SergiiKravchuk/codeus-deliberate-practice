-- ============================================
-- Exercise 04: Quick Replication Comparison
-- ============================================

-- ============================================
-- Task 1: Side-by-Side Behavior Test
-- ============================================

-- Query 1.1: Add test data (Run on Master - Port 5435)
-- This data will flow to both streaming slave and logical subscriber
INSERT INTO users (name, email, department)
VALUES ('Comparison Test', 'compare@test.com', 'Analysis');

SELECT COUNT(*) as total_users
FROM users;

-- Query 1.2a: Check count on Streaming Slave (Run on Port 5436)
-- Expected: Same count as master (automatic replication)
SELECT COUNT(*) as total_users
FROM users;

-- Query 1.2b: Check count on Logical Subscriber (Run on Port 5437)
-- Expected: 2-5 users (only new data added after subscription created)
SELECT COUNT(*) as total_users
FROM users;

-- ============================================
-- Task 2: Schema Change Comparison
-- ============================================

-- Query 2.1: Add column to test DDL replication (Run on Master - Port 5435)
-- This will test how each replication type handles schema changes
ALTER TABLE users
    ADD COLUMN test_column VARCHAR(10);

-- Query 2.2a: Check if column exists on Streaming Slave (Run on Port 5436)
-- Expected: Shows 'test_column' - DDL replicates automatically
SELECT column_name
FROM information_schema.columns
WHERE table_name = 'users'
  AND column_name = 'test_column';

-- Query 2.2b: Check if column exists on Logical Subscriber (Run on Port 5437)
-- Expected: No rows - DDL does NOT replicate in logical replication
SELECT column_name
FROM information_schema.columns
WHERE table_name = 'users'
  AND column_name = 'test_column';

-- ============================================
-- Summary Verification Queries
-- ============================================

-- Query S.1: Final streaming replication status (Run on Master - Port 5435)
-- Expected result example:
--   Streaming Replication,172.20.0.4,streaming,0
--   Streaming Replication,172.20.0.3,streaming,0
-- Explanation:
--   Two connections: one from streaming slave, one from logical subscriber
--   Both show 'streaming' state with 0 lag (healthy replication)
--   Different IP addresses indicate separate Docker containers
SELECT 'Streaming Replication'               as replication_type,
       client_addr                           as connected_slave,
       state,
       pg_wal_lsn_diff(sent_lsn, replay_lsn) as lag_bytes
FROM pg_stat_replication;

-- Query S.2: Final logical replication status (Run on Logical Subscriber - Port 5437)
-- Expected result example:
--   Logical Replication,lab_subscription,true
--   Logical Replication,products_subscription,true
-- Explanation:
--   Two subscriptions: lab_subscription (users+orders), products_subscription (products only)
--   Both enabled=true means logical replication is active
--   This shows the flexibility of multiple selective subscriptions
SELECT 'Logical Replication' as replication_type,
       subname               as subscription_name,
       subenabled            as enabled
FROM pg_subscription;