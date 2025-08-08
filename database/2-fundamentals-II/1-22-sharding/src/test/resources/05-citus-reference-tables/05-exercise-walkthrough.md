# Exercise 5: Citus Reference Table concept

## Prerequisites:
- [Exercise 4](..%2F04-citus-cluster-vs-postgresql%2F04-exercise-walkthrough.md) is completed.
- Citus Coordinator (port 5432) and Worker (port 5434) nodes are operational.

---

Citus has 3 types of tables:
- local - a table that is not partitioned and located only on a coordinator node;
- distributed - a table that is partitioned and distributed as partitions across all nodes in a Citus cluster;
- reference - a table that is not partitioned and distributed as a full copy across all worker nodes in a Citus cluster;

> [!TIP]
> Want to know more? Check out the official documentation [[1]](#useful-links)

We have already worked with "local" - we kept 'customer' table on the coordinator only, "distributed" - 'branches', 'accounts', 'transactions'.

As mentioned above, reference tables are special kind of tables that are replicated in full to every worker nodes. The replication happens only once at the start of the cluster. 
Primary use of reference tables is distribute tables that do not fit to the current distribution plan (distribution column is not applicable for the table) and achieve performant join operations [[2]](#useful-links).
Still, be careful with larger tables, making them reference tables may have a noticeable impact on your cluster start-up performance and storage.

---

Let's assume that users of our system wants to see more details on while doing some queries.
Thus, we need to join details from the 'customer' table with other data.

First, let's see performance metrics of 'customer' table being "local" table
1. Enable all tasks explanation mode for execution plans to see the more details by executing the following query on the `coordinator` node:
   ```postgresql
   SET citus.explain_all_tasks = 1;
   ```

2. Execute the following query and examine performance metrics:
    ```postgresql
    EXPLAIN ANALYSE
    SELECT acc.id, acc.customer_id, acc.account_type, (c.first_name || ' ' || c.last_name) as full_name, c.email, tx.id, tx.amount
    FROM accounts acc
    JOIN transactions tx ON tx.branch_id = acc.branch_id AND tx.account_id = acc.id
    JOIN public.customers c on acc.customer_id = c.id
    WHERE acc.branch_id = 3 AND tx.amount > 5000 AND tx.transaction_date > NOW() - INTERVAL '5 days'
    GROUP BY acc.id, acc.customer_id, acc.account_type, (c.first_name || ' ' || c.last_name), c.email, tx.id, tx.amount;
    ```
    In the execution plan, you should see the "Distributed Subplan" section, which contains info about data that are transmitted between nodes for Join operation, typically this is caused by joining with local table.
    <details> 
    <summary>Example: Execution Plan BEFORE Reference Table</summary>
        <pre><code class="block">
    Custom Scan (Citus Adaptive)  (cost=0.00..0.00 rows=0 width=0) (actual time=95.725..95.764 rows=604 loops=1)
      ->  Distributed Subplan 23_1
            Subplan Duration: 56.57 ms
            Intermediate Data Size: 2995 kB
            Result destination: Send to 1 nodes
            ->  Seq Scan on customers c  (cost=0.00..1166.00 rows=50000 width=44) (actual time=0.016..6.094 rows=50000 loops=1)
            Planning Time: 0.000 ms
            Execution Time: 8.687 ms
      Task Count: 1
      Tuple data received from nodes: 41 kB
      Tasks Shown: All
      ->  Task
            Tuple data received from node: 41 kB
            Node: host=master port=5432 dbname=postgres
            ->  Group  (cost=4616.68..4624.69 rows=83 width=276) (actual time=87.182..90.849 rows=604 loops=1)
    "              Group Key: acc.id, acc.customer_id, acc.account_type, ((((intermediate_result.first_name)::text || ' '::text) || (intermediate_result.last_name)::text)), intermediate_result.email, tx.id, tx.amount"
                  ->  Gather Merge  (cost=4616.68..4623.41 rows=49 width=276) (actual time=87.180..90.632 rows=604 loops=1)
                        Workers Planned: 1
                        Workers Launched: 1
                        ->  Group  (cost=3616.67..3617.89 rows=49 width=276) (actual time=78.374..78.496 rows=302 loops=2)
    "                          Group Key: acc.id, acc.customer_id, acc.account_type, ((((intermediate_result.first_name)::text || ' '::text) || (intermediate_result.last_name)::text)), intermediate_result.email, tx.id, tx.amount"
                              ->  Sort  (cost=3616.67..3616.79 rows=49 width=276) (actual time=78.370..78.390 rows=302 loops=2)
    "                                Sort Key: acc.id, acc.customer_id, acc.account_type, ((((intermediate_result.first_name)::text || ' '::text) || (intermediate_result.last_name)::text)), intermediate_result.email, tx.id, tx.amount"
                                    Sort Method: quicksort  Memory: 62kB
                                    Worker 0:  Sort Method: quicksort  Memory: 39kB
                                    ->  Hash Join  (cost=793.65..3615.29 rows=49 width=276) (actual time=66.104..77.966 rows=302 loops=2)
                                          Hash Cond: (tx.account_id = acc.id)
                                          ->  Parallel Seq Scan on transactions_102036 tx  (cost=0.00..2819.69 rows=325 width=18) (actual time=0.615..12.232 rows=302 loops=2)
                                                Filter: ((amount > '5000'::numeric) AND (branch_id = 3) AND (transaction_date > (now() - '5 days'::interval)))
                                                Rows Removed by Filter: 64815
                                          ->  Hash  (cost=780.49..780.49 rows=1053 width=474) (actual time=65.456..65.459 rows=6983 loops=2)
                                                Buckets: 8192 (originally 2048)  Batches: 1 (originally 1)  Memory Usage: 722kB
                                                ->  Hash Join  (cost=233.58..780.49 rows=1053 width=474) (actual time=42.357..60.781 rows=6983 loops=2)
                                                      Hash Cond: (intermediate_result.id = acc.customer_id)
                                                      ->  Function Scan on read_intermediate_result intermediate_result  (cost=0.00..438.78 rows=6443 width=458) (actual time=39.368..42.874 rows=50000 loops=2)
                                                      ->  Hash  (cost=146.29..146.29 rows=6983 width=20) (actual time=2.965..2.966 rows=6983 loops=2)
                                                            Buckets: 8192  Batches: 1  Memory Usage: 433kB
                                                            ->  Seq Scan on accounts_102026 acc  (cost=0.00..146.29 rows=6983 width=20) (actual time=0.062..1.219 rows=6983 loops=2)
                                                                  Filter: (branch_id = 3)
                Planning Time: 0.690 ms
                Execution Time: 91.911 ms
    Planning Time: 1.014 ms
    Execution Time: 152.404 ms
    </code></pre>
    </details>
3. Make the 'customer' table a "reference" table:
    ```postgresql
    SELECT create_reference_table('customers');
    ```
4. Verify that 'customer' is in the list of Citus "shared" tables:
    ```postgresql
    SELECT * FROM pg_dist_partition;
    -- OR
    SELECT * FROM citus_tables;
    ```
5. Check that reference table is stored as one shard on each node (`coordinator` and `worker`): 
    ```postgresql
    SELECT * FROM citus_shards;
   ```
6. Observe the performance changes by executing the same query:
    ```postgresql
    EXPLAIN ANALYSE
    SELECT acc.id, acc.customer_id, acc.account_type, (c.first_name || ' ' || c.last_name) as full_name, c.email, tx.id, tx.amount
    FROM accounts acc
    JOIN transactions tx ON tx.branch_id = acc.branch_id AND tx.account_id = acc.id
    JOIN public.customers c on acc.customer_id = c.id
    WHERE acc.branch_id = 3 AND tx.amount > 5000 AND tx.transaction_date > NOW() - INTERVAL '5 days'
    GROUP BY acc.id, acc.customer_id, acc.account_type, (c.first_name || ' ' || c.last_name), c.email, tx.id, tx.amount;
    ```
    Now, the "Distributed Subplan" section should be gone because each worker have a local copy of the 'customers' table and can perform join operation locally. 
    <details> 
    <summary>Example: Execution Plan AFTER Reference Table</summary>
        <pre><code class="block">
    Custom Scan (Citus Adaptive)  (cost=0.00..0.00 rows=0 width=0) (actual time=33.660..33.696 rows=604 loops=1)
      Task Count: 1
      Tuple data received from nodes: 41 kB
      Tasks Shown: All
      ->  Task
            Tuple data received from node: 41 kB
            Node: host=master port=5432 dbname=postgres
            ->  Group  (cost=4321.72..4374.86 rows=553 width=79) (actual time=26.293..29.702 rows=604 loops=1)
    "              Group Key: acc.id, acc.customer_id, acc.account_type, ((((c.first_name)::text || ' '::text) || (c.last_name)::text)), c.email, tx.id, tx.amount"
                  ->  Gather Merge  (cost=4321.72..4366.40 rows=325 width=79) (actual time=26.291..29.438 rows=604 loops=1)
                        Workers Planned: 1
                        Workers Launched: 1
                        ->  Group  (cost=3321.71..3329.83 rows=325 width=79) (actual time=23.422..23.569 rows=302 loops=2)
    "                          Group Key: acc.id, acc.customer_id, acc.account_type, ((((c.first_name)::text || ' '::text) || (c.last_name)::text)), c.email, tx.id, tx.amount"
                              ->  Sort  (cost=3321.71..3322.52 rows=325 width=79) (actual time=23.417..23.442 rows=302 loops=2)
    "                                Sort Key: acc.id, acc.customer_id, acc.account_type, ((((c.first_name)::text || ' '::text) || (c.last_name)::text)), c.email, tx.id, tx.amount"
                                    Sort Method: quicksort  Memory: 65kB
                                    Worker 0:  Sort Method: quicksort  Memory: 36kB
                                    ->  Nested Loop  (cost=233.87..3308.15 rows=325 width=79) (actual time=3.326..22.985 rows=302 loops=2)
                                          ->  Hash Join  (cost=233.58..3054.11 rows=325 width=26) (actual time=3.269..21.991 rows=302 loops=2)
                                                Hash Cond: (tx.account_id = acc.id)
                                                ->  Parallel Seq Scan on transactions_102036 tx  (cost=0.00..2819.69 rows=325 width=18) (actual time=0.448..19.021 rows=302 loops=2)
                                                      Filter: ((amount > '5000'::numeric) AND (branch_id = 3) AND (transaction_date > (now() - '5 days'::interval)))
                                                      Rows Removed by Filter: 64815
                                                ->  Hash  (cost=146.29..146.29 rows=6983 width=20) (actual time=2.748..2.749 rows=6983 loops=2)
                                                      Buckets: 8192  Batches: 1  Memory Usage: 433kB
                                                      ->  Seq Scan on accounts_102026 acc  (cost=0.00..146.29 rows=6983 width=20) (actual time=0.023..1.196 rows=6983 loops=2)
                                                            Filter: (branch_id = 3)
                                          ->  Index Scan using customers_pkey_102042 on customers_102042 c  (cost=0.29..0.78 rows=1 width=44) (actual time=0.003..0.003 rows=1 loops=604)
                                                Index Cond: (id = acc.customer_id)
                Planning Time: 0.491 ms
                Execution Time: 29.962 ms
    Planning Time: 0.541 ms
    Execution Time: 33.755 ms
        </code></pre>
    </details>


   > [!TIP]
   > Think about it!
   > Why can the `Index Scan` operation be seen for a join with a reference table and not for a join with a local table?
   > For answer see the corresponding section below: [Citus Joins between Local and Distributed tables](#citus-joins-between-local-and-distributed-tables).

---

## Key Takeaways
- In case you need to join one table that is not applicable for the current distribution plan (e.g. each row cannot be assigned to one distribution column value) and that table is small enough - use reference tables.
- Reference table data is transmitted to every worker nodes once but this takes more memory usage on nodes.
- Join operations between distributed and local tables have poor performance because of extra network traffic between coordinator and worker is added for each such query.
- Joins operations between reference and distributed table happen locally on each worker and only result is transmitted back to the coordinator.
- Reference table is transmitted with indexes, therefore indexes can be used during join operations.


## Useful Links:
1. Citus Table Types: https://docs.citusdata.com/en/v9.3/get_started/concepts.html#table-types
2. Citus Tips Joins between Local and Distributed Postgres tables: https://www.citusdata.com/blog/2021/07/02/citus-tips-joins-between-local-and-distributed-postgres-tables
3. Citus changing Local table Join Strategy: https://docs.citusdata.com/en/stable/develop/api_guc.html#citus-local-table-join-policy-enum

---

## Citus Joins between Local and Distributed tables:
Citus has 2 strategies of joining local and distributed tables. By default, strategy is chosen in runtime depending on the size of dataset for join from local and distributed tables [[2]](#useful-links). Still, you can explicitly set any strategy using `citus.local_table_join_policy` setting [[3]](#useful-links). Both strategies cannot use indexes for the transmitted tables, because transmitted table is basically a stream of rows (consider it as the result of `SELECT * FROM customers`), not full table, so index is not available.
