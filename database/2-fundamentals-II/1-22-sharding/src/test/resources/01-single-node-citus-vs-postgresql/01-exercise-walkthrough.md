# Exercise 1: Selecting a distribution column and adjusting the schema

_branch id_ is a great distribution column candidate for this system. Therefore, it can be used to shard tables into
smaller unique slices (shards) for each branch.
We can tell Citus to use this column to read and write rows to the same node when the rows are marked for the same
branch.

At the moment, not all tables are ready to be distributed because some are missing the distribution column - _branch
id_.

Even in a single-machine database it can be useful to denormalize tables with the addition of _branch id_, whether it be
for row-level security or for additional indexing. The extra benefit, as we saw, is that including the extra column
helps for multi-machine scaling as well.

The schema we have created so far uses a separate id column as primary key for each table. Citus requires that primary
and foreign key constraints include the distribution column. This requirement makes enforcing these constraints much
more efficient in a distributed environment as **only a single node has to be checked to guarantee them**.

In SQL, this requirement translates to making primary and foreign keys composite by including `branch_id`. This is
compatible with the multi-tenant case because what we really need there is to ensure uniqueness on a per-tenant basis.

Putting it all together, here are the changes which prepare the tables for distribution by `branch_id`.

```postgresql
CREATE TABLE branches -- remains the same
(
    id       SERIAL PRIMARY KEY,
    name     VARCHAR(255) NOT NULL,
    location VARCHAR(255) NOT NULL
);


CREATE TABLE accounts
(
    id           SERIAL,        -- was: PRIMARY KEY
    branch_id    INT                                                         NOT NULL REFERENCES branches (id),
    customer_id  INT                                                         NOT NULL,
    account_type VARCHAR(20) CHECK (account_type IN ('checking', 'savings')) NOT NULL,
    balance      DECIMAL(15, 2)                                              NOT NULL DEFAULT 0.00,
    created_at   TIMESTAMP                                                            DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (branch_id, id) -- added
);

CREATE TABLE transactions
(
    id                SERIAL,                                                                                 -- was: PRIMARY KEY
    branch_id         INT                                                                           NOT NULL,
    account_id        INT                                                                           NOT NULL, -- was: REFERENCES accounts (id) ON DELETE CASCADE
    transaction_type  VARCHAR(20) CHECK (transaction_type IN ('deposit', 'withdrawal', 'transfer')) NOT NULL,
    amount            DECIMAL(15, 2)                                                                NOT NULL,
    transaction_date  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    target_account_id INT,                                                                                    -- was: REFERENCES accounts (id) ON DELETE SET NULL
    PRIMARY KEY (branch_id, id),                                                                              -- added
    FOREIGN KEY (branch_id, account_id)                                                                       -- added
        REFERENCES accounts (branch_id, id) ON DELETE CASCADE,
    FOREIGN KEY (branch_id, target_account_id)                                                                -- added
        REFERENCES accounts (branch_id, id)                                                                   -- was: including "ON DELETE SET NULL" (see note below)
);


CREATE TABLE customers -- remains the same
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

> [!IMPORTANT]  
> SET NULL or SET DEFAULT is not supported in ON DELETE operation when distribution key is included in the foreign key constraint.
> In our case, the foreign key for transaction table to accounts' ta target_account_id should be adjusted.

Once the table schema is updated, you will need to update your queries with filtering by the distribution column.

---

## Key Takeaways
- Add a distribution to all tables you want to distribute, even if table originally didn't have it;
- Make primary keys to include the distribution column value for tables that you plan to distribute, it will optimize query execution;
- Make foreign keys to include the distribution column to conform the primary keys change.
- Include the distribution column to queries.