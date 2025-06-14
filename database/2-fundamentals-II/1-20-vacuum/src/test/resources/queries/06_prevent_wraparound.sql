-- Exercise 6: Transaction ID Freezing
-- Description: Prevent transaction wraparound on historical tables (freeze both transactions_history and loans tables)

-- Create a historical transactions table for archived data
DROP TABLE IF EXISTS transactions_history;
CREATE TABLE transactions_history (
    id SERIAL PRIMARY KEY,
    account_id INTEGER,
    transaction_type VARCHAR(20),
    amount DECIMAL(15,2),
    transaction_date DATE,
    archived_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert historical data (simulating 1-2 year old archived transactions)
INSERT INTO transactions_history (account_id, transaction_type, amount, transaction_date)
SELECT 
    (random() * 10)::int + 1,
    CASE (random() * 2)::int
        WHEN 0 THEN 'deposit'
        ELSE 'withdrawal'
    END,
    (random() * 500 + 10)::numeric(15,2),
    CURRENT_DATE - (random() * 730)::int
FROM generate_series(1, 1000);

-- TODO: Implement your solution here
VACUUM FREEZE transactions_history;
VACUUM FREEZE loans;