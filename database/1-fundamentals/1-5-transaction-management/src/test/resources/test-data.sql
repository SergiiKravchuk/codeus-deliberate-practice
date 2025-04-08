-- Insert test data into customers table
INSERT INTO customers (first_name, last_name, email, phone_number, address, city, state, postal_code, date_of_birth)
VALUES ('John', 'Doe', 'john.doe@example.com', '1234567890', '123 Elm St', 'Springfield', 'IL', '62701', '1980-01-01'),
       ('Jane', 'Smith', 'jane.smith@example.com', '0987654321', '456 Oak St', 'Springfield', 'IL', '62702',
        '1985-02-02'),
       ('Alice', 'Johnson', 'alice.johnson@example.com', '1112223333', '789 Pine St', 'Springfield', 'IL', '62703',
        '1990-03-03'),
       ('Bob', 'Brown', 'bob.brown@example.com', '4445556666', '101 Maple St', 'Springfield', 'IL', '62704',
        '1975-04-04'),
       ('Charlie', 'Davis', 'charlie.davis@example.com', '7778889999', '202 Birch St', 'Springfield', 'IL', '62705',
        '2000-05-05');

-- Insert test data into accounts table
INSERT INTO accounts (customer_id, account_type, balance)
VALUES (1, 'checking', 1000.00),
       (2, 'savings', 2000.00),
       (3, 'checking', 1500.00),
       (4, 'savings', 2500.00),
       (5, 'checking', 3000.00),
       (5, 'checking', 50.00);

-- Insert test data into transactions table
INSERT INTO transactions (account_id, transaction_type, amount, description)
VALUES (1, 'deposit', 500.00, 'Initial deposit'),
       (2, 'withdrawal', 200.00, 'ATM withdrawal'),
       (3, 'transfer', 300.00, 'Transfer to savings'),
       (4, 'deposit', 400.00, 'Paycheck deposit'),
       (5, 'withdrawal', 100.00, 'Bill payment');

-- Insert test data into loans table
INSERT INTO loans (customer_id, loan_type, loan_amount, interest_rate, loan_start_date, loan_end_date)
VALUES (1, 'personal', 5000.00, 5.5, '2023-01-01', '2028-01-01'),
       (2, 'auto', 10000.00, 4.5, '2023-02-01', '2028-02-01'),
       (3, 'mortgage', 150000.00, 3.5, '2023-03-01', '2043-03-01'),
       (4, 'student', 20000.00, 6.0, '2023-04-01', '2033-04-01'),
       (5, 'business', 25000.00, 7.0, '2023-05-01', '2033-05-01');

-- Insert test data into loan_payments table
INSERT INTO loan_payments (loan_id, payment_amount, description)
VALUES (1, 500.00, 'First payment'),
       (2, 1000.00, 'Initial payment'),
       (3, 1500.00, 'Monthly payment'),
       (4, 2000.00, 'Quarterly payment'),
       (5, 2500.00, 'Annual payment');

-- Insert test data into employees table
INSERT INTO employees (first_name, last_name, email, phone_number, hire_date, job_title)
VALUES ('Emily', 'White', 'emily.white@example.com', '1231231234', '2020-01-01', 'Manager'),
       ('David', 'Green', 'david.green@example.com', '2342342345', '2021-02-01', 'Teller'),
       ('Sophia', 'Black', 'sophia.black@example.com', '3453453456', '2022-03-01', 'Loan Officer'),
       ('James', 'Blue', 'james.blue@example.com', '4564564567', '2023-04-01', 'Customer Service'),
       ('Olivia', 'Gray', 'olivia.gray@example.com', '5675675678', '2024-05-01', 'Branch Manager');

-- Insert test data into branches table
INSERT INTO branches (branch_name, address, city, state, postal_code, phone_number)
VALUES ('Downtown', '100 Main St', 'Springfield', 'IL', '62701', '1111111111'),
       ('Uptown', '200 Main St', 'Springfield', 'IL', '62702', '2222222222'),
       ('Eastside', '300 Main St', 'Springfield', 'IL', '62703', '3333333333'),
       ('Westside', '400 Main St', 'Springfield', 'IL', '62704', '4444444444'),
       ('Northside', '500 Main St', 'Springfield', 'IL', '62705', '5555555555');

-- Insert test data into employments table
INSERT INTO employments (employee_id, branch_id, position, start_date)
VALUES (1, 1, 'Manager', '2020-01-01'),
       (2, 2, 'Teller', '2021-02-01'),
       (3, 3, 'Loan Officer', '2022-03-01'),
       (4, 4, 'Customer Service', '2023-04-01'),
       (5, 5, 'Branch Manager', '2024-05-01');
