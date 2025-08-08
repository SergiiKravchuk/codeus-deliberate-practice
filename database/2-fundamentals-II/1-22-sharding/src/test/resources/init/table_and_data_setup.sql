--===========================================>>TABLES<<===========================================--
CREATE TABLE branches
(
    id       SERIAL PRIMARY KEY,
    name     VARCHAR(255) NOT NULL,
    location VARCHAR(255) NOT NULL,
    city     VARCHAR(255) NOT NULL
);

CREATE TABLE accounts
(
    id           SERIAL,
    branch_id    INT                                                         NOT NULL,
    customer_id  INT                                                         NOT NULL,
    account_type VARCHAR(20) CHECK (account_type IN ('checking', 'savings')) NOT NULL,
    balance      DECIMAL(15, 2)                                              NOT NULL DEFAULT 0.00,
    created_at   TIMESTAMP                                                            DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (branch_id, id),
    FOREIGN KEY (branch_id) REFERENCES branches (id)
);

CREATE TABLE transactions
(
    id                SERIAL,
    branch_id         INT                                                                           NOT NULL,
    account_id        INT                                                                           NOT NULL,
    transaction_type  VARCHAR(20) CHECK (transaction_type IN ('deposit', 'withdrawal', 'transfer')) NOT NULL,
    amount            DECIMAL(15, 2)                                                                NOT NULL,
    transaction_date  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    target_account_id INT,
    PRIMARY KEY (branch_id, id),
    FOREIGN KEY (branch_id, account_id) REFERENCES accounts (branch_id, id) ON DELETE CASCADE,
    FOREIGN KEY (branch_id, target_account_id) REFERENCES accounts (branch_id, id)
);


-- Extra
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

--===========================================>>PROCEDURES<<===========================================--
-- 0. Create a master driver procedure
CREATE OR REPLACE PROCEDURE populate_all()
    LANGUAGE plpgsql
AS
$$
BEGIN
    CALL populate_branches();
    CALL populate_customers();
    CALL populate_accounts();
    CALL populate_transactions(1000000, 0.456, 30);
END;
$$;

-- 1) Populate branches (15 rows)
CREATE OR REPLACE PROCEDURE populate_branches()
    LANGUAGE plpgsql
AS
$$
DECLARE
    total_branches INT := 15;
    cities TEXT[] := ARRAY['Kyiv', 'Lviv', 'Kolomyya'];
    total_cities INT := array_length(cities, 1);
BEGIN
    INSERT INTO branches(name, location, city)
    SELECT 'Branch ' || i,
           'Location ' || i,
           cities[floor(random() * total_cities + 1)::INT]
    FROM generate_series(1, total_branches) AS s(i);
END;
$$;

-- 2) Populate customers (50_000 rows) deterministically
CREATE OR REPLACE PROCEDURE populate_customers()
    LANGUAGE plpgsql
AS
$$
BEGIN
    -- deterministic random seed for any future random() calls
    PERFORM setseed(0.123);

    INSERT INTO customers(first_name, last_name, email, phone, address)
    SELECT 'First' || i,
           'Last' || i,
           'user' || i || '@example.com',
           -- pad phones to 10 digits
           LPAD((1000000000 + (i % 900000000))::text, 10, '0'),
           'Address ' || i
    FROM generate_series(1, 50000) AS s(i);
END;
$$;

-- 3) Populate accounts (≥50_000 rows; 1–3 accounts per customer)
CREATE OR REPLACE PROCEDURE populate_accounts()
    LANGUAGE plpgsql
AS
$$
BEGIN
    PERFORM setseed(0.456);
    WITH cust AS (SELECT id AS customer_id
                  FROM customers),
         gen_counts AS (
             -- decide 1–3 accounts per customer
             SELECT customer_id,
                    floor(random() * 3 + 1)::int AS acct_count
             FROM cust),
         to_insert AS (
             -- explode each customer into N rows
             SELECT generate_series(1, acct_count) AS seq,
                    customer_id
             FROM gen_counts),
         numbered AS (
             -- assign a row_number for a synthetic account id and pick branch uniformly
             SELECT row_number() OVER (ORDER BY customer_id, seq)::int          AS acct_id,
                    round((random() * 14 + 1)::numeric)                         AS branch_id,
                    customer_id,
                    CASE WHEN random() < 0.5 THEN 'checking' ELSE 'savings' END AS account_type,
                    -- random balance between 100.00 and 10_100.00
                    round((random() * 10000 + 100)::numeric, 2)                 AS balance
             FROM to_insert)
    INSERT
    INTO accounts(id, branch_id, customer_id, account_type, balance)
    SELECT acct_id, branch_id, customer_id, account_type, balance
    FROM numbered;
