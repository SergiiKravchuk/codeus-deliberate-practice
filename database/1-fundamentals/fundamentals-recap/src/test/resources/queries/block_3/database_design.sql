-- ===================================================================================================
-- DATABASE DESIGN
-- ===================================================================================================
--Goal:
-- Design a small DB following the document design from the README.
-- Populate your DB with some data, you can use data from the queries/block_3/input_data_sample.md file.
-- ===================================================================================================
-- WORKING AREA 👇
-- 1. Bank Branch
CREATE TABLE Branch
(
    branch_id      SERIAL PRIMARY KEY,
    branch_name    VARCHAR(100) NOT NULL,
    street_address VARCHAR(150) NOT NULL,
    city           VARCHAR(50)  NOT NULL,
    state_province VARCHAR(50)  NOT NULL,
    postal_code    VARCHAR(10)  NOT NULL,
    manager_name   VARCHAR(100) NOT NULL,
    CHECK (postal_code ~ '^\d{5}$'
)
    );

-- 2. Customer
CREATE TABLE Customer
(
    customer_id     SERIAL PRIMARY KEY,
    full_name       VARCHAR(100) NOT NULL,
    email_address   VARCHAR(150) NOT NULL UNIQUE,
    phone_number    VARCHAR(20)  NOT NULL,
    date_of_birth   DATE         NOT NULL,
    mailing_address VARCHAR(200) NOT NULL,
    home_branch_id  INTEGER      NOT NULL REFERENCES Branch (branch_id),
    CHECK (date_of_birth < CURRENT_DATE),
    CHECK (phone_number ~ '^\+?\d{7,15}$'
)
    );

-- 3. Account Rating
CREATE TABLE AccountRating
(
    rating_code        INTEGER PRIMARY KEY,
    rating_description VARCHAR(50) NOT NULL
);

-- 4. Account
CREATE TABLE Account
(
    account_id   SERIAL PRIMARY KEY,
    customer_id  INTEGER        NOT NULL REFERENCES Customer (customer_id),
    branch_id    INTEGER        NOT NULL REFERENCES Branch (branch_id),
    account_type VARCHAR(20)    NOT NULL,
    balance      NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (balance >= 0),
    rating_code  INTEGER        NOT NULL,
    CHECK (account_type IN ('checking', 'savings')),
    FOREIGN KEY (rating_code) REFERENCES AccountRating (rating_code)

);


INSERT INTO Branch (branch_name, street_address, city, state_province, postal_code, manager_name)
VALUES ('Downtown Central', '100 Main St', 'Kyiv', 'Kyiv', '01001', 'Olena Shevchenko'),
       ('Suburban', '45 Evergreen Terrace', 'Vinnytsia', 'Vinnytsia', '21000', 'Viktor Tkachenko');

INSERT INTO Customer (full_name, email_address, phone_number, date_of_birth, mailing_address, home_branch_id)
VALUES ('Anastasia Kovalenko', 'anastasia.k@example.com', '+380671234567', '1985-07-12', '123 Sapfir St, Kyiv', 1),
       ('Mykhailo Kovtun', 'mykhailo.k@example.com', '+380681234678', '1987-05-20', '110 Metro Plaza, Kharkiv', 2);

INSERT INTO AccountRating (rating_code, rating_description)
VALUES (1, 'Bronze'),
       (2, 'Silver'),
       (3, 'Gold');

INSERT INTO Account (customer_id, branch_id, account_type, balance, rating_code)
VALUES (1, 1, 'checking', 1200.50, 1),
       (2, 2, 'savings', 5400.00, 2),
       (2, 2, 'checking', 300.75, 1);
