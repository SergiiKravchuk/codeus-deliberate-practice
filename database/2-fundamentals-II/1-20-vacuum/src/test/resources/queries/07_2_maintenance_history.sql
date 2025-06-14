-- Exercise 7.2: VACUUM Activity History
-- Description: Monitor VACUUM frequency and timing for maintenance scheduling

-- Trigger some autovacuum activity for monitoring
UPDATE transactions SET amount = amount + 0.001 WHERE id % 4 = 0;

-- TODO: Implement your solution here

/* 🔍 DETAILED WALKTHROUGH:
   
   📊 Table: pg_stat_user_tables
   - relname: table name
   - vacuum_count: times manual VACUUM was run
   - autovacuum_count: times autovacuum cleaned this table
   - last_vacuum: timestamp of last manual VACUUM
   - last_autovacuum: timestamp of last automatic cleanup
   - last_analyze: timestamp of last statistics update
*/

-- Solution:
SELECT relname, vacuum_count, autovacuum_count, last_vacuum, last_autovacuum FROM pg_stat_user_tables;

