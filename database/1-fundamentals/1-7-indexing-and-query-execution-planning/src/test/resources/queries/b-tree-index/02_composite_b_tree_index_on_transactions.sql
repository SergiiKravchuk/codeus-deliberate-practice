-- TASK 2: Composite Index on Transactions
--
-- Create a composite B-Tree index to optimize this query.
--
-- Index Name: transaction_amount_and_date_idx
--
-- Analyze the query with EXPLAIN ANALYZE.
-- Query:
SELECT amount, transaction_date FROM transactions WHERE amount > 200 AND transaction_date > NOW() - INTERVAL '10 days';

create index transaction_amount_and_date_idx on transactions(amount, transaction_date);
-- Expected solution:
-- CREATE INDEX transaction_amount_and_date_idx on transactions(amount, transaction_date);