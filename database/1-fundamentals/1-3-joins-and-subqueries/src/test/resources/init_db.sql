CREATE TABLE branches (
    branch_id SERIAL PRIMARY KEY,
    branch_name VARCHAR(100),
    location VARCHAR(200),
    opening_date DATE
);

CREATE TABLE employees (
    employee_id SERIAL PRIMARY KEY,
    branch_id INTEGER REFERENCES branches(branch_id),
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    position VARCHAR(100),
    hire_date DATE,
    salary DECIMAL(10,2)
);

CREATE TABLE customers (
    customer_id SERIAL PRIMARY KEY,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    email VARCHAR(100),
    phone VARCHAR(20),
    registration_date DATE
);

CREATE TABLE accounts (
    account_id SERIAL PRIMARY KEY,
    customer_id INTEGER REFERENCES customers(customer_id),
    branch_id INTEGER REFERENCES branches(branch_id),
    account_type VARCHAR(20),
    balance DECIMAL(12,2),
    opening_date DATE,
    status VARCHAR(20)
);

CREATE TABLE transactions (
    transaction_id SERIAL PRIMARY KEY,
    account_id INTEGER REFERENCES accounts(account_id),
    transaction_type VARCHAR(20),
    amount DECIMAL(12,2),
    transaction_date TIMESTAMP,
    description VARCHAR(200)
);

CREATE TABLE loans (
    loan_id SERIAL PRIMARY KEY,
    customer_id INTEGER REFERENCES customers(customer_id),
    branch_id INTEGER REFERENCES branches(branch_id),
    loan_type VARCHAR(50),
    amount DECIMAL(12,2),
    interest_rate DECIMAL(5,2),
    start_date DATE,
    end_date DATE,
    status VARCHAR(20)
);