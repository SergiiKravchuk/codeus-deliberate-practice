# Exercise 02: Streaming Replication

## Objective
Master streaming replication monitoring, lag measurement, and performance testing in a production-like environment.

## Duration: **~ 15 minutes**

## Learning Goals
By the end of this exercise, you will:
- ✅ Monitor streaming replication status and health
- ✅ Measure and understand replication lag
- ✅ Test replication under load conditions
- ✅ Identify performance bottlenecks in streaming replication
- ✅ Use production-ready monitoring queries

## Prerequisites
- **Exercise 01 completed** (environment verified)
- Master (5435) and Streaming Slave (5436) operational
- Understanding of `pg_is_in_recovery()` concept

---

## Task 1: Replication Status Monitoring 

### Step 1.1: Check Replication Connections (Master Side)
Run this query on **Master (5435)**:

```sql
-- Monitor active replication connections
SELECT
    client_addr as slave_ip,
    state,
    sent_lsn,
    write_lsn,
    flush_lsn,
    replay_lsn,
    sync_state,
    backend_start
FROM pg_stat_replication;
```

**Expected Results:**
- `client_addr`: IP of streaming slave container (172.x.x.x)
- `state`: `streaming` (active replication)
- `sync_state`: `async` (asynchronous replication)
- LSN values: Should be close/identical

**💡 Key Insight:** `pg_stat_replication` shows all slaves connected to this master. In production, you'd see multiple slaves here.

### Step 1.2: Check WAL Receiver Status (Slave Side)
Run this query on **Streaming Slave (5436)**:

```sql
-- Monitor WAL receiver process
SELECT 
    status,
    receive_start_lsn,
    receive_start_tli,
    written_lsn,
    flushed_lsn,
    last_msg_send_time,
    last_msg_receipt_time,
    conninfo
FROM pg_stat_wal_receiver;
```

**Expected Results:**
- `status`: `streaming`
- `written_lsn`: Should match or be close to master's `sent_lsn`
- `last_msg_receipt_time`: Recent timestamp (within seconds)

**💡 Key Insight:** This shows the slave's perspective of replication health. If `status != 'streaming'`, replication is broken.

---

## Task 2: Replication Lag Measurement 

### Step 2.1: Calculate Current Lag (Master)
Run this query on **Master (5435)**:

```sql
-- Calculate replication lag in bytes and MB
SELECT 
    client_addr as slave_ip,
    pg_wal_lsn_diff(sent_lsn, replay_lsn) as lag_bytes,
    ROUND(pg_wal_lsn_diff(sent_lsn, replay_lsn) / 1024.0 / 1024.0, 2) as lag_mb,
    CASE 
        WHEN pg_wal_lsn_diff(sent_lsn, replay_lsn) = 0 THEN 'No Lag'
        WHEN pg_wal_lsn_diff(sent_lsn, replay_lsn) < 1024*1024 THEN 'Low Lag'
        ELSE 'High Lag'
    END as lag_status
FROM pg_stat_replication;
```

**Expected Results:**
- `lag_bytes`: 0 or very small number (< 1000)
- `lag_mb`: 0.00 or close to 0
- `lag_status`: `No Lag` or `Low Lag`

**💡 Why Low Lag?** In a lightly loaded test environment, lag should be minimal. Production systems under heavy write load will show higher lag.

### Step 2.2: Time-Based Lag Analysis (Slave)
Run this query on **Streaming Slave (5436)**:

```sql
-- Calculate time-based replication delay
SELECT 
    EXTRACT(EPOCH FROM (now() - pg_last_xact_replay_timestamp())) as replay_lag_seconds,
    pg_last_xact_replay_timestamp() as last_transaction_replayed,
    CASE 
        WHEN pg_is_wal_replay_paused() THEN 'PAUSED'
        ELSE 'ACTIVE'
    END as replay_status
FROM pg_stat_wal_receiver;
```

**Expected Results:**
- `replay_lag_seconds`: Small number or NULL
- If `replay_lag_seconds` is NULL: No recent transactions to measure
- `replay_status`: `ACTIVE`

**💡 Production Insight:** `replay_lag_seconds` shows how "current" your slave data is compared to master.

---

## Task 3: Load Testing Replication 

### Step 3.1: Generate Write Load on Master
Run this INSERT loop on **Master (5435)**:

```sql
-- Create a batch of orders to test replication under load
INSERT INTO orders (user_id, product_name, amount, status)
SELECT 
    (random() * 3 + 1)::INTEGER as user_id,
    'Load Test Product ' || generate_series as product_name,
    (random() * 500 + 50)::DECIMAL(10,2) as amount,
    CASE (random() * 4)::INTEGER
        WHEN 0 THEN 'pending'
        WHEN 1 THEN 'processing' 
        WHEN 2 THEN 'shipped'
        ELSE 'completed'
    END as status
FROM generate_series(1, 100);
```

**Expected Result:** `INSERT 0 100` (100 new orders created)

**💡 What's Happening:** This simulates a burst of e-commerce orders hitting your database.

