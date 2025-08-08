# Exercise 6: Rollup Table concept

## Prerequisites:
- [Exercise 5](..%2F05-citus-reference-tables%2F05-exercise-walkthrough.md) is completed.
- Citus Coordinator (port 5432) and Worker (port 5434) nodes are operational.

---

> [!NOTE]
> All queries in this exercise should be executed on the `coordinator` node. 

Rollup Table is a pre-aggregated table that is incrementally updated using extra functions. In contrast to views, rollups typically doesn't re-process all the underlying data in order to get fresh data [[1]](#useful-links). That is achieved by the upsert operation (`INSERT ... ON CONFLICT ... DO UPDATE`), which gives a combination of INSERT and UPDATE operations within one INSERT query [[2]](#useful-links).

Let's create a rollup table for an analytical query for the 'transactions' table from previous exercises.

---
## Creating an incremental rollup table

1. Define a 'transactions_1day' rollup table for the target 'transactions' table and make it a Citus distributed table using the `branch_id` column:
    ```postgresql
    DROP TABLE IF EXISTS transactions_1day;
    CREATE TABLE transactions_1day
    (
        branch_id                INT,
        transaction_count        BIGINT,
        transaction_total_amount DECIMAL(15, 2),
        period_start             TIMESTAMP,
        PRIMARY KEY (branch_id, period_start) -- <-- our unique constraint columns for rollup
    );
    
    -- Citus only: distribute the table by branch_id
    SELECT create_distributed_table('transactions_1day', 'branch_id');
    ```
   
2. Define a 'rollups' support table which will used to track positions of incremental updates for any rollup table:
   ```postgresql
   CREATE TABLE rollups (
       name text primary key,
       event_table_name text not null,
       event_id_sequence_name text not null,
       last_aggregated_id bigint default 0
   );
   ```

3. Add a record for 'transactions_1day' and its target table 'transactions' to 'rollups':
   ```postgresql
   
   -- Add the 1-minute rollup to the rollups table
   INSERT INTO rollups (name, event_table_name, event_id_sequence_name)
   VALUES ('transactions_1day_rollup', 'transactions', 'transactions_id_seq');
   ```

   > [!NOTE]
   > `transactions_id_seq` is used here, it is a default name of the auto-incrementing SEQUENCE, and it is constructed: "<table_name>_<serial_column name>_seq".

4. Define special support functions to achieve incremental rollup table updates:
   ```postgresql
   -- Function that retrieves a new records ID range (window) for incremental update from 'rollups' table and preserves it.
   CREATE OR REPLACE FUNCTION incremental_rollup_window(rollup_name text, OUT window_start bigint, OUT window_end bigint)
   RETURNS record
   LANGUAGE plpgsql
   AS $function$
   DECLARE
       table_to_lock regclass;
   BEGIN
       /*
        * Perform aggregation from the last aggregated ID + 1 up to the last committed ID.
        * We do a SELECT .. FOR UPDATE on the row in the rollup table to prevent
        * aggregations from running concurrently.
        */
       SELECT event_table_name, last_aggregated_id+1, pg_sequence_last_value(event_id_sequence_name)
       INTO table_to_lock, window_start, window_end
       FROM rollups
       WHERE name = rollup_name FOR UPDATE;
   
       IF NOT FOUND THEN
           RAISE 'rollup ''%'' is not in the rollups table', rollup_name;
       END IF;
   
       IF window_end IS NULL THEN
           /* sequence was never used */
           window_end := 0;
           RETURN;
       END IF;
   
       /*
        * Play a little trick: We very briefly lock the table for writes in order to
        * wait for all pending writes to finish. That way, we are sure that there are
        * no more uncommitted writes with a identifier lower or equal to window_end.
        * By throwing an exception, we release the lock immediately after obtaining it
        * such that writes can resume.
        */
       BEGIN
           EXECUTE format('LOCK %s IN EXCLUSIVE MODE', table_to_lock);
           RAISE 'release table lock';
       EXCEPTION WHEN OTHERS THEN
       END;
   
       /*
        * Remember the end of the window to continue from there next time.
        */
       UPDATE rollups SET last_aggregated_id = window_end WHERE name = rollup_name;
   END;
   $function$;

   ```
   
   ```postgresql
   -- Function that incrementatlly updates target rollup table ('transactions_1day_rollup')
    CREATE OR REPLACE FUNCTION do_transactions_aggregation(OUT start_id bigint, OUT end_id bigint)
        RETURNS record
        LANGUAGE plpgsql
    AS $function$
    BEGIN
        /* determine which rows we can safely aggregate */
        SELECT window_start, window_end INTO start_id, end_id
        FROM incremental_rollup_window('transactions_1day_rollup');
    
        /* exit early if there are no new rows to aggregate */
        IF start_id > end_id THEN RETURN; END IF;
    
        /* aggregate the new rows, merge results if the entry already exists */
        INSERT INTO transactions_1day (branch_id, transaction_count, transaction_total_amount, period_start)
        SELECT branch_id, count(tx.id) as transaction_count, sum(tx.amount) AS transaction_total_amount, date_trunc('day', transaction_date) as period_start
        FROM transactions tx
        WHERE tx.id BETWEEN start_id AND end_id
        GROUP BY branch_id, date_trunc('day', transaction_date)
        ON CONFLICT (branch_id, period_start) DO UPDATE
            SET
                transaction_count = transactions_1day.transaction_count + EXCLUDED.transaction_count,
                transaction_total_amount = transactions_1day.transaction_total_amount + EXCLUDED.transaction_total_amount;
    
    
    END;
    $function$;
   ```
5. Play around the rollup table and compare with direct query:
   Run initial aggregation:
   ```postgresql
   SELECT * FROM do_transactions_aggregation();
   ```
   
   Check the results:
   ```postgresql
   SELECT * FROM transactions_1day;
   ```

   Add new transactions to the main 'transactions' table:
   ```postgresql
   CALL add_extra_transactions();
   ```

   Run incremental aggregation and check the 'transactions_1day' rollup table (pay attention to the execution time):
   ```postgresql
   SELECT * FROM do_transactions_aggregation();
   SELECT * FROM transactions_1day;
   ```

   Query the same aggregation directly (pay attention to the execution time)
   ```postgresql
   SELECT branch_id, count(tx.id) as transaction_count, sum(tx.amount) AS transaction_total_amount, date_trunc('day', transaction_date) as period_start
   FROM transactions tx
   GROUP BY branch_id, date_trunc('day', transaction_date)
   ORDER BY date_trunc('day', transaction_date) DESC, branch_id;
   ```

---

## Key Takeaways
- Rollup Table is a pre-aggregated table that is incrementally updated using extra functions;
- Rollup Table utilizes UPSERT operation;
- Rollup Table is a great option for aggregated analytics or real-time dashboards.


## Useful Links:
1. Materialized views vs. Rollup tables: https://www.citusdata.com/blog/2018/10/31/materialized-views-vs-rollup-tables/
2. PostgreSQL Upsert: https://neon.com/postgresql/postgresql-tutorial/postgresql-upsert
3. Scalable Incremental Data Aggregation: https://www.citusdata.com/blog/2018/06/14/scalable-incremental-data-aggregation/