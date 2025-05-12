-- ===================================================================================================
-- EXERCISE 5.2 Insert data using the new DOMAIN type
-- ===================================================================================================
-- Description:
--  Insert new rows into `customers` setting `local_phone`.
-- Sample Data:
--  1. id: 106, first_name: 'Frank', last_name: 'Green', email: 'frank.green@example.com'  local_phone: '1234567890', address: '303 Fir St'
--  2. id: 107, first_name: 'Grace', last_name: 'Hall',  email: 'grace.hall@example.com'   local_phone: '0987654321', address: '404 Gum Rd'
--  3. id: 108, first_name: 'Hank',  last_name: 'Ivy',   email: 'hank.ivy@example.com'     local_phone: '5555555555', address: '505 Holly Ave'
--  4. id: 109, first_name: 'Irene', last_name: 'Jones', email: 'irene.jones@example.com'  local_phone: '1112223333', address: '606 Ivy Blvd'
--  5. id: 110, first_name: 'Jack',  last_name: 'King',  email: 'jack.king@example.com'    local_phone: '9998887777', address: '707 Juniper Ln'
-- ===================================================================================================
-- WORKING AREA 👇

INSERT INTO customers(id, first_name, last_name, email, local_phone, address) VALUES
                                                                            (106,'Frank','Green','frank.green@example.com','1234567890','303 Fir St'),
                                                                            (107,'Grace','Hall','grace.hall@example.com','0987654321','404 Gum Rd'),
                                                                            (108,'Hank','Ivy','hank.ivy@example.com','5555555555','505 Holly Ave'),
                                                                            (109,'Irene','Jones','irene.jones@example.com','1112223333','606 Ivy Blvd'),
                                                                            (110,'Jack','King','jack.king@example.com','9998887777','707 Juniper Ln');