END;
$$;

-- 4) Populate transactions (1_000_000 rows; 30% in branches 1–3, over last 30 days; 30% txs from 1-3 branch are held by 5% of accounts;)
CREATE OR REPLACE PROCEDURE populate_transactions(IN number_transactions integer, IN seed real, IN tx_day_range integer)
    LANGUAGE plpgsql
AS
$$
DECLARE
    total_transactions INT   := number_transactions;
    popular_branch_ids INT[] := ARRAY [1, 2, 3];
    popular_txns       INT   := (total_transactions * 0.30)::INT; -- 300000
    remaining_txns     INT   := total_transactions - popular_txns; -- 700000
    base_tx_day_range  INT;
BEGIN
    PERFORM setseed(seed);

    IF tx_day_range = 0 THEN
        base_tx_day_range := 1;
    ELSE
        base_tx_day_range := tx_day_range;
    END IF;

    -- Step 1: Ensure every account has at least 1 transaction
    INSERT INTO transactions (branch_id, account_id, transaction_type, amount, transaction_date)
    SELECT a.branch_id,
           a.id,
           CASE WHEN a.id % 2 = 0 THEN 'deposit' ELSE 'withdrawal' END,
           round((50 + (a.id % 950))::NUMERIC, 2),
           CURRENT_DATE - ((a.id % base_tx_day_range) || ' days')::INTERVAL
    FROM accounts a;

    -- Step 2: Popular branch accounts
    CREATE TEMP TABLE temp_popular_accs AS
    SELECT a.branch_id, a.id, a.id AS account_id
    FROM accounts a
    WHERE a.branch_id = ANY (popular_branch_ids);

    -- Pick 5% of accounts in popular branches (hot accounts)
    CREATE TEMP TABLE temp_hot_accs AS
    SELECT *
    FROM temp_popular_accs
    WHERE id % 20 = 0;
    -- ~5%

    -- 30% of popular transactions (for 1mil ~ 90K)
    INSERT INTO transactions (branch_id, account_id, transaction_type, amount, transaction_date)
    SELECT p.branch_id,
           p.account_id,
           CASE WHEN p.id % 2 = 0 THEN 'deposit' ELSE 'transfer' END,
           round((500 + (p.id % 5000))::NUMERIC, 2),
           CURRENT_DATE - ((random() * tx_day_range)::INT || ' days')::INTERVAL
    FROM temp_hot_accs p
             CROSS JOIN generate_series(1, (popular_txns * 0.30 / (SELECT COUNT(*) FROM temp_hot_accs))::INT);

    -- Remaining 70% popular branch txns (for 1mil ~210K)
    CREATE TEMP TABLE temp_cold_accs AS
    SELECT * FROM temp_popular_accs WHERE id NOT IN (SELECT id FROM temp_hot_accs);

    INSERT INTO transactions (branch_id, account_id, transaction_type, amount, transaction_date)
    SELECT c.branch_id,
           c.account_id,
           CASE WHEN c.id % 2 = 1 THEN 'deposit' ELSE 'withdrawal' END,
           round((20 + (random() * 980))::NUMERIC, 2),
           CURRENT_DATE - ((random() * tx_day_range)::INT || ' days')::INTERVAL
    FROM temp_cold_accs c
             CROSS JOIN generate_series(1, (popular_txns * 0.70 / (SELECT COUNT(*) FROM temp_cold_accs))::INT);

    -- Step 3: Remaining transactions for other branches (for 1mil 700K)
    CREATE TEMP TABLE temp_other_accs AS
    SELECT a.branch_id, a.id AS account_id
    FROM accounts a
    WHERE a.branch_id NOT IN (SELECT UNNEST(popular_branch_ids));

    INSERT INTO transactions (branch_id, account_id, transaction_type, amount, transaction_date)
    SELECT o.branch_id,
           o.account_id,
           CASE WHEN o.account_id % 3 = 0 THEN 'transfer' ELSE 'withdrawal' END,
           round((25 + (random() * 2000))::NUMERIC, 2),
           CURRENT_DATE - ((random() * tx_day_range)::INT || ' days')::INTERVAL
    FROM temp_other_accs o
             CROSS JOIN generate_series(1, (remaining_txns / (SELECT COUNT(*) FROM temp_other_accs))::INT);

    DROP TABLE IF EXISTS temp_popular_accs, temp_hot_accs, temp_cold_accs, temp_other_accs;
END;
$$;

-- EXTRA
CREATE OR REPLACE PROCEDURE add_extra_transactions()
    LANGUAGE plpgsql
AS
$$
BEGIN
    CALL populate_transactions(10, 0.789, 0);
END;
$$;


--===========================================>>EXECUTION<<===========================================--

CALL populate_all()