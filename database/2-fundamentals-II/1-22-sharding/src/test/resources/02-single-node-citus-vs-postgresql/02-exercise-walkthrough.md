# Exercise 2: single-node PostgreSQL vs single-node Citus

## Start PostgreSQL and Citus containers
Start single-node PostgreSQL and single-node Citus:

```bash
docker compose -f .\sharding-startup-compose.yml up -d
```
OR
```bash
docker compose -f .\sharding-startup-with-pgadmin-compose.yml up -d
```

> [!NOTE]  
> If you want to use PgAdmin, the second option will deploy additional PgAdmin containers at ports 5433 and 5437.
> Yes, Citus work with PgAdmin, this is one of the benefits of being an extension.
> Otherwise, you can set up DB connections in the Intellij Idea.


Both containers have the same init step which creates:

- 15 branches
- 50k customers
- ~100k accounts
- ~1mil transactions

> [!NOTE]  
> The init script contains static seed for random operation which means that data between runs/containers should be the
> same.

> [!TIP]  
> If you want to reset a container completely you can run `down` command with `-v` flag to remove all volumes (includes
> table data and configs).

```bash
docker compose -f <your-compose-file> down -v
```

---

## Compare performance:

1. Run the following queries in both `postgres` and `coordinator` containers:

    ```sql
    -- Select transactions from branch 3 that have amount over 5k
    SELECT tx.id, tx.amount, tx.transaction_date FROM transactions tx
    WHERE tx.branch_id = 3 AND tx.amount > 5000 AND tx.transaction_date > NOW() - INTERVAL '5 days';
    ```
    ```sql
    -- Select accounts from branch 3 that have transactions over 5k for last 5 days
    SELECT acc.id, acc.customer_id, acc.account_type, tx.id, tx.amount
    FROM accounts acc
             JOIN transactions tx
                  ON tx.branch_id = acc.branch_id
                      AND tx.account_id = acc.id
    WHERE acc.branch_id = 3 AND tx.amount > 5000 AND tx.transaction_date > NOW() - INTERVAL '5 days'
    GROUP BY acc.id, acc.customer_id, acc.account_type, tx.id, tx.amount;
    ```
    ```sql
    -- Select accounts from branches 2 and 3 that have transactions over 5k for last 5 days
    SELECT acc.id, acc.customer_id, acc.account_type, tx.id, tx.amount
    FROM accounts acc
             JOIN transactions tx
                  ON tx.branch_id = acc.branch_id
                      AND tx.account_id = acc.id
    WHERE acc.branch_id IN (2,3) AND tx.amount > 5000 AND tx.transaction_date > NOW() - INTERVAL '5 days'
    GROUP BY acc.id, acc.customer_id, acc.account_type, tx.id, tx.amount;
    ```

   > [!NOTE]
   > Pay attention that all queries include filtering by the distribution column - `branch_id`. 
   > This would be required to navigate to the correct DB cluster's node and shard.

2. Run the same queries with `EXPLAIN ANALYZE` to see if Execution Plans have difference.

   > [!NOTE]  
   > Citus extends the PostgreSQL's `EXPLAIN ANALYZE` with additional metrics for each task. They are not visible, because there is only one task.

---

## Key Takeaways
- A single Citus node without table sharding has generally the same performance metrics as a single PostgreSQL node.  