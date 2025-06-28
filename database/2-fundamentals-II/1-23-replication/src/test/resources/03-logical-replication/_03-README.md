# Exercise 03: Logical Replication

## Objective
Learn to configure logical replication, understand selective data synchronization, and explore the differences from streaming replication.

## Duration
**~ 25 minutes**

## Learning Goals
By the end of this exercise, you will:
- ✅ Configure logical replication subscriptions manually
- ✅ Understand selective table replication
- ✅ Test data flow in logical replication
- ✅ Identify limitations of logical replication (DDL, sequences)
- ✅ Compare logical vs streaming replication behavior

## Prerequisites
- **Exercise 02 completed** (streaming replication working)
- Master (5435) with publications already created
- Logical Subscriber (5437) with empty tables

---

## Task 1: Verify Starting State 

### Step 1.1: Check Publications on Master
First, verify publications exist on **Master (5435)**:

```sql
-- Check if publications were created
SELECT pubname FROM pg_publication;
```

**If no results**, create them manually:

```sql
-- Create publications manually
CREATE PUBLICATION lab_publication FOR TABLE users, orders;
CREATE PUBLICATION products_publication FOR TABLE products;
```

Then verify with the full query:

```sql
-- Check publication details
SELECT 
    pubname as publication_name,
    puballtables as includes_all_tables,
    pubinsert as replicates_insert,
    pubupdate as replicates_update,
    pubdelete as replicates_delete
FROM pg_publication;
```

**Expected Results:**
- `lab_publication`: includes users and orders tables
- `products_publication`: includes only products table
- All operation types (insert/update/delete) = `true`

**💡 Key Insight:** Publications define WHAT data the Master is willing to share. Think of them as "data catalogs."

### Step 1.2: Verify Subscriber is Empty
Run this query on **Logical Subscriber (5437)**:

```sql
-- Check current data state
SELECT 'users' as table_name, COUNT(*) as records FROM users
UNION ALL
SELECT 'orders' as table_name, COUNT(*) as records FROM orders
UNION ALL
SELECT 'products' as table_name, COUNT(*) as records FROM products
ORDER BY table_name;
```

**Expected Results:** Tables could consist records from previous queries

Logical replication doesn't start automatically. You must create subscriptions manually.

---

## Task 2: Create First Subscription 

### Step 2.1: Create Subscription for Users and Orders
Run this command on **Logical Subscriber (5437)**:

```sql
-- Create subscription to replicate users and orders
CREATE SUBSCRIPTION lab_subscription
CONNECTION 'host=postgres-master port=5432 user=postgres dbname=replication_lab password=password'
PUBLICATION lab_publication
WITH (copy_data = false);
```

**Expected Result:**
```
NOTICE: created replication slot "lab_subscription" on publisher
CREATE SUBSCRIPTION
```

**💡 What Happened:**
- Created connection from Subscriber to Master
- Subscribed to `lab_publication` (users + orders tables)
- copy_data = false prevents primary key conflicts with existing data from previous exercises

### Step 2.2: Verify Subscription Status
Run this query on **Logical Subscriber (5437)**:

```sql
-- Check subscription status
SELECT 
    subname as subscription_name,
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
```

**Expected Results (initially):**
- Both tables showing `Data is being copied` or `Synchronized`
- After 30 seconds: Both showing `Ready (normal replication)`

**💡 Understanding States:** Logical replication goes through phases: initial copy → synchronization → ongoing replication.

### Step 2.3: Verify Data Copied
Run this query on **Logical Subscriber (5437)** after 30 seconds:

```sql
-- Check if data was copied from master
SELECT 'users' as table_name, COUNT(*) as records FROM users
UNION ALL
SELECT 'orders' as table_name, COUNT(*) as records FROM orders
UNION ALL  
SELECT 'products' as table_name, COUNT(*) as records FROM products
ORDER BY table_name;
```

**Expected Results:**
- `users`: 1 record 
- `orders`: 105 records (original 5 + 100 from load test)
- `products`: 0 records (not subscribed yet)

**💡 Selective Replication:** Only subscribed tables received data. Products table remains empty because it's in a different publication.

---

## Task 3: Test Live Replication 

### Step 3.1: Add New Data on Master
Run this on **Master (5435)**:

```sql
-- Add a new user
INSERT INTO users (name, email, department) 
VALUES ('Logical Test User', 'logical@test.com', 'QA');

-- Add a new order
INSERT INTO orders (user_id, product_name, amount, status)
VALUES (1, 'Replication Test Product', 199.99, 'testing');
```

**Expected Results:** Both INSERT statements succeed

### Step 3.2: Verify Real-time Replication
Wait **5 seconds**, then run on **Logical Subscriber (5437)**:

```sql
-- Check for new data
SELECT name, email, department 
FROM users 
WHERE email = 'logical@test.com';

SELECT user_id, product_name, amount, status
FROM orders 
WHERE product_name = 'Replication Test Product';
```

**Expected Results:** Both new records should appear on the subscriber

**💡 Key Insight:** Logical replication is near real-time. Data flows within seconds of being committed on the master.

### Step 3.3: Test Update Operations
Run this UPDATE on **Master (5435)**:

