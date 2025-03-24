INSERT INTO Customers (customer_name, city, contact_details) VALUES
    ('Ivan Kyivsky', 'Kyiv', 'ivan@kyiv.ua'),
    ('Petro Lvivsky', 'Lviv', 'petro@lviv.ua'),
    ('Maria Kyivska', 'Kyiv', 'maria@kyiv.ua'),
    ('Stepan Kharkivsky', 'Kharkiv', 'stepan@kharkiv.ua'),
    ('Olena Dniprovska', 'Dnipro', 'olena@dnipro.ua'),
    ('Maksym Kharkivsky', 'Kharkiv', 'maksym@kharkiv.ua');

INSERT INTO Accounts (account_number, customer_id, account_type, balance) VALUES
    ('CA001', 1, 'checking', 500.00),
    ('SA001', 1, 'savings', 7500.00),
    ('CA002', 2, 'checking', 1200.00),
    ('SA002', 2, 'savings', 25000.00),
    ('SA003', 3, 'savings', 45000.00),
    ('CA004', 5, 'checking', 120000.00);

INSERT INTO Transactions (account_number, transaction_type, transaction_date, amount) VALUES
    ('CA001', 'deposit', '2023-08-01', 1000.00),
    ('SA001', 'withdrawal', '2023-08-15', 200.00),
    ('CA002', 'transfer', '2023-09-01', 500.00),
    ('SA002', 'deposit', '2023-09-10', 10000.00),
    ('SA003', 'deposit', '2023-10-01', 5000.00),
    ('CA004', 'withdrawal', '2023-10-15', 20000.00);

INSERT INTO Loans (customer_id, loan_amount, interest_rate, loan_term_months) VALUES
    (1, 100000.00, 0.10, 36),
    (2, 50000.00, 0.08, 24),
    (3, 200000.00, 0.07, 48);

INSERT INTO Employees (employee_name, department) VALUES
    ('Anna Manager', 'Management'),
    ('Bohdan Clerk', 'Operations'),
    ('Viktoria Financier', 'Finance'),
    ('Hryhorii Programmer', 'IT');

INSERT INTO Branches (branch_name, location) VALUES
    ('Central Branch', 'Kyiv'),
    ('Lviv Branch', 'Lviv'),
    ('Kharkiv Branch', 'Kharkiv');