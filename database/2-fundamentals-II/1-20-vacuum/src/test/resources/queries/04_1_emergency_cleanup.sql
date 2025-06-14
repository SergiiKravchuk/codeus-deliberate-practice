-- Exercise 4.1: Emergency VACUUM
-- Description: Perform fastest possible VACUUM when immediate cleanup is needed for table transaction_summary

-- Create a test table with indexes for demonstration
CREATE TABLE IF NOT EXISTS transaction_summary (
    id SERIAL PRIMARY KEY,
    account_id INTEGER,
    daily_total DECIMAL(15,2),
    transaction_count INTEGER,
    summary_date DATE
);

CREATE INDEX IF NOT EXISTS idx_transaction_summary_account ON transaction_summary(account_id);
CREATE INDEX IF NOT EXISTS idx_transaction_summary_date ON transaction_summary(summary_date);

-- Insert test data and create bloat
INSERT INTO transaction_summary (account_id, daily_total, transaction_count, summary_date)
SELECT 
    (random() * 100)::int + 1,
    (random() * 10000)::numeric(15,2),
    (random() * 50)::int + 1,
    CURRENT_DATE - (random() * 30)::int
FROM generate_series(1, 1000);

UPDATE transaction_summary SET daily_total = daily_total + 1;

-- TODO: Implement your solution here

/* 🔍 DETAILED WALKTHROUGH:

   📊 VACUUM speed options:
   - INDEX_CLEANUP TRUE: processes indexes (slower, complete cleanup)
   - INDEX_CLEANUP FALSE: skips index processing (faster, for emergencies)

   💡 Emergency scenarios: when you need immediate dead tuple cleanup
   but can defer index maintenance to later
*/

