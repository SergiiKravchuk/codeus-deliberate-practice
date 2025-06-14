-- Exercise 4.3: Production Maintenance
-- Description: Perform comprehensive VACUUM with detailed logging, statistics update, index processing, and page truncation

-- Create a test table for demonstration
CREATE TABLE IF NOT EXISTS transaction_summary (
    id SERIAL PRIMARY KEY,
    account_id INTEGER,
    daily_total DECIMAL(15,2),
    transaction_count INTEGER,
    summary_date DATE
);

CREATE INDEX IF NOT EXISTS idx_transaction_summary_account ON transaction_summary(account_id);
CREATE INDEX IF NOT EXISTS idx_transaction_summary_date ON transaction_summary(summary_date);

-- Insert test data
INSERT INTO transaction_summary (account_id, daily_total, transaction_count, summary_date)
SELECT 
    (random() * 100)::int + 1,
    (random() * 10000)::numeric(15,2),
    (random() * 50)::int + 1,
    CURRENT_DATE - (random() * 30)::int
FROM generate_series(1, 1000);

-- Create some dead tuples for comprehensive cleanup demonstration
UPDATE transaction_summary SET transaction_count = transaction_count + 1;
DELETE FROM transaction_summary WHERE summary_date < CURRENT_DATE - 30;

-- TODO: Implement your solution here

/* 🔍 DETAILED WALKTHROUGH:
   
   📊 VACUUM options:
   - VERBOSE: shows detailed progress information
   - ANALYZE: updates table statistics for query planner
   - INDEX_CLEANUP TRUE: processes table indexes (default, but explicit)
   - TRUNCATE TRUE: returns empty pages to operating system
   
   💡 Syntax: VACUUM (option1, option2, ...) table_name;
*/

VACUUM (VERBOSE, ANALYZE, INDEX_CLEANUP TRUE, TRUNCATE TRUE) transaction_summary;

