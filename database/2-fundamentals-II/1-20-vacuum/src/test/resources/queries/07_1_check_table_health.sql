-- Exercise 7.1: Table Health Monitoring
-- Description: Query system statistics to identify tables that need maintenance

-- Create some dead tuples for monitoring demonstration
UPDATE transactions SET amount = amount + 0.01 WHERE id % 5 = 0;
DELETE FROM transactions WHERE id % 25 = 0;

-- TODO: Implement your solution here

/* 🔍 DETAILED WALKTHROUGH:
   
   📊 Table: pg_stat_user_tables
   - relname: table name
   - n_live_tup: count of live (visible) rows
   - n_dead_tup: count of dead (invisible) rows that need cleanup
   - n_tup_upd: number of rows updated
   - n_tup_del: number of rows deleted
*/

-- Solution:
SELECT relname, n_live_tup, n_dead_tup FROM pg_stat_user_tables;

