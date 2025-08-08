# Database Sharding Deep Dive Theoretical Session

This module contains dive into Database Sharding mechanisms using PostgreSQL Extension - Citus.
Before starting, check the presentation for a deep dive into Database Sharding mechanisms, its benefits, challenges, and
existing implementations and their comparison.
The presentation covers the Citus as well, including its concepts, architecture and benchmark information.

## Practice Prerequisites

1. Open the presentation [Codeus - Sharding_short.pdf](Codeus%20-%20Sharding_short.pdf) and get familiar with main concepts
2. [Optional] Check the recording for more details on the topic. See `🔥│deliberate-practice` channel in our
   Discord: https://discord.com/invite/xMBaqNHvMP

---

## Practice

In the course of this practice, you will transform a simple database into a distributed database for a Multi-Tenant application.

> [!NOTE]  
> DISCLAIMER: This practice is an extended version of the Citus' Multi-Tenant app use case [[1]](#useful-links), 
> some explanations are reused with the DB domain change and extra steps.

### System description

Consider that your system serves transaction processing for different bank branches.

There are 4 tables: branches, accounts, customers and transactions

A simplified DB SQL-schema of a typical finance system:
![codeus_sharding_initial_db_scheme.png](src%2Ftest%2Fresources%2Fexamples%2Fcodeus_sharding_initial_db_scheme.png)

SQL DDLs are provided below:

```postgresql
CREATE TABLE branches
(
    id       SERIAL PRIMARY KEY,
    name     VARCHAR(255) NOT NULL,
    location VARCHAR(255) NOT NULL
);

CREATE TABLE accounts
(
    id           SERIAL PRIMARY KEY,
    branch_id    INT                                                         NOT NULL REFERENCES branches (id),
    customer_id  INT                                                         NOT NULL,
    account_type VARCHAR(20) CHECK (account_type IN ('checking', 'savings')) NOT NULL,
    balance      DECIMAL(15, 2)                                              NOT NULL DEFAULT 0.00,
    created_at   TIMESTAMP                                                            DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE transactions
(
    id                SERIAL PRIMARY KEY,
    account_id        INT                                                                           NOT NULL REFERENCES accounts (id) ON DELETE CASCADE,
    transaction_type  VARCHAR(20) CHECK (transaction_type IN ('deposit', 'withdrawal', 'transfer')) NOT NULL,
    amount            DECIMAL(15, 2)                                                                NOT NULL,
    transaction_date  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    target_account_id INT                                                                           REFERENCES accounts (id) ON DELETE SET NULL
);

CREATE TABLE customers
(
    id         SERIAL PRIMARY KEY,
    first_name VARCHAR(50)         NOT NULL,
    last_name  VARCHAR(50)         NOT NULL,
    email      VARCHAR(100) UNIQUE NOT NULL,
    phone      VARCHAR(20)         NOT NULL,
    address    TEXT                NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

Data between branches is loosely coupled and the only reason you need to read data from multiple branches is **analytics**.
Thus, containing a lot of irrelevant info within one table can introduce a bottleneck at some point in the future.

**Sharding can help to split the data for branches into standalone tables and keeping**

### Practice Exercises:

1. [Exercise 1: Selecting a distribution column and adjusting the schema](src%2Ftest%2Fresources%2F01-single-node-citus-vs-postgresql%2F01-exercise-walkthrough.md)
2. [Exercise 2: single-node PostgreSQL vs single-node Citus](src%2Ftest%2Fresources%2F02-single-node-citus-vs-postgresql%2F02-exercise-walkthrough.md)
3. [Exercise 3: single-node PostgreSQL vs single-node Citus with table sharding](src%2Ftest%2Fresources%2F03-single-node-citus-w-dist-tables-vs-postgresql%2F03-exercise-walkthrough.md)
4. [Exercise 4: single-node PostgreSQL vs multi-node Citus with table sharding](src%2Ftest%2Fresources%2F04-citus-cluster-vs-postgresql%2F04-exercise-walkthrough.md)
5. [Exercise 5: Citus Reference Table concept](src%2Ftest%2Fresources%2F05-citus-reference-tables%2F05-exercise-walkthrough.md)
6. [Exercise 6: Rollup Table concept](src%2Ftest%2Fresources%2F06-rollup-table-concept%2F06-exercise-walkthrough.md)

---

## Useful links:

Here are gathered all useful links from all exercises.

1. Original Multi-Tenant app use-case guide: https://docs.citusdata.com/en/stable/use_cases/multi_tenant.html
2. Citus Coordinator metadata (
   tables): https://docs.citusdata.com/en/stable/develop/api_metadata.html?highlight=pg_dist_placement
3. Example of moving a shard from one node to
   another: https://docs.citusdata.com/en/v13.0/admin_guide/cluster_management.html?highlight=move+shard#make-the-move
4. `citus_move_shard_placement` function
   details: https://docs.citusdata.com/en/v13.0/develop/api_udf.html#citus-move-shard-placement
5. Citus Co-Location concept
   implementation: https://docs.citusdata.com/en/v13.0/sharding/data_modeling.html#table-co-location
6. Citus Table Types: https://docs.citusdata.com/en/v9.3/get_started/concepts.html#table-types
7. Citus Tips Joins between Local and Distributed Postgres
   tables: https://www.citusdata.com/blog/2021/07/02/citus-tips-joins-between-local-and-distributed-postgres-tables
8. Citus changing Local table Join
   Strategy: https://docs.citusdata.com/en/stable/develop/api_guc.html#citus-local-table-join-policy-enum
9. Materialized views vs. Rollup tables: https://www.citusdata.com/blog/2018/10/31/materialized-views-vs-rollup-tables/
10. PostgreSQL Upsert: https://neon.com/postgresql/postgresql-tutorial/postgresql-upsert
11. Scalable Incremental Data
    Aggregation: https://www.citusdata.com/blog/2018/06/14/scalable-incremental-data-aggregation/
12. [Extra] Caching Aggregations with
    Rollups: https://docs.citusdata.com/en/stable/develop/reference_dml.html#caching-aggregations-with-rollups








