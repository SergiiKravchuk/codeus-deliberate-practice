--------------------------------------------------------------------------------
-- Create Customers table
--------------------------------------------------------------------------------
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

CREATE INDEX idx_customers_first_name_pattern ON customers(first_name)

--------------------------------------------------------------------------------
-- Create Accounts table
--------------------------------------------------------------------------------
CREATE TABLE accounts
(
    id           SERIAL PRIMARY KEY,
    customer_id  INT                                                         NOT NULL,
    account_type VARCHAR(20) CHECK (account_type IN ('checking', 'savings')) NOT NULL,
    balance      DECIMAL(15, 2)                                              NOT NULL DEFAULT 0.00,
    created_at   TIMESTAMP                                                            DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers (id) ON DELETE CASCADE
);

CREATE INDEX idx_accounts_account_type ON accounts(account_type);
--------------------------------------------------------------------------------
-- Create Transactions table
--------------------------------------------------------------------------------
CREATE TABLE transactions
(
    id                SERIAL PRIMARY KEY,
    account_id        INT                                                                           NOT NULL REFERENCES accounts (id) ON DELETE CASCADE,
    transaction_type  VARCHAR(20) CHECK (transaction_type IN ('deposit', 'withdrawal', 'transfer')) NOT NULL,
    amount            DECIMAL(15, 2)                                                                NOT NULL,
    transaction_date  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    target_account_id INT                                                                           REFERENCES accounts (id) ON DELETE SET NULL
);

--------------------------------------------------------------------------------
-- Populate Customers table
--------------------------------------------------------------------------------
DO $$
DECLARE
    first_names TEXT[] := ARRAY['John', 'Jane', 'Alex', 'Emily', 'Michael', 'Sarah', 'David', 'Olivia', 'Daniel', 'Emma'];
    last_names TEXT[] := ARRAY['Smith', 'Johnson', 'Brown', 'Williams', 'Jones', 'Miller', 'Davis', 'Garcia', 'Martinez', 'Wilson'];
    num_transactions INT := COALESCE(NULLIF(current_setting('app.num_transactions', TRUE), '')::INT, 1000000);
    num_accounts INT := num_transactions / 10;
    num_customers INT;
    rand_fn_idx INT;
    rand_ln_idx INT;
    fn TEXT;
    ln TEXT;
BEGIN
    num_customers := GREATEST(1, num_accounts / 3);

    FOR i IN 1..num_customers LOOP
        rand_fn_idx := floor(random() * array_length(first_names, 1) + 1)::INT;
        rand_ln_idx := floor(random() * array_length(last_names, 1) + 1)::INT;

        fn := first_names[rand_fn_idx];
        ln := last_names[rand_ln_idx];

        INSERT INTO customers (first_name, last_name, email, phone, address)
        VALUES (
            fn,
            ln,
            lower(fn || '.' || ln || i || '@example.com'),
            '+380-' || lpad((1000000 + floor(random() * 9000000)::INT)::TEXT, 7, '0'),
            ln || ' Street, City, Country'
        );
    END LOOP;
END $$;

--------------------------------------------------------------------------------
-- Populate Accounts table
--------------------------------------------------------------------------------
DO $$
    DECLARE
        account_types TEXT[] := ARRAY['checking', 'savings'];
    BEGIN
        INSERT INTO accounts (customer_id, account_type, balance, created_at)
        SELECT
            c.id,
            account_types[(floor(random() * 2)::INT + 1)],
            ROUND((random() * 10000)::NUMERIC, 2),
            CURRENT_TIMESTAMP - (random() * INTERVAL '365 days')
        FROM customers c,
             generate_series(2, (2 + floor(random() * 5))::INT); -- 2 to 5 accounts per customer
    END$$;

--------------------------------------------------------------------------------
-- Populate Transactions table
--------------------------------------------------------------------------------
DO $$
DECLARE
    num_transactions INT := COALESCE(NULLIF(current_setting('app.num_transactions', TRUE), '')::INT, 1000000);
    account_ids INT[];
    acc_count INT;
BEGIN
    -- Load all account IDs once into an array
    SELECT array_agg(id), COUNT(*) INTO account_ids, acc_count FROM accounts;

    IF acc_count = 0 THEN
        RAISE EXCEPTION 'No accounts available!';
    END IF;

    -- Insert random transactions
    INSERT INTO transactions (account_id, transaction_type, amount, transaction_date, target_account_id)
    SELECT
        account_ids[1 + (random() * (acc_count - 1))::INT],
        CASE
            WHEN rnd < 0.8 THEN 'deposit'
            WHEN rnd < 0.9 THEN 'withdrawal'
            ELSE 'transfer'
        END,
        ROUND((abs(100 * sqrt(-2 * ln(random())) * cos(2 * pi() * random())))::NUMERIC, 2),
        NOW() - INTERVAL '365 days' * power(random(), 3),
        CASE WHEN rnd >= 0.9 THEN
            account_ids[1 + (random() * (acc_count - 1))::INT]
        ELSE NULL END
    FROM (
        SELECT random() AS rnd FROM generate_series(1, num_transactions)
    ) t;
END $$;

