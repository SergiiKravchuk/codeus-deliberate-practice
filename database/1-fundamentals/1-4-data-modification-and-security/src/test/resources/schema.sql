--------------------------------------------------------------------------------
-- Create Customers table
--------------------------------------------------------------------------------
CREATE TABLE customers
(
    id         SERIAL PRIMARY KEY,
    first_name VARCHAR(50)         NOT NULL,
    last_name  VARCHAR(50)         NOT NULL,
    email      VARCHAR(100) UNIQUE NOT NULL,
    phone      VARCHAR(20) UNIQUE  NOT NULL,
    address    TEXT                NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

--------------------------------------------------------------------------------
-- Step 2: Create Employees table
--------------------------------------------------------------------------------
CREATE TABLE employees
(
    id         SERIAL PRIMARY KEY,
    first_name VARCHAR(50)    NOT NULL,
    last_name  VARCHAR(50)    NOT NULL,
    position   VARCHAR(50)    NOT NULL,
    salary     DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

--------------------------------------------------------------------------------
-- Step 3: Create challenges table
--------------------------------------------------------------------------------

CREATE TABLE challenges (
    id SERIAL PRIMARY KEY,
    challenge_name VARCHAR(55),
    challenge_task jsonb
);

--------------------------------------------------------------------------------
-- Step 4: Create orders table
--------------------------------------------------------------------------------
CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    order_name VARCHAR(55),
    amount INT,
    employee_id INT REFERENCES employees(id) ON DELETE RESTRICT
);