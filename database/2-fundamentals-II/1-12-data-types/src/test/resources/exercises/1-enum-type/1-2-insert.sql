-- ===================================================================================================
-- EXERCISE 1.2 Insert data using the new ENUM type
-- ===================================================================================================
-- Description:
--  Insert new rows into `accounts` setting the `status` column.
--
-- Sample Data (use these as guides):
--  1. account_id: 101, customer_id: 5, account_type: 'checking', balance: 100.00, status: 'active'
--  2. account_id: 102, customer_id: 6, account_type: 'savings', balance: 2500.50, status: 'inactive'
--  3. account_id: 103, customer_id: 7, account_type: 'checking', balance: 0.00, status: 'closed'
--  4. account_id: 104, customer_id: 5, account_type: 'savings', balance: 500.00, status: 'active'
--  5. account_id: 105, customer_id: 8, account_type: 'checking', balance: 750.75, status: 'inactive'
-- ===================================================================================================
-- WORKING AREA 👇

INSERT INTO accounts(id, customer_id, account_type, balance, status) VALUES
                                                                         (101, 5, 'checking', 100.00, 'active'),
                                                                         (102, 6, 'savings', 2500.50, 'inactive'),
                                                                         (103, 7, 'checking', 0.00, 'closed'),
                                                                         (104, 5, 'savings', 500.00, 'active'),
                                                                         (105, 8, 'checking', 750.75, 'inactive');