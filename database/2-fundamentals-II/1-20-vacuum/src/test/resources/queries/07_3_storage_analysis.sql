-- Exercise 7.3: Storage Analysis
-- Description: Analyze table storage usage to identify space optimization opportunities

-- Add more data to demonstrate size differences
INSERT INTO transactions (account_id, transaction_type, amount, transaction_date)
SELECT 
    (random() * 5)::int + 1,
    'fee',
    5.00,
    CURRENT_TIMESTAMP
FROM generate_series(1, 200);

-- TODO: Implement your solution here

/* 🔍 DETAILED WALKTHROUGH:
   
   📊 Functions for size analysis:
   - pg_total_relation_size(table): total size including indexes
   - pg_relation_size(table): table size without indexes  
   - pg_indexes_size(table): size of all indexes
   - pg_size_pretty(bytes): converts bytes to human readable format
   
   📊 Table: pg_stat_user_tables
   - relname: table name (use with ::regclass for size functions)
*/

-- Solution:
SELECT relname, pg_size_pretty(pg_total_relation_size(relname::regclass)) AS total_size FROM pg_stat_user_tables;

