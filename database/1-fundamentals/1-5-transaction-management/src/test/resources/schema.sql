-- Table for storing customer details
CREATE TABLE customers
(
    customer_id   SERIAL PRIMARY KEY,
    first_name    VARCHAR(50)         NOT NULL,
    last_name     VARCHAR(50)         NOT NULL,
    email         VARCHAR(100) UNIQUE NOT NULL,
    phone_number  VARCHAR(15),
    address       VARCHAR(255),
    city          VARCHAR(50),
    state         VARCHAR(50),
    postal_code   VARCHAR(10),
    date_of_birth DATE
);

-- Table for storing account details
CREATE TABLE accounts
(
    account_id   SERIAL PRIMARY KEY,
    customer_id  INTEGER                     NOT NULL,
    account_type VARCHAR(20) CHECK (account_type IN ('checking', 'savings')),
    balance      DECIMAL(15, 2) DEFAULT 0.00 NOT NULL CHECK ( balance >= 0 ),
    created_at   TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers (customer_id)
);

-- Table for logging transactions
CREATE TABLE transactions
(
    transaction_id   SERIAL PRIMARY KEY,
    account_id       INTEGER        NOT NULL,
    transaction_type VARCHAR(20) CHECK (transaction_type IN ('deposit', 'withdrawal', 'transfer')),
    amount           DECIMAL(15, 2) NOT NULL,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    description      TEXT,
    FOREIGN KEY (account_id) REFERENCES accounts (account_id)
);

-- Table for managing loan details and repayments
CREATE TABLE loans
(
    loan_id         SERIAL PRIMARY KEY,
    customer_id     INTEGER        NOT NULL,
    loan_type       VARCHAR(50),
    loan_amount     DECIMAL(15, 2) NOT NULL,
    interest_rate   DECIMAL(5, 2)  NOT NULL,
    loan_start_date DATE           NOT NULL,
    loan_end_date   DATE           NOT NULL,
    -- todo: remove the line below
    -- balance         DECIMAL(15, 2) NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES customers (customer_id)
);

-- Table for storing loan payments
CREATE TABLE loan_payments
(
    payment_id     SERIAL PRIMARY KEY,
    loan_id        INTEGER        NOT NULL,
    payment_amount DECIMAL(15, 2) NOT NULL,
    payment_date   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    description    TEXT           NOT NULL,
    FOREIGN KEY (loan_id) REFERENCES loans (loan_id)
);

-- Table for storing employee details
CREATE TABLE employees
(
    employee_id  SERIAL PRIMARY KEY,
    first_name   VARCHAR(50)         NOT NULL,
    last_name    VARCHAR(50)         NOT NULL,
    email        VARCHAR(100) UNIQUE NOT NULL,
    phone_number VARCHAR(15),
    hire_date    DATE                NOT NULL,
    job_title    VARCHAR(50)         NOT NULL
);

-- Table for storing branch details
CREATE TABLE branches
(
    branch_id    SERIAL PRIMARY KEY,
    branch_name  VARCHAR(100) NOT NULL,
    address      VARCHAR(255),
    city         VARCHAR(50),
    state        VARCHAR(50),
    postal_code  VARCHAR(10),
    phone_number VARCHAR(15)
);

-- Table for representing bank staff managing customer accounts
CREATE TABLE employments
(
    employment_id SERIAL PRIMARY KEY,
    employee_id   INTEGER     NOT NULL,
    branch_id     INTEGER     NOT NULL,
    position      VARCHAR(50) NOT NULL,
    start_date    DATE        NOT NULL,
    end_date      DATE,
    FOREIGN KEY (employee_id) REFERENCES employees (employee_id),
    FOREIGN KEY (branch_id) REFERENCES branches (branch_id)
);
