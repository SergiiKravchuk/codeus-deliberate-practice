CREATE TABLE Customers (
    customer_id SERIAL PRIMARY KEY,
    customer_name VARCHAR(255) NOT NULL,
    city VARCHAR(255) NOT NULL,
    contact_details VARCHAR(255)
);

CREATE TABLE Accounts (
    account_number VARCHAR(20) PRIMARY KEY,
    customer_id INT REFERENCES Customers(customer_id),
    account_type VARCHAR(50) NOT NULL,
    balance DECIMAL(15, 2) NOT NULL
);

CREATE TABLE Transactions (
    transaction_id SERIAL PRIMARY KEY,
    account_number VARCHAR(20) REFERENCES Accounts(account_number),
    transaction_type VARCHAR(50) NOT NULL,
    transaction_date DATE NOT NULL,
    amount DECIMAL(15, 2) NOT NULL
);

CREATE TABLE Loans (
    loan_id SERIAL PRIMARY KEY,
    customer_id INT REFERENCES Customers(customer_id),
    loan_amount DECIMAL(15, 2) NOT NULL,
    interest_rate DECIMAL(5, 2) NOT NULL,
    loan_term_months INT NOT NULL
);

CREATE TABLE Employees (
    employee_id SERIAL PRIMARY KEY,
    employee_name VARCHAR(255) NOT NULL,
    department VARCHAR(255) NOT NULL
);

CREATE TABLE Branches (
    branch_id SERIAL PRIMARY KEY,
    branch_name VARCHAR(255) NOT NULL,
    location VARCHAR(255) NOT NULL
);