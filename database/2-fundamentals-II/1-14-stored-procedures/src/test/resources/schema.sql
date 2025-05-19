--------------------------------------------------------------------------------
-- Create Customers table
--------------------------------------------------------------------------------
CREATE TABLE customers
(
    id         SERIAL PRIMARY KEY,
    first_name VARCHAR(50)         NOT NULL,
    last_name  VARCHAR(50)         NOT NULL,
    email      VARCHAR(100) UNIQUE NOT NULL,
    phone      VARCHAR(20) UNIQUE  NOT NULL,
    address    TEXT                NOT NULL,
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
    transaction_type  VARCHAR(20) CHECK (transaction_type IN ('deposit', 'withdrawal', 'transfer', 'reversal')) NOT NULL,
    amount            DECIMAL(15, 2)                                                                NOT NULL,
    transaction_date  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    target_account_id INT                                                                           REFERENCES accounts (id) ON DELETE SET NULL
);

--------------------------------------------------------------------------------
-- Step 1: Create Branches table WITHOUT manager_id
--------------------------------------------------------------------------------
CREATE TABLE branches
(
    id         SERIAL PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    location   TEXT         NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

--------------------------------------------------------------------------------
-- Step 2: Create Employees table
--------------------------------------------------------------------------------
CREATE TABLE employees
(
    id         SERIAL PRIMARY KEY,
    first_name VARCHAR(50)    NOT NULL,
    last_name  VARCHAR(50)    NOT NULL,
    position   VARCHAR(50)    NOT NULL,
    salary     DECIMAL(10, 2) NOT NULL,
    branch_id  INT            REFERENCES branches (id) ON DELETE SET NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


--------------------------------------------------------------------------------
-- Create Loans table
--------------------------------------------------------------------------------
CREATE TABLE loans
(
    id            SERIAL PRIMARY KEY,
    customer_id   INT                                                             NOT NULL,
    amount        DECIMAL(15, 2)                                                  NOT NULL,
    interest_rate DECIMAL(5, 2)                                                   NOT NULL,
    term_months   INT                                                             NOT NULL,
    status        VARCHAR(20) CHECK (status IN ('pending', 'approved', 'active', 'closed', 'defaulted'))
                                                                                 NOT NULL DEFAULT 'pending',
    branch_id     INT REFERENCES branches(id) ON DELETE SET NULL,
    approved_by   INT REFERENCES employees(id) ON DELETE SET NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
);

--------------------------------------------------------------------------------
-- Step 3: Add manager_id to Branches table AFTER Employees exists
--------------------------------------------------------------------------------
ALTER TABLE branches
    ADD COLUMN manager_id INT UNIQUE,
    ADD CONSTRAINT fk_manager FOREIGN KEY (manager_id) REFERENCES employees (id) ON DELETE SET NULL;
