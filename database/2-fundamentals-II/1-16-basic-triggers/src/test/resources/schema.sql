--------------------------------------------------------------------------------
-- Create Audit Logs table
--------------------------------------------------------------------------------
CREATE TABLE audit_logs
(
    id             SERIAL PRIMARY KEY,
    operation_type VARCHAR(10)  NOT NULL,
    table_name     VARCHAR(255) NOT NULL,
    executed_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    query_text     TEXT         NOT NULL
);

--------------------------------------------------------------------------------
-- Create Customers table
--------------------------------------------------------------------------------
CREATE TABLE customers
(
    id         SERIAL PRIMARY KEY,
    first_name VARCHAR(50)         NOT NULL,
    last_name  VARCHAR(50)         NOT NULL,
    email      VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

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
