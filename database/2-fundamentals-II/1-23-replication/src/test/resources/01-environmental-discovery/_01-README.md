# Exercise 01: Environment Discovery

## Objective
Learn to identify database roles and understand the current replication setup by exploring the multi-container PostgreSQL environment.

## Duration: **~ 15 minutes**

## Learning Goals
By the end of this exercise, you will:
- ✅ Distinguish between Master, Streaming Slave, and Logical Subscriber databases
- ✅ Understand how to check database replication status
- ✅ Verify that streaming replication is working automatically
- ✅ Confirm that logical replication is not yet configured

## Prerequisites
- All containers running (`docker compose ps` shows 3 "Up" containers)
- All database connections configured in IntelliJ IDEA
- Basic familiarity with SQL SELECT statements

---

## Task 1: Identify Database Roles 

### Step 1.1: Check Recovery Status
Run this query on **each of the three databases**:

```sql
SELECT 
    inet_server_port() as internal_port,
    current_database() as database,
    pg_is_in_recovery() as is_standby_server,
    CASE 
        WHEN pg_is_in_recovery() THEN 'STANDBY (Read-Only)'
        ELSE 'PRIMARY (Read-Write)'
    END as server_role;
```

**Expected Results:**
- **Master (5435):** `is_standby_server = false`, role = `PRIMARY`
- **Streaming Slave (5436):** `is_standby_server = true`, role = `STANDBY`
- **Logical Subscriber (5437):** `is_standby_server = false`, role = `PRIMARY`

### Step 1.2: Understanding the Results

**❓ Question:** Why is the Logical Subscriber showing as PRIMARY and not STANDBY?

**💡 Answer:** Logical replication creates an independent database that receives logical operations (INSERT/UPDATE/DELETE), not a byte-level copy. The subscriber can accept writes and acts as a normal PostgreSQL instance.

**❓ Question:** What does `pg_is_in_recovery()` actually check?

**💡 Answer:** This function returns `true` when PostgreSQL is replaying WAL records from another server (streaming replication). It's the definitive way to identify a hot standby server.

---

## Task 2: Examine Sample Data 

### Step 2.1: Count Records on Each Database
Run this query on **all three databases**:

```sql
-- Check record counts in each table
SELECT 'users' as table_name, COUNT(*) as record_count FROM users
UNION ALL
SELECT 'orders' as table_name, COUNT(*) as record_count FROM orders  
UNION ALL
SELECT 'products' as table_name, COUNT(*) as record_count FROM products
ORDER BY table_name;
```

### Step 2.2: Understanding Data Distribution

**✅ Master & Streaming Slave:** Same data because streaming replication automatically copies everything.

**❌ Logical Subscriber:** Empty because logical replication subscriptions are not yet configured (we'll do this in Exercise 03).

---

## Task 3: Test Write Operations 

### Step 3.1: Test Write to Master
Run this INSERT on the **Master (5435)**:

```sql
INSERT INTO users (name, email, department) 
VALUES ('Lab Student', 'student@lab.com', 'IT');
```

**Expected Result:** ✅ Success - "INSERT 0 1"

### Step 3.2: Test Write to Streaming Slave
Run this INSERT on the **Streaming Slave (5436)**:

```sql
INSERT INTO users (name, email, department) 
VALUES ('Should Fail', 'fail@lab.com', 'IT');
```

**Expected Result:** ❌ Error - `"cannot execute INSERT in a read-only transaction"`

### Step 3.3: Test Write to Logical Subscriber
Run this INSERT on the **Logical Subscriber (5437)**:

```sql
INSERT INTO users (name, email, department) 
VALUES ('Local User', 'local@lab.com', 'IT');
```

**Expected Result:** ✅ Success - "INSERT 0 1" (logical subscriber accepts writes)

---

## Task 4: Verify Streaming Replication 

### Step 4.1: Check Data Replication
After the INSERT to Master, run this query on **Streaming Slave (5436)**:

```sql
SELECT name, email, department 
FROM users 
WHERE email = 'student@lab.com';
```

**Expected Result:** The new user should appear within 1-2 seconds.

**💡 Key Insight:** Data flows automatically from Master → Streaming Slave without any manual configuration.

### Step 4.2: Verify Logical Subscriber Status
Run this query on **Logical Subscriber (5437)**:

```sql
-- Check if any subscriptions exist
SELECT COUNT(*) as subscription_count FROM pg_subscription;

-- Check data from master  
SELECT name, email, department 
FROM users 
WHERE email = 'student@lab.com';
```

**Expected Results:**
- `subscription_count = 0` (no logical replication yet)
- No record with `student@lab.com` (logical replication not configured)

---

## Validation & Checkpoints

### ✅ Checkpoint 1: Role Identification
- [ ] Master shows `pg_is_in_recovery() = false`
- [ ] Streaming Slave shows `pg_is_in_recovery() = true`
- [ ] Logical Subscriber shows `pg_is_in_recovery() = false`

### ✅ Checkpoint 2: Data Verification
- [ ] Master and Streaming Slave have identical data
- [ ] Logical Subscriber tables are empty
- [ ] INSERT works on Master and Logical Subscriber
- [ ] INSERT fails on Streaming Slave

### ✅ Checkpoint 3: Replication Understanding
- [ ] Data automatically flows from Master to Streaming Slave
- [ ] No data flows to Logical Subscriber (subscriptions not configured)
- [ ] Can identify the difference between physical and logical replication behavior

---

## Key Takeaways

**🎯 Streaming Replication:**
- Automatically configured and working
- Creates exact read-only copy (Hot Standby)
- Uses `pg_is_in_recovery()` to identify standby status

**🎯 Logical Replication:**
- Requires manual configuration (subscriptions)
- Subscriber remains read-write capable
- Tables exist but data doesn't flow until subscribed

**🎯 Production Implications:**
- Streaming = High Availability, Load Balancing for reads
- Logical = Selective data sync, Cross-version replication

---

## Next Steps

You have successfully discovered the environment and understand the current replication status.

**Continue to:** [Exercise 02: Streaming Replication](../02-streaming-replication/README.md)

In the next exercise, you'll dive deeper into streaming replication monitoring, lag measurement, and load testing scenarios.