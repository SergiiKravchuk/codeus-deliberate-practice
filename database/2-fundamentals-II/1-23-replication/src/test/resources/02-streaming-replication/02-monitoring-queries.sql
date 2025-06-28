-- ============================================
-- Exercise 02: Streaming Replication
-- Monitoring and Performance Testing Queries
-- ============================================

-- ============================================
-- Task 1: Replication Status Monitoring
-- ============================================

-- Query 1.1: Check replication connections (Run on Master - Port 5435)
-- Expected: One row showing streaming slave connection
-- slave_ip: 172.x.x.x, state: 'streaming', sync_state: 'async'
SELECT client_addr as slave_ip,
       state,
       sent_lsn,
       write_lsn,
       flush_lsn,
       replay_lsn,
       sync_state,
       backend_start
FROM pg_stat_replication;

-- Query 1.2: Check WAL receiver status (Run on Streaming Slave - Port 5436)
-- Expected: status: 'streaming', recent last_msg_receipt_time
-- If empty result: replication is broken
SELECT status,
       receive_start_lsn,
       receive_start_tli,
       written_lsn,
       flushed_lsn,
       last_msg_send_time,
       last_msg_receipt_time,
       conninfo
FROM pg_stat_wal_receiver;

-- ============================================
-- Task 2: Replication Lag Measurement
-- ============================================

-- Query 2.1: Calculate current lag in bytes and MB (Run on Master - Port 5435)
-- Expected: lag_bytes: 0 or small, lag_mb: 0.00, lag_status: 'No Lag' or 'Low Lag'
-- High lag indicates performance issues or heavy write load
SELECT client_addr                                                       as slave_ip,
       pg_wal_lsn_diff(sent_lsn, replay_lsn)                             as lag_bytes,
       ROUND(pg_wal_lsn_diff(sent_lsn, replay_lsn) / 1024.0 / 1024.0, 2) as lag_mb,
       CASE
           WHEN pg_wal_lsn_diff(sent_lsn, replay_lsn) = 0 THEN 'No Lag'
           WHEN pg_wal_lsn_diff(sent_lsn, replay_lsn) < 1024 * 1024 THEN 'Low Lag'
           ELSE 'High Lag'
           END                                                           as lag_status
FROM pg_stat_replication;

-- Query 2.2: Time-based lag analysis (Run on Streaming Slave - Port 5436)
-- Expected: receive_replay_lag_bytes: 0, replay_lag_seconds: small or NULL
-- NULL replay_lag_seconds means no recent transactions to measure
SELECT pg_last_wal_receive_lsn()                                     as last_received,
       pg_last_wal_replay_lsn()                                      as last_replayed,
       pg_wal_lsn_diff(
               pg_last_wal_receive_lsn(),
               pg_last_wal_replay_lsn()
       )                                                             as receive_replay_lag_bytes,
       EXTRACT(EPOCH FROM (now() - pg_last_xact_replay_timestamp())) as replay_lag_seconds;

-- ============================================
-- Task 3: Load Testing Replication
-- ============================================

-- Query 3.1: Generate write load (Run on Master - Port 5435)
-- Expected: "INSERT 0 100" - creates 100 new orders
-- This simulates a burst of e-commerce transactions
INSERT INTO orders (user_id, product_name, amount, status)
SELECT (random() * 3 + 1)::INTEGER             as user_id,
       'Load Test Product ' || generate_series as product_name,
       (random() * 500 + 50)::DECIMAL(10, 2)   as amount,
       CASE (random() * 4)::INTEGER
           WHEN 0 THEN 'pending'
           WHEN 1 THEN 'processing'
           WHEN 2 THEN 'shipped'
           ELSE 'completed'
           END                                 as status
FROM generate_series(1, 100);

-- Query 3.2: Monitor lag during load (Run IMMEDIATELY after 3.1 on Streaming Slave - Port 5436)
-- Expected: pending_replay_bytes may be > 0 temporarily, shows slave catching up
-- Demonstrates lag from slave perspective during write bursts
SELECT NOW()                                                         as measurement_time,
       pg_last_wal_receive_lsn()                                     as received_lsn,
       pg_last_wal_replay_lsn()                                      as replayed_lsn,
       pg_wal_lsn_diff(
               pg_last_wal_receive_lsn(),
               pg_last_wal_replay_lsn()
       )                                                             as pending_replay_bytes,
       EXTRACT(EPOCH FROM (now() - pg_last_xact_replay_timestamp())) as seconds_behind;

-- Query 3.3a: Verify data consistency - total count (Run on Streaming Slave - Port 5436 after 10 seconds)
-- Expected: total_orders: 105 (original 5 + 100 new)
-- Confirms all data replicated successfully
SELECT COUNT(*) as total_orders
FROM orders;

-- Query 3.3b: Verify load test data (Run on Streaming Slave - Port 5436)
-- Expected: load_test_orders: 100
-- Confirms our specific test data arrived
SELECT COUNT(*) as load_test_orders
FROM orders
WHERE product_name LIKE 'Load Test Product%';

-- ============================================
-- Task 4: Production Monitoring Query
-- ============================================

-- Query 4.1: Comprehensive replication health check (Run on Master - Port 5435)
-- Expected: replication_state: 'streaming', lag_mb: < 1.0, health_status: '🟢 HEALTHY'
-- Use this query for production monitoring and alerting
WITH replication_status AS (SELECT client_addr,
                                   state,
                                   pg_wal_lsn_diff(sent_lsn, replay_lsn)       as lag_bytes,
                                   EXTRACT(EPOCH FROM (now() - backend_start)) as connection_age_seconds
                            FROM pg_stat_replication)
SELECT client_addr                             as slave_server,
       state                                   as replication_state,
       lag_bytes,
       ROUND(lag_bytes / 1024.0 / 1024.0, 2)   as lag_mb,
       ROUND(connection_age_seconds / 60.0, 1) as connection_age_minutes,
       CASE
           WHEN state != 'streaming' THEN '🔴 CRITICAL: Not streaming'
           WHEN lag_bytes > 100 * 1024 * 1024 THEN '🟡 WARNING: High lag (>100MB)'
           WHEN lag_bytes > 10 * 1024 * 1024 THEN '🟠 CAUTION: Moderate lag (>10MB)'
           ELSE '🟢 HEALTHY: Low lag'
           END                                 as health_status
FROM replication_status;

-- ============================================
-- Bonus: Advanced Monitoring Queries
-- ============================================

-- ============================================
-- Bonus: Advanced Monitoring Queries
-- ============================================

-- Query B.1: Detailed slave status (Run on Streaming Slave - Port 5436)
-- Comprehensive slave health information
SELECT pg_is_in_recovery()                                           as is_standby,
       CASE
           WHEN pg_is_wal_replay_paused() THEN 'PAUSED'
           ELSE 'ACTIVE'
           END                                                       as replay_status,
       pg_last_xact_replay_timestamp()                               as last_transaction_time,
       EXTRACT(EPOCH FROM (now() - pg_last_xact_replay_timestamp())) as seconds_since_last_transaction;

-- Query B.2: Check for replication conflicts (Run on Streaming Slave - Port 5436)
-- Expected: All conflict counts should be 0 in normal operation
-- Non-zero values indicate issues that can pause replication
SELECT confl_tablespace as tablespace_conflicts,
       confl_lock       as lock_conflicts,
       confl_snapshot   as snapshot_conflicts,
       confl_bufferpin  as buffer_conflicts,
       confl_deadlock   as deadlock_conflicts
FROM pg_stat_database_conflicts
WHERE datname = current_database();