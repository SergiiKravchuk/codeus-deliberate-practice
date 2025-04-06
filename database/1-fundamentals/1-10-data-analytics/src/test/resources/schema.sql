-- SQL Schema for Banking Analytics Database
-- Compatible with PostgreSQL, MySQL 8+

-- Drop tables if they exist (optional, for clean setup)
--DROP TABLE IF EXISTS transactions;
--DROP TABLE IF EXISTS accounts;
--DROP TABLE IF EXISTS customers;
--DROP TABLE IF EXISTS branches;

-- Branches Table
CREATE TABLE branches (
    branch_id INT PRIMARY KEY,
    branch_name VARCHAR(100) NOT NULL,
    city VARCHAR(50),
    state VARCHAR(50)
);

-- Customers Table
CREATE TABLE customers (
    customer_id INT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE,
    join_date DATE
);

-- Accounts Table
CREATE TABLE accounts (
    account_id INT PRIMARY KEY,
    customer_id INT NOT NULL,
    branch_id INT NOT NULL,
    account_type VARCHAR(50) NOT NULL, -- e.g., 'Checking', 'Savings'
    balance DECIMAL(15, 2) DEFAULT 0.00,
    open_date DATE,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    FOREIGN KEY (branch_id) REFERENCES branches(branch_id)
);

-- Transactions Table
CREATE TABLE transactions (
    transaction_id INT PRIMARY KEY,
    account_id INT NOT NULL,
    transaction_date TIMESTAMP NOT NULL,
    transaction_type VARCHAR(50) NOT NULL, -- e.g., 'Deposit', 'Withdrawal', 'Transfer', 'Fee'
    amount DECIMAL(15, 2) NOT NULL, -- Positive for inflow, negative for outflow
    description VARCHAR(255),
    FOREIGN KEY (account_id) REFERENCES accounts(account_id)
);

-- Add indexes for performance (optional but recommended)
CREATE INDEX idx_accounts_customer_id ON accounts(customer_id);
CREATE INDEX idx_accounts_branch_id ON accounts(branch_id);
CREATE INDEX idx_transactions_account_id ON transactions(account_id);
CREATE INDEX idx_transactions_transaction_date ON transactions(transaction_date);

-- End of Schema Definition
