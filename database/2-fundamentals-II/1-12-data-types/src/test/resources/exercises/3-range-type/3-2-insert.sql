-- ===================================================================================================
-- EXERCISE 3.2 Insert data using the new RANGE type
-- ===================================================================================================
-- Description:
--  Insert new rows into `accounts` setting `credit_limit`.
-- Sample Data:
--  1. account_id: 201, credit_limit: from inclusive 0 to exclusive 10000
--  2. account_id: 202, credit_limit: from inclusive 500 to inclusive 5000
--  3. account_id: 203, credit_limit: from exclusive 1000 to exclusive 15000
--  4. account_id: 204, credit_limit: from inclusive 0 to inclusive 0
--  5. account_id: 205, credit_limit: from inclusive 100 to inclusive 200
-- ===================================================================================================
-- WORKING AREA 👇

INSERT INTO accounts(id, customer_id, account_type, balance, credit_limit) VALUES
                                                                               (201,1,'checking',0.00,'[0,10000)'),
                                                                               (202,2,'savings',0.00,'[500,5000]'),
                                                                               (203,3,'checking',0.00,'(1000,15000)'),
                                                                               (204,4,'savings',0.00,'[0,0]'),
                                                                               (205,5,'checking',0.00,'[100,200]');