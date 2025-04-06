-- SQL Seed Data for Banking Analytics Database

-- Clear existing data (optional, ensures clean insert)
DELETE FROM transactions;
DELETE FROM accounts;
DELETE FROM customers;
DELETE FROM branches;

-- Insert Branches
INSERT INTO branches (branch_id, branch_name, city, state) VALUES
(1, 'Downtown Central', 'Metropolis', 'NY'),
(2, 'Westside Branch', 'Metropolis', 'NY'),
(3, 'Oceanview Branch', 'Coast City', 'CA'),
(4, 'Mountain Branch', 'Alpine', 'CO');

-- Insert Customers
INSERT INTO customers (customer_id, first_name, last_name, email, join_date) VALUES
(101, 'Alice', 'Smith', 'alice.s@email.com', '2022-01-15'),
(102, 'Bob', 'Johnson', 'bob.j@email.com', '2022-03-22'),
(103, 'Charlie', 'Williams', 'charlie.w@email.com', '2021-11-01'),
(104, 'Diana', 'Brown', 'diana.b@email.com', '2022-05-10'),
(105, 'Ethan', 'Davis', 'ethan.d@email.com', '2023-02-28'),
(106, 'Fiona', 'Miller', 'fiona.m@email.com', '2023-06-19');

-- Insert Accounts
INSERT INTO accounts (account_id, customer_id, branch_id, account_type, balance, open_date) VALUES
(1001, 101, 1, 'Checking', 5000.00, '2022-01-15'),
(1002, 101, 1, 'Savings', 15000.00, '2022-01-15'),
(1003, 102, 2, 'Checking', 2500.00, '2022-03-22'),
(1004, 103, 1, 'Checking', 8000.00, '2021-11-01'),
(1005, 103, 3, 'Savings', 25000.00, '2022-07-01'),
(1006, 104, 2, 'Checking', 1200.00, '2022-05-10'),
(1007, 105, 4, 'Checking', 3000.00, '2023-02-28'),
(1008, 105, 4, 'Savings', 7000.00, '2023-03-15'),
(1009, 106, 3, 'Checking', 4500.00, '2023-06-19');

-- Insert Transactions (More data for better analysis)
-- Account 1001 (Alice Checking)
INSERT INTO transactions (transaction_id, account_id, transaction_date, transaction_type, amount, description) VALUES
(1, 1001, '2024-01-10 09:00:00', 'Deposit', 1000.00, 'Salary'),
(2, 1001, '2024-01-15 14:30:00', 'Withdrawal', -200.00, 'Groceries'),
(3, 1001, '2024-01-25 11:00:00', 'Withdrawal', -50.00, 'Coffee Shop'),
(4, 1001, '2024-02-10 09:05:00', 'Deposit', 1000.00, 'Salary'),
(5, 1001, '2024-02-12 16:00:00', 'Withdrawal', -300.00, 'Online Shopping'),
(6, 1001, '2024-02-20 08:15:00', 'Withdrawal', -150.00, 'Restaurant'),
(7, 1001, '2024-03-10 09:10:00', 'Deposit', 1000.00, 'Salary'),
(8, 1001, '2024-03-18 10:00:00', 'Withdrawal', -250.00, 'Utilities Bill'),
(27, 1001, '2024-03-18 10:05:00', 'Withdrawal', -250.00, 'Duplicate Bill Payment?'), -- Potential fraud/error
(9, 1001, '2024-03-28 17:00:00', 'Withdrawal', -75.00, 'Movie Tickets');

-- Account 1002 (Alice Savings)
INSERT INTO transactions (transaction_id, account_id, transaction_date, transaction_type, amount, description) VALUES
(10, 1002, '2024-01-20 10:00:00', 'Deposit', 500.00, 'Transfer from Checking'),
(11, 1002, '2024-02-20 10:00:00', 'Deposit', 500.00, 'Transfer from Checking'),
(12, 1002, '2024-03-20 10:00:00', 'Deposit', 500.00, 'Transfer from Checking');

