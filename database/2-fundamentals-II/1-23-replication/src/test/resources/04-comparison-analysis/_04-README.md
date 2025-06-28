# Exercise 04: Replication Comparison

## Objective
Quick comparison of streaming vs logical replication to understand when to use each approach.

## Duration
**10 minutes**

## Learning Goals
- ✅ Compare both replication types side-by-side
- ✅ Understand production use cases for each
- ✅ Make informed decisions about replication strategy

---

## Task 1: Side-by-Side Behavior Test

### Step 1.1: Add Test Data
Run this on **Master (5435)**:

```sql
-- Add data that will flow to both replicas
INSERT INTO users (name, email, department) 
VALUES ('Comparison Test', 'compare@test.com', 'Analysis');
```

### Step 1.2: Verify Distribution
Check on **Streaming Slave (5436)**:
```sql
SELECT COUNT(*) as total_users FROM users;
```

Check on **Logical Subscriber (5437)**:
```sql
SELECT COUNT(*) as total_users FROM users;
```

**Expected Results:**
- Streaming Slave: Same count as Master (automatic)
- Logical Subscriber: 2-5

**💡 Key Insight:** Both receive it's data, but through different mechanisms.

---

## Task 2: Limitation Comparison 

### Step 2.1: Schema Change Test
Run this on **Master (5435)**:
```sql
ALTER TABLE users ADD COLUMN test_column VARCHAR(10);
```

### Step 2.2: Check Schema Sync
Check table structure on **Streaming Slave (5436)**:
```sql
SELECT column_name FROM information_schema.columns 
WHERE table_name = 'users' AND column_name = 'test_column';
```

Check on **Logical Subscriber (5437)**:
```sql
SELECT column_name FROM information_schema.columns 
WHERE table_name = 'users' AND column_name = 'test_column';
```

**Results:**
- **Streaming Slave:** Shows `test_column` ✅
- **Logical Subscriber:** Does NOT show `test_column` ❌

---

## Task 3: When to Use What 

Review these scenarios and choose the right replication type:

### Scenario A: High Availability Setup
**Need:** Exact backup for failover if main database fails

**Best Choice:** 🟦 **Streaming Replication**

**Why:** Identical copy, can promote to master instantly

### Scenario B: Analytics Database
**Need:** Only customer and order data for reporting

**Best Choice:** 🟩 **Logical Replication**

**Why:** Selective tables, no sensitive data, read-write capability

### Scenario C: Database Version Upgrade
**Need:** Migrate from PostgreSQL 13 to PostgreSQL 16

**Best Choice:** 🟩 **Logical Replication**

**Why:** Different versions supported, gradual migration

### Scenario D: Load Balancing Reads
**Need:** Multiple read-only copies for query distribution

**Best Choice:** 🟦 **Streaming Replication**

**Why:** Automatic setup, identical data, read-only enforcement

---

## Quick Decision Matrix

| Requirement | Streaming | Logical | Winner |
|-------------|-----------|---------|--------|
| **Easy Setup** | Auto ✅ | Manual ❌ | 🟦 Streaming |
| **Selective Data** | All tables ❌ | Choose tables ✅ | 🟩 Logical |
| **DDL Sync** | Yes ✅ | No ❌ | 🟦 Streaming |
| **Cross-Version** | Same version ❌ | Different OK ✅ | 🟩 Logical |
| **Failover Ready** | Yes ✅ | No ❌ | 🟦 Streaming |
| **Data Transform** | No ❌ | Yes ✅ | 🟩 Logical |

---

## Key Takeaways

**🟦 Use Streaming Replication For:**
- High availability and disaster recovery
- Load balancing read queries
- Exact database copies
- Simple setup requirements

**🟩 Use Logical Replication For:**
- Data warehousing and analytics
- Cross-version database upgrades
- Selective data synchronization
- Multi-tenant data distribution

**🎯 Production Reality:**
Many organizations use **BOTH** - streaming for HA/DR and logical for analytics!

---

## Practice Complete! 🎉

You've successfully:
- ✅ Set up streaming replication (automatic)
- ✅ Configured logical replication (manual)
- ✅ Tested both under load
- ✅ Understood limitations and use cases

**Next Steps:** Apply this knowledge to your real projects and choose the right replication strategy for your specific needs.