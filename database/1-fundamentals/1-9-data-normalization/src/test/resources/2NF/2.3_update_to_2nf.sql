-- ====================================================================================================================
-- TASK 2.3: Update table to 2NF
-- ====================================================================================================================
-- RULE: Each NON-KEY attribute must depend on the ENTIRE primary key + be in 1NF!
--
-- In our `customers_1nf` table we have such types of dependency groups:
-- 1. ssn -> customer_name (Each SSN always determines exactly one customer_name. Doesn't have any relation with `contact_type`)
-- 2. ssn -> reliability_score (Each SSN always determines exactly one reliability_score. Doesn't have any relation with `contact_type`)
-- 3. (ssn, contact_type) -> contact_value (Each combination of SSN and contact_type determines exactly one contact_value)
--
-- Now, we need to follow the 2NF. To do so, we need to segregate the `customers_1nf` on the two tables where each
-- NON-KEY attribute should depend on an ENTIRE primary key!

-- TODO: Create a new table:
-- Table name: `customers`
-- Fields:
-- - `ssn`, int, primary key
-- - `customer_name`, VARCHAR(100), NOT NULL
-- - `reliability_score`, VARCHAR(30), NOT NULL with check that `reliability_score` in ['vip', 'most reliable', 'reliable', 'probably reliable', 'not reliable']
CREATE TABLE customers
(
    ssn               INT PRIMARY KEY,
    customer_name     VARCHAR(100) NOT NULL,
    reliability_score VARCHAR(30) CHECK (
        reliability_score IN ('vip', 'most reliable', 'reliable', 'probably reliable', 'not reliable')
        )                          NOT NULL
);

-- TODO: Create a new table:
-- Table name: `customer_contacts`
-- Fields:
-- - `ssn`, int
-- - `contact_type`, VARCHAR(20), NOT NULL
-- - `contact_value`, VARCHAR(30)
-- It must composite primary key of the `ssn` and `contact_type`
-- It must have foreign key to `customers.ssn` to support functional dependency
CREATE TABLE customer_contacts
(
    ssn           INT,
    contact_type  VARCHAR(20),
    contact_value VARCHAR(30),
    PRIMARY KEY (ssn, contact_type),
    FOREIGN KEY (ssn) REFERENCES customers (ssn)
);