-- Exercise 4.2: Space Management Control
-- Description: Perform VACUUM without returning empty pages to the operating system for table transaction_summary

-- Create a test table for demonstration
CREATE TABLE IF NOT EXISTS transaction_summary (
    id SERIAL PRIMARY KEY,
    account_id INTEGER,
    daily_total DECIMAL(15,2),
    transaction_count INTEGER,
    summary_date DATE
);

-- Insert test data
INSERT INTO transaction_summary (account_id, daily_total, transaction_count, summary_date)
SELECT 
    (random() * 100)::int + 1,
    (random() * 10000)::numeric(15,2),
    (random() * 50)::int + 1,
    CURRENT_DATE - (random() * 30)::int
FROM generate_series(1, 1000);

-- Create some dead tuples for this exercise
UPDATE transaction_summary SET daily_total = daily_total * 1.01;
DELETE FROM transaction_summary WHERE id % 10 = 0;

-- TODO: Implement your solution here

/* 🔍 DETAILED WALKTHROUGH:

   📊 VACUUM space management:
   - TRUNCATE TRUE: returns empty pages to OS (slower, saves disk space)
   - TRUNCATE FALSE: keeps empty pages in PostgreSQL (faster, space reusable)

   💡 Use TRUNCATE FALSE when you expect table to grow again soon
   and want to avoid OS-level file operations
*/

