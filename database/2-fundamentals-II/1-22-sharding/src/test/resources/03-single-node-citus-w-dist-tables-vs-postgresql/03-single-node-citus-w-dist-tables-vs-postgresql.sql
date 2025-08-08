-- STEP 1
SET citus.shard_count TO 10;

SELECT create_distributed_table('accounts',     'branch_id');

SELECT create_distributed_table('branches',     'id');
SELECT create_distributed_table('accounts',     'branch_id');
SELECT create_distributed_table('transactions', 'branch_id');
--

-- STEP 2
SELECT * FROM pg_dist_partition;
-- OR
SELECT * FROM citus_tables;

SELECT * FROM pg_dist_shard;

SELECT * FROM citus_shards_on_worker;
--
-- STEP 3
-- Select transactions from branch 3 that have amount over 5k
EXPLAIN ANALYZE SELECT tx.id, tx.amount, tx.transaction_date
FROM transactions tx
WHERE tx.branch_id = 3
  AND tx.amount > 5000
  AND tx.transaction_date > NOW() - INTERVAL '5 days';


-- Select accounts from branch 3 that have transactions over 5k for last 5 days
EXPLAIN ANALYZE SELECT acc.id, acc.customer_id, acc.account_type, tx.id, tx.amount
FROM accounts acc
         JOIN transactions tx
              ON tx.branch_id = acc.branch_id
                  AND tx.account_id = acc.id
WHERE acc.branch_id = 3
  AND tx.amount > 5000
  AND tx.transaction_date > NOW() - INTERVAL '5 days'
GROUP BY acc.id, acc.customer_id, acc.account_type, tx.id, tx.amount;


-- Select accounts from branches 2 and 3 that have transactions over 5k for last 5 days
EXPLAIN ANALYZE SELECT acc.id, acc.customer_id, acc.account_type, tx.id, tx.amount
FROM accounts acc
         JOIN transactions tx
              ON tx.branch_id = acc.branch_id
                  AND tx.account_id = acc.id
WHERE acc.branch_id IN (2, 3)
  AND tx.amount > 5000
  AND tx.transaction_date > NOW() - INTERVAL '5 days'
GROUP BY acc.id, acc.customer_id, acc.account_type, tx.id, tx.amount;


--
