-- STEP 1
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
--

-- STEP 2
CREATE TABLE rollups
(
    name                   text primary key,
    event_table_name       text not null,
    event_id_sequence_name text not null,
    last_aggregated_id     bigint default 0
);
--

-- STEP 3
-- Add the 1-day rollup to the rollups table
INSERT INTO rollups (name, event_table_name, event_id_sequence_name)
VALUES ('transactions_1day_rollup', 'transactions', 'transactions_id_seq');
--


--STEP 4
-- Function that retrieves a new records ID range (window) for incremental update from 'rollups' table and preserves it.
CREATE
OR REPLACE FUNCTION incremental_rollup_window(rollup_name text, OUT window_start bigint, OUT window_end bigint)
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
SELECT event_table_name, last_aggregated_id + 1, pg_sequence_last_value(event_id_sequence_name)
INTO table_to_lock, window_start, window_end
FROM rollups
WHERE name = rollup_name FOR UPDATE;

IF
NOT FOUND THEN
        RAISE 'rollup ''%'' is not in the rollups table', rollup_name;
END IF;

    IF
window_end IS NULL THEN
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
RAISE
'release table lock';
EXCEPTION WHEN OTHERS THEN
END;

    /*
     * Remember the end of the window to continue from there next time.
     */
UPDATE rollups
SET last_aggregated_id = window_end
WHERE name = rollup_name;
END;
$function$;


-- Function that incrementally updates target rollup table ('transactions_1day_rollup')
CREATE
OR REPLACE FUNCTION do_transactions_aggregation(OUT start_id bigint, OUT end_id bigint)
    RETURNS record
    LANGUAGE plpgsql
AS $function$
BEGIN
    /* determine which rows we can safely aggregate */
SELECT window_start, window_end
INTO start_id, end_id
FROM incremental_rollup_window('transactions_1day_rollup');

/* exit early if there are no new rows to aggregate */
IF
start_id > end_id THEN RETURN;
END IF;

    /* aggregate the new rows, merge results if the entry already exists */
INSERT INTO transactions_1day (branch_id, transaction_count, transaction_total_amount, period_start)
SELECT branch_id,
       count(tx.id)                           as transaction_count,
       sum(tx.amount)                         AS transaction_total_amount,
       date_trunc('day', transaction_date) as period_start
FROM transactions tx
WHERE tx.id BETWEEN start_id AND end_id
GROUP BY branch_id, date_trunc('day', transaction_date) ON CONFLICT (branch_id, period_start) DO
UPDATE
    SET
        transaction_count = transactions_1day.transaction_count + EXCLUDED.transaction_count,
        transaction_total_amount = transactions_1day.transaction_total_amount + EXCLUDED.transaction_total_amount;

END;
$function$;
--


------------ Playground -----------------
-- Update the transactions_1day rollup table
SELECT * FROM do_transactions_aggregation();

SELECT count(*) FROM transactions;
-- Add new transactions
CALL add_extra_transactions();

-- Query aggregated data from the transactions_1day rollup table
SELECT * FROM transactions_1day ORDER BY period_start DESC, branch_id ASC;

-- Query the same aggregation directly (pay attention to the execution time)
SELECT branch_id, count(tx.id) as transaction_count, sum(tx.amount) AS transaction_total_amount, date_trunc('day', transaction_date) as period_start
FROM transactions tx
GROUP BY branch_id, date_trunc('day', transaction_date)
ORDER BY date_trunc('day', transaction_date) DESC, branch_id ASC;

