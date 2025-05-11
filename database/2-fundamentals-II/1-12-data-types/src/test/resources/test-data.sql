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
INSERT INTO transactions (account_id, transaction_type, amount, target_account_id)
VALUES (1, 'deposit', 500.00, NULL),
       (2, 'withdrawal', 200.00, NULL),
       (3, 'transfer', 300.00, 4),
       (5, 'deposit', 1000.00, NULL),
       (6, 'withdrawal', 500.00, NULL),
       (7, 'transfer', 200.00, 8),
       (9, 'deposit', 700.00, NULL),
       (10, 'withdrawal', 300.00, NULL);