### Step 3.2: Monitor Lag During Load
**Immediately after** the INSERT, run this monitoring query on **Streaming Slave (5436)**:

```sql
-- Check lag from slave perspective right after bulk insert
SELECT 
    NOW() as measurement_time,
    pg_last_wal_receive_lsn() as received_lsn,
    pg_last_wal_replay_lsn() as replayed_lsn,
    pg_wal_lsn_diff(
        pg_last_wal_receive_lsn(), 
        pg_last_wal_replay_lsn()
    ) as pending_replay_bytes,
    EXTRACT(EPOCH FROM (now() - pg_last_xact_replay_timestamp())) as seconds_behind
FROM pg_stat_wal_receiver;
```

**Expected Results:**
- `pending_replay_bytes`: May be > 0 temporarily (WAL received but not yet replayed)
- `seconds_behind`: Small number showing time lag

**💡 Key Observation:** You can see the slave "catching up" - receiving WAL faster than replaying it during load bursts.

### Step 3.3: Verify Data Consistency
Wait **10 seconds**, then run on **Streaming Slave (5436)**:

```sql
-- Count total orders on slave
SELECT COUNT(*) as total_orders FROM orders;

-- Check for recent load test orders
SELECT COUNT(*) as load_test_orders 
FROM orders 
WHERE product_name LIKE 'Load Test Product%';
```

**Expected Results:**
- `total_orders`: Should match master count (original 5 + 100 new = 105)
- `load_test_orders`: 100

**💡 Production Insight:** Even under load, streaming replication maintains consistency. All data eventually arrives on slave.

---

## Task 4: Production Monitoring Queries 

### Step 4.1: Create a Replication Health Check
Run this comprehensive monitoring query on **Master (5435)**:

```sql
-- Production-ready replication monitoring query
WITH replication_status AS (
    SELECT 
        client_addr,
        state,
        pg_wal_lsn_diff(sent_lsn, replay_lsn) as lag_bytes,
        EXTRACT(EPOCH FROM (now() - backend_start)) as connection_age_seconds
    FROM pg_stat_replication
)
SELECT 
    client_addr as slave_server,
    state as replication_state,
    lag_bytes,
    ROUND(lag_bytes / 1024.0 / 1024.0, 2) as lag_mb,
    ROUND(connection_age_seconds / 60.0, 1) as connection_age_minutes,
    CASE 
        WHEN state != 'streaming' THEN '🔴 CRITICAL: Not streaming'
        WHEN lag_bytes > 100*1024*1024 THEN '🟡 WARNING: High lag (>100MB)'
        WHEN lag_bytes > 10*1024*1024 THEN '🟠 CAUTION: Moderate lag (>10MB)'
        ELSE '🟢 HEALTHY: Low lag'
    END as health_status
FROM replication_status;
```

**Expected Results:**
- `replication_state`: `streaming`
- `lag_mb`: < 1.0
- `health_status`: `🟢 HEALTHY: Low lag`

**💡 Production Use:** Save this query as a monitoring dashboard or alerting rule.

---

## Validation & Checkpoints

### ✅ Checkpoint 1: Monitoring Setup
- [ ] `pg_stat_replication` shows connected slave
- [ ] `pg_stat_wal_receiver` shows streaming status
- [ ] Can calculate lag in both bytes and time

### ✅ Checkpoint 2: Load Testing
- [ ] Successfully inserted 100 orders on master
- [ ] Observed lag increase during bulk insert
- [ ] Verified all data replicated to slave within 10 seconds

### ✅ Checkpoint 3: Production Readiness
- [ ] Understand difference between `sent_lsn`, `write_lsn`, `flush_lsn`, `replay_lsn`
- [ ] Can identify healthy vs unhealthy replication states
- [ ] Created reusable monitoring queries

---

## Key Takeaways

**🎯 Monitoring is Essential:**
- `pg_stat_replication` (master perspective)
- `pg_stat_wal_receiver` (slave perspective)
- Both views needed for complete picture

**🎯 Lag is Normal:**
- Some lag expected under write load
- Zero lag only in idle systems
- Monitor trends, not absolute values

**🎯 Production Implications:**
- Streaming replication handles bursts well
- Slaves eventually consistent (not immediately)
- Health checks should include lag thresholds

**🎯 Performance Characteristics:**
- Lag increases with write volume
- Network latency affects replication speed
- Slave hardware impacts replay performance

---

## Troubleshooting Common Issues

**❌ Problem:** `pg_stat_replication` shows no rows
**✅ Solution:** Streaming slave not connected. Check slave logs: `docker logs pg-streaming-slave`

**❌ Problem:** High lag (>100MB) persists
**✅ Solution:** Check network, slave performance, or reduce write load

**❌ Problem:** `state != 'streaming'`  
**✅ Solution:** Replication broken. Check authentication, network, WAL retention

---

## Next Steps

You've mastered streaming replication monitoring and understand its performance characteristics under load.

**Continue to:** [Exercise 03: Logical Replication](../03-logical-replication/README.md)

In the next exercise, you'll set up logical replication subscriptions and explore selective data synchronization capabilities.