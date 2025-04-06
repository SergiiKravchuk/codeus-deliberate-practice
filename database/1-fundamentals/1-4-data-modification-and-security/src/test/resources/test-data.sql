------------------------------------------------------------------------
-- Populate Customers table
------------------------------------------------------------------------
INSERT INTO customers (first_name, last_name, email, phone, address)
VALUES ('John', 'Doe', 'email@example.com', '1234567890', '123 Main St'),
       ('Jane', 'Smith', 'jane.smith@example.com', '2345678901', '456 Oak St');

------------------------------------------------------------------------
-- Populate Employees table
------------------------------------------------------------------------
INSERT INTO employees (first_name, last_name, position, salary)
VALUES ('Michael', 'Scott', 'Manager', 60000.00),
       ('Dwight', 'Schrute', 'Assistant Manager', 50000.00),
       ('Jim', 'Halpert', 'Teller', 40000.00),
       ('Pam', 'Beesly', 'Customer Service', 45000.00),
       ('Stanley', 'Hudson', 'Loan Officer', 55000.00),
       ('Angela', 'Martin', 'Accountant', 48000.00),
       ('Kevin', 'Malone', 'Cashier', 39000.00),
       ('Pes', 'Patron', 'Auditor', 53000.00);

------------------------------------------------------------------------
-- Populate Challenges table
------------------------------------------------------------------------

INSERT INTO challenges (challenge_name, challenge_task)
VALUES  ('algo-challenge', '{"topic": "Linked List", "days":5}'),
        ('book-club', '{"name": "System Design", "weeks":8}');

------------------------------------------------------------------------
-- Populate Orders table
------------------------------------------------------------------------
INSERT INTO orders (employee_id, order_name, amount) VALUES (1, 'algo course', 1);