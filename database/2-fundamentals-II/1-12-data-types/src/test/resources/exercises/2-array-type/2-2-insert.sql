-- ===================================================================================================
-- EXERCISE 2.2 Insert data using the new ARRAY type
-- ===================================================================================================
-- Description:
--  Insert new rows into `customers` populating `tags`.
-- Sample Data:
--  1. id: 101, first_name: 'Alice', last_name: 'Smith', email:'alice.smith@example.com', phone: '1111111111',address: '123 Apple St' tags: 'premium','newsletter'
--  2. id: 102, first_name: 'Bob', last_name: 'Jones', email:'bob.jones@example.com', phone: '2222222222',address: '456 Berry Rd' tags: 'new','trial'
--  3. id: 103, first_name: 'Carol', last_name: 'Lee', email:'carol.lee@example.com', phone: '3333333333',address: '789 Cherry Ave' tags: 'premium'
--  4. id: 104, first_name: 'Dave', last_name: 'Kim', email:'dave.kim@example.com', phone: '4444444444',address: '101 Date Blvd' tags: <empty>
--  5. id: 105, first_name: 'Eve', last_name: 'Brown', email:'eve.brown@example.com', phone: '5555555555',address: '202 Elm St' tags: 'loyal','premium','vip'
-- ===================================================================================================
-- WORKING AREA 👇
INSERT INTO customers(id, first_name, last_name, email, phone, address, tags) VALUES
                                                                                  (101,'Alice','Smith','alice.smith@example.com','1111111111','123 Apple St','{ "premium","newsletter" }'),
                                                                                  (102,'Bob','Jones','bob.jones@example.com','2222222222','456 Berry Rd','{ "new","trial" }'),
                                                                                  (103,'Carol','Lee','carol.lee@example.com','3333333333','789 Cherry Ave','{ "premium" }'),
                                                                                  (104,'Dave','Kim','dave.kim@example.com','4444444444','101 Date Blvd',ARRAY[]::TEXT[]),
                                                                                  (105,'Eve','Brown','eve.brown@example.com','5555555555','202 Elm St','{ "loyal","premium","vip" }');