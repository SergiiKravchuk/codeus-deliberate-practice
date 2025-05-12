-- ===================================================================================================
-- EXERCISE 5.1 Declare DOMAIN type
-- ===================================================================================================
-- Description:
--  Create a DOMAIN `us_phone` based on VARCHAR(20) enforcing exactly 10 digits and
--  alter `customers.phone` to use the `us_phone` domain.
-- ===================================================================================================
-- WORKING AREA 👇

CREATE DOMAIN us_phone AS VARCHAR(20)
    CHECK (VALUE ~ '^[0-9]{10}$');

ALTER TABLE customers
    ADD COLUMN local_phone us_phone;