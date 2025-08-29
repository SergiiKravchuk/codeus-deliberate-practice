# Exercise 4: single-node PostgreSQL vs multi-node Citus with table sharding

## Prerequisites:
- [Exercise 3](..%2F03-single-node-citus-w-dist-tables-vs-postgresql%2F03-exercise-walkthrough.md) is completed.
- PostgreSQL (port 5436) and Citus (port 5432) nodes are operational.

---

## Setup worker node in docker:

1. Ensure that the initial Citus instance is a Coordinator - the expected result is `true`:
    ```postgresql
    SELECT citus_is_coordinator();
    ```
    If not, mark it as a Coordinator
    ```postgresql
    SELECT citus_set_coordinator_host('citus_coordinator', 5432);
    ```
    where `citus_coordinator` is docker container name, `5432` - its port **within docker environment**.
    
    > [!NOTE]
    > Coordinator is a Citus node that handles all input queries and routes them to correct worker nodes.
    > Citus Coordinator may become a single point of failure in your system.


2. Run docker compose for worker:
    ```bash
    docker compose -f .\citus-worker-compose.yml up -d
    ```
   OR
   ```bash
       docker compose -f .\citus-worker-compose.yml up -d
   ```
   if you want to use PgAdmin for the worker node which will be available at port 5435.

3. Ensure that coordinator and worker containers are in the same docker network:
    ```bash
    docker network inspect 1-22-sharding_citus_net
    ```
    where `1-22-sharding_citus_net` is the full name of the `citus_net` network defined in the `sharding-startup-compose.yml`.
    
    If not, add worker to the coordinator container's docker network:
    ```bash
    docker network connect 1-22-sharding_citus_net citus_worker
    ```
   
4. [Optional] If you want to use pgadmin for the worker node, repeat steps 2-3 for the `worker_pgadmin` container. 

---

## Setup worker node in the Citus cluster:

1. Add the worker container to cluster as a Citus Worker, run the following command on the `coordinator` node:
    ```postgresql
    SELECT citus_add_node('citus_worker', 5432);
    ``` 
    where `citus_worker` is docker container name, `5432` - its port **within docker environment**.

2. Check that Citus doesn't automatically distribute shards to a newly added workers. Run the following query to confirm that:
    ```postgresql
    SELECT * FROM citus_shards;
    ```
   You should see all 30 shards assigned to node with 'nodename'='citus_coordinator'.

3. Run re-balancing job on the Coordinator node:
    ```postgresql
    SELECT citus_rebalance_start();
    ```

   > [!TIP]
   > Check it out!
   > Check the logs in the worker node container, have you seen anything interesting? Check the related [section](#citus-node-rebalancing-is-done-via-postgresql-replication) below for the answer.

   > [!NOTE]
   >  In case you need to move only specific shards and/or avoid all shards re-balancing, you can use `citus_move_shard_placement` function to a shard from one node to another [[1]](#useful-links), [[2]](#useful-links)

4. Check status of rebalancing and wait until it is finished:
    ```postgresql
    SELECT * FROM citus_rebalance_status();
    ```

5. Check the `citus_shards_on_worker` view on the `worker` node:
   ```postgresql
   SELECT * FROM citus_shards_on_worker;
   ```
   You should see 15 unique shards, meaning that you won't see those shards on another node.

   > [!NOTE]
   >  Those 15 unique shards are "co-located" on one node, meaning that rows in those shards has the same hash of value of the distribution key (e.g. 'branch_id'=hash(3)).
   > See more details in the Official Documentation [[3]](#useful-links).

---

### Check performance of the Citus cluster:

1. Enable all tasks explanation mode for execution plans to see execution plans for all nodes:
   ```postgresql
   SET citus.explain_all_tasks = 1;
   ```

2. Rerun some queries to see if performance has changed for the distributed sharded tables.
   ```sql
    EXPLAIN ANALYZE SELECT tx.id, tx.amount, tx.transaction_date FROM transactions tx
    WHERE tx.branch_id = 3 AND tx.amount > 5000 AND tx.transaction_date > NOW() - INTERVAL '5 days';
    ```
    ```sql
    EXPLAIN ANALYZE SELECT acc.id, acc.customer_id, acc.account_type, tx.id, tx.amount
    FROM accounts acc
             JOIN transactions tx
                  ON tx.branch_id = acc.branch_id
                      AND tx.account_id = acc.id
    WHERE acc.branch_id = 3 AND tx.amount > 5000 AND tx.transaction_date > NOW() - INTERVAL '5 days'
    GROUP BY acc.id, acc.customer_id, acc.account_type, tx.id, tx.amount;
    ```
    ```sql
    EXPLAIN ANALYZE
    SELECT acc.id, acc.customer_id, acc.account_type, tx.id, tx.amount
    FROM accounts acc
             JOIN transactions tx
                  ON tx.branch_id = acc.branch_id
                      AND tx.account_id = acc.id
    WHERE acc.branch_id IN (2,3) AND tx.amount > 5000 AND tx.transaction_date > NOW() - INTERVAL '5 days'
    GROUP BY acc.id, acc.customer_id, acc.account_type, tx.id, tx.amount;
    ```

3. Run the analytical query to see benefits of parallelism:
   ```postgresql
   SELECT b.city, sum(tx.amount) as total_amount
   FROM transactions tx
       JOIN public.branches b on b.id = tx.branch_id
   GROUP BY b.city
   ORDER BY 2 DESC;
   ```

---

## Key Takeaways
- each cluster should have a coordinator node;
- Coordinator node is your single API to communicate with DB, and it manages routing within the Citus cluster;
- Citus doesn't automatically distribute shards to a newly added workers;
- Use `citus_rebalance_start` or `citus_move_shard_placement` to populate newly added workers with shards;
- Citus node re-balancing is done via PostgreSQL replication
- Even a small Citus cluster delivers performance improvement but don't forget about extra latencies in the distributed environment.


## Useful Links:
1. Example of moving a shard from one node to another: https://docs.citusdata.com/en/v13.0/admin_guide/cluster_management.html?highlight=move+shard#make-the-move
2. `citus_move_shard_placement` function details: https://docs.citusdata.com/en/v13.0/develop/api_udf.html#citus-move-shard-placement
3. Citus Co-Location concept implementation: https://docs.citusdata.com/en/v13.0/sharding/data_modeling.html#table-co-location 

---

## Citus node Re-balancing is done via PostgreSQL replication:
   <details> 
      <summary>Example</summary>
      <pre><code class="block">
   LOG:  logical replication apply worker for subscription "citus_shard_move_subscription_10_1" has started 
   LOG:  logical replication worker for subscription "citus_shard_move_subscription_10_1" will stop because he subscription was disabled
   LOG:  logical replication apply worker for subscription "citus_shard_move_subscription_10_2" has started 
   LOG:  logical replication worker for subscription "citus_shard_move_subscription_10_2" will stop because he subscription was disabled
   LOG:  logical replication apply worker for subscription "citus_shard_move_subscription_10_3" has started 
   LOG:  logical replication worker for subscription "citus_shard_move_subscription_10_3" will stop because he subscription was disabled
      </code></pre>
    </details>