-- Account 1003 (Bob Checking)
INSERT INTO transactions (transaction_id, account_id, transaction_date, transaction_type, amount, description) VALUES
(13, 1003, '2024-01-05 12:00:00', 'Deposit', 1500.00, 'Freelance Payment'),
(14, 1003, '2024-01-22 13:00:00', 'Withdrawal', -400.00, 'Rent'),
(15, 1003, '2024-02-05 12:05:00', 'Deposit', 1500.00, 'Freelance Payment'),
(16, 1003, '2024-02-22 13:05:00', 'Withdrawal', -400.00, 'Rent'),
(17, 1003, '2024-03-05 12:10:00', 'Deposit', 1750.00, 'Freelance Payment Bonus'),
(18, 1003, '2024-03-22 13:10:00', 'Withdrawal', -400.00, 'Rent');

-- Account 1004 (Charlie Checking)
INSERT INTO transactions (transaction_id, account_id, transaction_date, transaction_type, amount, description) VALUES
(19, 1004, '2024-01-15 09:30:00', 'Deposit', 2000.00, 'Paycheck'),
(20, 1004, '2024-01-20 10:30:00', 'Withdrawal', -1000.00, 'Car Payment'),
(21, 1004, '2024-02-15 09:35:00', 'Deposit', 2000.00, 'Paycheck'),
(22, 1004, '2024-02-20 10:35:00', 'Withdrawal', -1000.00, 'Car Payment'),
(23, 1004, '2024-03-15 09:40:00', 'Deposit', 2000.00, 'Paycheck'),
(24, 1004, '2024-03-20 10:40:00', 'Withdrawal', -1000.00, 'Car Payment');

-- Account 1005 (Charlie Savings) - Fewer transactions
INSERT INTO transactions (transaction_id, account_id, transaction_date, transaction_type, amount, description) VALUES
(25, 1005, '2024-01-30 15:00:00', 'Deposit', 1000.00, 'Bonus Transfer'),
(26, 1005, '2024-03-30 15:00:00', 'Deposit', 1500.00, 'Savings Goal');

-- Account 1006 (Diana Checking)
INSERT INTO transactions (transaction_id, account_id, transaction_date, transaction_type, amount, description) VALUES
(28, 1006, '2024-01-01 10:00:00', 'Deposit', 500.00, 'Initial Deposit'),
(29, 1006, '2024-01-15 11:00:00', 'Withdrawal', -50.00, 'Books'),
(30, 1006, '2024-02-01 10:00:00', 'Deposit', 300.00, 'Part-time Job'),
(31, 1006, '2024-02-20 14:00:00', 'Withdrawal', -100.00, 'Shopping'),
(32, 1006, '2024-03-01 10:00:00', 'Deposit', 300.00, 'Part-time Job'),
(33, 1006, '2024-03-10 16:00:00', 'Withdrawal', -80.00, 'Dinner'),
-- Added transactions for Task 20 Test Case
(40, 1006, '2024-03-11 09:15:00', 'Deposit', 200.00, 'Refund Received'),
(41, 1006, '2024-03-11 09:45:00', 'Withdrawal', -150.00, 'Quick Cash'); -- Withdrawal within 1 hour of deposit

-- Account 1009 (Fiona Checking)
INSERT INTO transactions (transaction_id, account_id, transaction_date, transaction_type, amount, description) VALUES
(34, 1009, '2024-01-05 10:00:00', 'Deposit', 1200.00, 'Consulting Fee'),
(35, 1009, '2024-01-20 11:00:00', 'Withdrawal', -300.00, 'Supplies'),
(36, 1009, '2024-02-05 10:00:00', 'Deposit', 1300.00, 'Consulting Fee'),
(37, 1009, '2024-02-18 14:00:00', 'Withdrawal', -500.00, 'Software Subscription'),
(38, 1009, '2024-03-05 10:00:00', 'Deposit', 1250.00, 'Consulting Fee'),
(39, 1009, '2024-03-25 16:00:00', 'Withdrawal', -150.00, 'Travel');

-- Note: Balances in the `accounts` table are initial balances and are NOT automatically updated by these transaction inserts in this simple model.
-- The transaction amounts reflect the change that *would* affect the balance.

-- End of Seed Data
