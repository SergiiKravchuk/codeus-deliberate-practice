-- Exercise 5: Automatic Maintenance Configuration
-- Description: Configure autovacuum with very aggressive settings for transactions (vacuum_scale_factor 0.5%, vacuum_threshold 10, analyze_scale_factor 0.5%, analyze_threshold 10),
-- moderate for accounts (vacuum_scale_factor 10%, analyze_scale_factor 5%),
-- relaxed for customers (vacuum_scale_factor 20%, analyze_scale_factor 10%, analyze_threshold 25)

-- Create sample accounts and customers tables for configuration practice
CREATE TABLE IF NOT EXISTS accounts (
    id SERIAL PRIMARY KEY,
    customer_id INTEGER,
    balance NUMERIC(15,2),
    account_type VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS customers (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(20),
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


/* 🔍 DETAILED WALKTHROUGH:
   
   📊 ALTER TABLE syntax: ALTER TABLE table_name SET (parameter = value, ...)
   
   🔧 Autovacuum parameters explained:
   - autovacuum_vacuum_scale_factor: % of table size to trigger VACUUM
   - autovacuum_vacuum_threshold: minimum dead tuples before VACUUM
   - autovacuum_analyze_scale_factor: % of table size to trigger ANALYZE
   - autovacuum_analyze_threshold: minimum changed tuples for ANALYZE

   💡 Lower values = more frequent cleaning, higher values = less frequent
   🧮 Trigger formula: threshold + (live_tuples × scale_factor)
*/

-- TODO: Configure aggressive autovacuum for high-activity transactions table
ALTER TABLE transactions SET (
    autovacuum_vacuum_scale_factor = 0.005,
    autovacuum_vacuum_threshold = 10,
    autovacuum_analyze_scale_factor = 0.005,
    autovacuum_analyze_threshold = 10
);

-- TODO: Configure moderate autovacuum for medium-activity accounts table
ALTER TABLE accounts SET (
    autovacuum_vacuum_scale_factor = 0.1,
    autovacuum_analyze_scale_factor = 0.05
);

-- TODO: Configure relaxed autovacuum for low-activity customers table  
ALTER TABLE customers SET (
    autovacuum_vacuum_scale_factor = 0.2,
    autovacuum_analyze_scale_factor = 0.1,
    autovacuum_analyze_threshold = 25
);

