CREATE TABLE IF NOT EXISTS customers (
    id         SERIAL PRIMARY KEY,
    first_name VARCHAR(50)         NOT NULL,
    last_name  VARCHAR(50)         NOT NULL,
    email      VARCHAR(100) UNIQUE NOT NULL,
    phone      VARCHAR(20) UNIQUE  NOT NULL,
    address    TEXT                NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS accounts (
    id           SERIAL PRIMARY KEY,
    customer_id  INT                                                         NOT NULL,
    account_type VARCHAR(20) CHECK (account_type IN ('checking', 'savings')) NOT NULL,
    balance      DECIMAL(15, 2)                                              NOT NULL DEFAULT 0.00,
    created_at   TIMESTAMP                                                            DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS transactions (
    id                SERIAL PRIMARY KEY,
    account_id        INT NOT NULL REFERENCES accounts (id) ON DELETE CASCADE,
    transaction_type  VARCHAR(20) CHECK (transaction_type IN ('deposit', 'withdrawal', 'transfer')) NOT NULL,
    amount            DECIMAL(15, 2) NOT NULL,
    transaction_date  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    target_account_id INT REFERENCES accounts (id) ON DELETE SET NULL,
    status            VARCHAR(20) NOT NULL DEFAULT 'pending'
);

CREATE TABLE IF NOT EXISTS loans (
    id            SERIAL PRIMARY KEY,
    customer_id   INT                                                             NOT NULL,
    amount        DECIMAL(15, 2)                                                  NOT NULL,
    interest_rate DECIMAL(5, 2)                                                   NOT NULL,
    term_months   INT                                                             NOT NULL,
    status        VARCHAR(20) CHECK (status IN ('active', 'closed', 'defaulted')) NOT NULL DEFAULT 'active',
    created_at    TIMESTAMP                                                                DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS branches (
    id         SERIAL PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    location   TEXT         NOT NULL,
    phone      VARCHAR(20)  NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS employees (
    id         SERIAL PRIMARY KEY,
    first_name VARCHAR(50)    NOT NULL,
    last_name  VARCHAR(50)    NOT NULL,
    position   VARCHAR(50)    NOT NULL,
    salary     DECIMAL(10, 2) NOT NULL,
    branch_id  INT            REFERENCES branches(id) ON DELETE SET NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name='branches' AND column_name='manager_id'
    ) THEN
        ALTER TABLE branches
            ADD COLUMN manager_id INT UNIQUE;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_manager'
    ) THEN
        ALTER TABLE branches
            ADD CONSTRAINT fk_manager FOREIGN KEY (manager_id) REFERENCES employees(id) ON DELETE SET NULL;
    END IF;
END$$;

TRUNCATE TABLE transactions, accounts, customers, loans, branches, employees RESTART IDENTITY CASCADE;

INSERT INTO customers (first_name, last_name, email, phone, address)
VALUES
('John', 'Doe', 'john.doe@example.com', '+1234567890', '123 Main St'),
('Jane', 'Smith', 'jane.smith@example.com', '+0987654321', '456 Elm St');

INSERT INTO accounts (id, customer_id, account_type, balance)
VALUES
(1, 1, 'checking', 1000.00),
(2, 2, 'savings', 2000.00),
(10, 1, 'checking', 1000.00),
(20, 2, 'savings', 1000.00);

INSERT INTO transactions (account_id, transaction_type, amount, status)
VALUES
(1, 'deposit', 500.00, 'pending'),
(2, 'withdrawal', 300.00, 'pending'),
(1, 'deposit', 100.00, 'pending'),
(2, 'withdrawal', 50.00, 'pending'),
(1, 'deposit', 200.00, 'pending'),
(2, 'withdrawal', 150.00, 'pending'),
(1, 'deposit', 250.00, 'pending'),
(2, 'withdrawal', 350.00, 'pending'),
(1, 'deposit', 400.00, 'pending'),
(2, 'withdrawal', 450.00, 'pending');

INSERT INTO loans (customer_id, amount, interest_rate, term_months)
VALUES
(1, 10000.00, 5.5, 36),
(2, 20000.00, 4.5, 48);
