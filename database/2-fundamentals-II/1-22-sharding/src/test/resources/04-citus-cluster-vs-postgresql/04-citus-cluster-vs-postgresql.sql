-- STEP 1
SELECT citus_is_coordinator();

SELECT citus_set_coordinator_host('citus_coordinator', 5432);
--

-- STEP 2
SELECT citus_add_node('citus_worker', 5432);

SELECT * FROM citus_shards

SELECT citus_rebalance_start();
-- AND
SELECT * FROM citus_rebalance_status();

-- Run on both: coordinator and worker
SELECT * FROM citus_shards_on_worker;
--

-- STEP 3
SET citus.explain_all_tasks = 1;

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


-- Analytical query
SELECT b.city, sum(tx.amount) as total_amount
FROM transactions tx
         JOIN public.branches b on b.id = tx.branch_id
GROUP BY b.city
ORDER BY 2 DESC;
--
