-- Insert Branches
INSERT INTO branches (branch_id, branch_name, location, opening_date) VALUES
(1, 'Downtown', '123 Main St', '2020-01-01'),
(2, 'Uptown', '456 High St', '2020-02-01'),
(3, 'Westside', '789 West Ave', '2020-03-01');

-- Insert Employees
INSERT INTO employees (employee_id, branch_id, first_name, last_name, position, hire_date, salary) VALUES
(1, 1, 'John', 'Smith', 'Manager', '2020-01-15', 75000),
(2, 1, 'Jane', 'Doe', 'Loan Officer', '2020-02-15', 65000),
(3, 2, 'Bob', 'Wilson', 'Manager', '2020-02-15', 72000),
(4, 2, 'Alice', 'Brown', 'Loan Officer', '2020-03-15', 63000),
(5, 3, 'Charlie', 'Davis', 'Manager', '2020-03-15', 70000);

-- Insert Customers
INSERT INTO customers (customer_id, first_name, last_name, email, phone, registration_date) VALUES
(1, 'Mike', 'Johnson', 'mike@email.com', '555-0101', '2020-04-01'),
(2, 'Sarah', 'Williams', 'sarah@email.com', '555-0102', '2020-04-02'),
(3, 'Tom', 'Davis', 'tom@email.com', '555-0103', '2020-04-03'),
(4, 'Emma', 'Wilson', 'emma@email.com', '555-0104', '2020-04-04'),
(5, 'James', 'Taylor', 'james@email.com', '555-0105', '2020-04-05');

-- Insert Accounts
INSERT INTO accounts (account_id, customer_id, branch_id, account_type, balance, opening_date, status) VALUES
(1, 1, 1, 'savings', 15000.00, '2020-04-01', 'active'),
(2, 1, 1, 'checking', 5000.00, '2020-04-01', 'active'),
(3, 2, 2, 'savings', 25000.00, '2020-04-02', 'active'),
(4, 3, 2, 'checking', 7500.00, '2020-04-03', 'active'),
(5, 4, 3, 'savings', 35000.00, '2020-04-04', 'active'),
(6, 5, 3, 'checking', 12500.00, '2020-04-05', 'active');

-- Insert Loans
INSERT INTO loans (loan_id, customer_id, branch_id, loan_type, amount, interest_rate, start_date, end_date, status) VALUES
(1, 1, 1, 'personal', 10000.00, 5.5, '2020-05-01', '2022-05-01', 'approved'),
(2, 2, 2, 'mortgage', 200000.00, 3.5, '2020-05-02', '2050-05-02', 'approved'),
(3, 3, 2, 'personal', 15000.00, 6.0, '2020-05-03', '2022-05-03', 'approved'),
(4, 4, 3, 'business', 50000.00, 4.5, '2020-05-04', '2023-05-04', 'approved'),
(5, 5, 3, 'personal', 20000.00, 5.8, '2020-05-05', '2022-05-05', 'approved');

-- Insert Transactions
INSERT INTO transactions (transaction_id, account_id, transaction_type, amount, transaction_date, description) VALUES
(1, 1, 'deposit', 5000.00, '2023-01-01 10:00:00', 'Salary deposit'),
(2, 1, 'withdrawal', 1000.00, '2023-01-02 11:00:00', 'ATM withdrawal'),
(3, 2, 'deposit', 3000.00, '2023-01-03 12:00:00', 'Check deposit'),
(4, 3, 'deposit', 7000.00, '2023-01-04 13:00:00', 'Transfer'),
(5, 4, 'withdrawal', 2000.00, '2023-01-05 14:00:00', 'Bill payment'),
(6, 5, 'deposit', 10000.00, '2023-01-06 15:00:00', 'Investment return'),
(7, 6, 'withdrawal', 3000.00, '2023-01-07 16:00:00', 'Credit card payment');