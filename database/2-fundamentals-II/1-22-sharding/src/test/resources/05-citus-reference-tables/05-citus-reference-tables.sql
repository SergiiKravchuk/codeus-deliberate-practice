-- STEP 1
SET citus.explain_all_tasks = 1;
--

-- STEP 2
EXPLAIN ANALYSE
SELECT acc.id, acc.customer_id, acc.account_type, (c.first_name || ' ' || c.last_name) as full_name, c.email, tx.id, tx.amount
FROM accounts acc
         JOIN transactions tx ON tx.branch_id = acc.branch_id AND tx.account_id = acc.id
         JOIN public.customers c on acc.customer_id = c.id
WHERE acc.branch_id = 3 AND tx.amount > 5000 AND tx.transaction_date > NOW() - INTERVAL '5 days'
GROUP BY acc.id, acc.customer_id, acc.account_type, (c.first_name || ' ' || c.last_name), c.email, tx.id, tx.amount;
--

-- STEP 3
SELECT create_reference_table('customers');
--

-- STEP 4
SELECT * FROM pg_dist_partition;
-- OR
SELECT * FROM citus_tables;
--

-- STEP 5
SELECT * FROM citus_shards;
--

-- STEP 6
-- A copy of the query above (just to feel the flow)
EXPLAIN ANALYSE
SELECT acc.id, acc.customer_id, acc.account_type, (c.first_name || ' ' || c.last_name) as full_name, c.email, tx.id, tx.amount
FROM accounts acc
         JOIN transactions tx ON tx.branch_id = acc.branch_id AND tx.account_id = acc.id
         JOIN public.customers c on acc.customer_id = c.id
WHERE acc.branch_id = 3 AND tx.amount > 5000 AND tx.transaction_date > NOW() - INTERVAL '5 days'
GROUP BY acc.id, acc.customer_id, acc.account_type, (c.first_name || ' ' || c.last_name), c.email, tx.id, tx.amount;
--