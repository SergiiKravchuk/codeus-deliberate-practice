------------------------------------------------------------------------
-- Populate Customers table
------------------------------------------------------------------------
INSERT INTO customers (first_name, last_name, email, phone, address)
VALUES ('John', 'Doe', 'john.doe@example.com', '1234567890', '123 Main St'),
       ('Jane', 'Smith', 'jane.smith@example.com', '2345678901', '456 Oak St'),
       ('Alice', 'Brown', 'alice.brown@example.com', '3456789012', '789 Pine St'),
       ('Bob', 'Johnson', 'bob.johnson@example.com', '4567890123', '101 Maple St'),
       ('Charlie', 'Davis', 'charlie.davis@example.com', '5678901234', '202 Cedar St'),
       ('Emma', 'Wilson', 'emma.wilson@example.com', '6789012345', '303 Birch St'),
       ('Liam', 'Anderson', 'liam.anderson@example.com', '7890123456', '404 Walnut St'),
       ('Olivia', 'Martinez', 'olivia.martinez@example.com', '8901234567', '505 Cherry St'),
       ('Mason', 'Clark', 'mason.clark@example.com', '9012345678', '606 Elm St'),
       ('Sophia', 'Rodriguez', 'sophia.rodriguez@example.com', '0123456789', '707 Ash St');

------------------------------------------------------------------------
-- Populate Accounts table
------------------------------------------------------------------------
INSERT INTO accounts (customer_id, account_type, balance)
VALUES (1, 'checking', 1500.00),
       (1, 'savings', 10000.00),
       (2, 'checking', 1200.00),
       (3, 'savings', 3000.00),
       (4, 'checking', 2000.00),
       (5, 'savings', 7000.00),
       (6, 'checking', 500.00),
       (7, 'savings', 2500.00),
       (8, 'checking', 3000.00),
       (9, 'savings', 11000.00);

------------------------------------------------------------------------
-- Populate Transactions table
------------------------------------------------------------------------
--INSERT INTO transactions (account_id, transaction_type, amount, target_account_id)
--VALUES (1, 'deposit', 500.00, NULL),
--       (2, 'withdrawal', 200.00, NULL),
--       (3, 'transfer', 300.00, 4),
--       (5, 'deposit', 1000.00, NULL),
--       (6, 'withdrawal', 500.00, NULL),
--       (7, 'transfer', 200.00, 8),
--       (9, 'deposit', 700.00, NULL),
--       (10, 'withdrawal', 300.00, NULL);

------------------------------------------------------------------------
-- Populate Loans table
------------------------------------------------------------------------
INSERT INTO loans (customer_id, amount, interest_rate, term_months, status)
VALUES (1, 5000.00, 5.5, 24, 'active'),
       (2, 10000.00, 4.8, 36, 'active'),
       (3, 7000.00, 6.0, 48, 'closed'),
       (4, 15000.00, 3.9, 60, 'defaulted'),
       (5, 20000.00, 4.5, 72, 'active'),
       (6, 12000.00, 5.2, 36, 'closed'),
       (7, 9000.00, 4.0, 24, 'active'),
       (8, 11000.00, 5.7, 48, 'active'),
       (9, 13000.00, 4.9, 60, 'closed'),
       (10, 18000.00, 3.5, 72, 'active');

------------------------------------------------------------------------
-- Populate Branches table (without manager_id initially
------------------------------------------------------------------------
INSERT INTO branches (name, location, phone)
VALUES ('Downtown Branch', 'City Center', '111-222-3333'),
       ('Uptown Branch', 'North Side', '222-333-4444'),
       ('Westside Branch', 'West End', '333-444-5555'),
       ('Eastside Branch', 'East District', '444-555-6666');

------------------------------------------------------------------------
-- Populate Employees table
------------------------------------------------------------------------
INSERT INTO employees (first_name, last_name, position, salary, branch_id)
VALUES ('Michael', 'Scott', 'Manager', 60000.00, 1),
       ('Dwight', 'Schrute', 'Assistant Manager', 50000.00, 1),
       ('Jim', 'Halpert', 'Teller', 40000.00, 2),
       ('Pam', 'Beesly', 'Customer Service', 45000.00, 2),
       ('Stanley', 'Hudson', 'Loan Officer', 55000.00, 3),
       ('Angela', 'Martin', 'Accountant', 48000.00, 3),
       ('Kevin', 'Malone', 'Cashier', 39000.00, 4),
       ('Oscar', 'Martinez', 'Auditor', 53000.00, 4);

------------------------------------------------------------------------
-- Update Branches table to set manager_id after Employees are inserted
------------------------------------------------------------------------
UPDATE branches SET manager_id = 1 WHERE id = 1;
UPDATE branches SET manager_id = 3 WHERE id = 2;
UPDATE branches SET manager_id = 5 WHERE id = 3;