```sql
-- Update user department
UPDATE users 
SET department = 'Quality Assurance', updated_at = NOW()
WHERE email = 'logical@test.com';
```

Verify on **Logical Subscriber (5437)**:

```sql
-- Check if update replicated
SELECT name, email, department, updated_at
FROM users 
WHERE email = 'logical@test.com';
```

**Expected Result:** Department should show `Quality Assurance` with recent `updated_at`

**💡 Key Insight:** Logical replication handles INSERT, UPDATE, and DELETE operations, but tracks changes at the row level.

---

## Task 4: Explore Logical Replication Limitations 

### Step 4.1: Test DDL Replication (Should NOT Work)
Run this on **Master (5435)**:

```sql
-- Try to add a new column
ALTER TABLE users ADD COLUMN phone VARCHAR(20);
```

Check if column exists on **Logical Subscriber (5437)**:

```sql
-- Check table structure
SELECT column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'users' 
ORDER BY ordinal_position;
```

**Expected Results:**
- Master: Shows `phone` column
- Subscriber: Does NOT show `phone` column

**💡 Major Limitation:** DDL (schema changes) are NOT replicated in logical replication. You must apply schema changes manually on all subscribers.

### Step 4.2: Test Sequence Behavior
Run this on **Master (5435)**:

```sql
-- Check current sequence values
SELECT 'users_id_seq' as sequence_name, last_value FROM users_id_seq
UNION ALL
SELECT 'orders_id_seq' as sequence_name, last_value FROM orders_id_seq;
```

Run the same query on **Logical Subscriber (5437)**:

**Expected Results:**
- Master: Higher sequence values (from all INSERTs)
- Subscriber: Different (usually lower) sequence values

**💡 Another Limitation:** Sequences are NOT synchronized. Each database maintains independent sequence values.

### Step 4.3: Test Independent Writes on Subscriber
Run this INSERT on **Logical Subscriber (5437)**:

```sql
-- Add data directly to subscriber (this works!)
INSERT INTO users (name, email, department)
VALUES ('Local Subscriber User', 'local@subscriber.com', 'Local Ops');
```

**Expected Result:** INSERT succeeds

Check this data does NOT appear on **Master (5435)**:

```sql
-- This should return no rows
SELECT * FROM users WHERE email = 'local@subscriber.com';
```

**💡 Key Difference:** Unlike streaming replication, logical subscribers can accept their own writes. Replication is unidirectional.

---

## Task 5: Add Second Subscription 

### Step 5.1: Subscribe to Products Table
Run this on **Logical Subscriber (5437)**:

```sql
-- Create second subscription for products
CREATE SUBSCRIPTION products_subscription
CONNECTION 'host=postgres-master port=5432 user=postgres dbname=replication_lab password=password'
PUBLICATION products_publication
WITH (copy_data = true);
```

### Step 5.2: Verify Products Data
After 30 seconds, run on **Logical Subscriber (5437)**:

```sql
-- Check products data
SELECT name, price, category FROM products;
```

**Expected Results:** Should show 3 products (Laptop, Mouse, Chair)

**💡 Flexible Subscriptions:** You can subscribe to different publications independently, giving fine-grained control over what data to replicate.

---

## Validation & Checkpoints

### ✅ Checkpoint 1: Subscription Setup
- [ ] `lab_subscription` created and showing `Ready` status
- [ ] Users and orders data copied from master
- [ ] Products table initially empty (different publication)

### ✅ Checkpoint 2: Live Replication Testing
- [ ] New INSERT on master appears on subscriber within 5 seconds
- [ ] UPDATE operations replicate correctly
- [ ] Real-time replication is working

### ✅ Checkpoint 3: Limitations Understanding
- [ ] DDL changes (ALTER TABLE) do NOT replicate
- [ ] Sequence values are independent between master and subscriber
- [ ] Subscriber can accept independent writes (bidirectional capability)

### ✅ Checkpoint 4: Multiple Subscriptions
- [ ] Products subscription created successfully
- [ ] Products data copied independently
- [ ] Can manage multiple publications/subscriptions per subscriber

---

## Key Takeaways

**🎯 Manual Configuration:**
- Logical replication requires explicit subscription setup
- Publications define what's available, subscriptions define what you want
- Each subscription can target different tables

**🎯 Selective Replication:**
- Choose exactly which tables to replicate
- Different publications can contain different table sets
- Fine-grained control over data flow

**🎯 Important Limitations:**
- NO DDL replication (schema changes must be manual)
- NO sequence synchronization
- NO automatic conflict resolution

**🎯 Flexibility Benefits:**
- Subscribers remain read-write capable
- Can replicate between different PostgreSQL versions
- Supports data transformation (with triggers)
- Multiple subscribers can subscribe to same publication

**🎯 Production Use Cases:**
- Data warehousing (selective table replication)
- Cross-region data distribution
- Database upgrades (replicate from old to new version)
- Reporting databases (without affecting main database)

---

## Next Steps

You've successfully configured logical replication and understand its capabilities and limitations.

**Continue to:** [Exercise 04: Comparison Analysis](../04-comparison-analysis/README.md)

In the final exercise, you'll compare both replication types side-by-side and learn when to use each approach in production scenarios.