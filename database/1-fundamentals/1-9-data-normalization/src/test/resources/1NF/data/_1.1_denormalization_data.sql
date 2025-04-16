-- Script to generate 100,000 customer records for customers_denormalized_order table
-- This script uses PostgreSQL-specific functions for data generation

-- First, truncate the table if it already has data
TRUNCATE TABLE customers_denormalized_order RESTART IDENTITY;

-- Temporarily disable triggers if any exist
-- ALTER TABLE customers_denormalized_order DISABLE TRIGGER ALL;

-- Create a function to generate random contact information
CREATE OR REPLACE FUNCTION random_contact()
    RETURNS VARCHAR(500) AS $$
DECLARE
    contact_type INT;
    result VARCHAR(500);
    has_personal_phone BOOLEAN;
    has_work_phone BOOLEAN;
    has_personal_email BOOLEAN;
    has_work_email BOOLEAN;
    first_name TEXT;
    last_name TEXT;
    domain TEXT;
    company TEXT;
BEGIN
    -- Determine which contact types to include (0 = NULL, 1-15 = various combinations)
    contact_type := floor(random() * 16)::INT;

    -- 1/16 chance of NULL contact
    IF contact_type = 0 THEN
        RETURN NULL;
    END IF;

    -- Determine which contact elements to include based on bit flags
    has_personal_phone := (contact_type & 1) > 0;
    has_work_phone := (contact_type & 2) > 0;
    has_personal_email := (contact_type & 4) > 0;
    has_work_email := (contact_type & 8) > 0;

    -- Generate the first and last name components that will be used in emails
    first_name := lower(chr(97 + floor(random() * 26)::INT) ||
                        chr(97 + floor(random() * 26)::INT) ||
                        chr(97 + floor(random() * 26)::INT) ||
                        chr(97 + floor(random() * 26)::INT));

    last_name := lower(chr(97 + floor(random() * 26)::INT) ||
                       chr(97 + floor(random() * 26)::INT) ||
                       chr(97 + floor(random() * 26)::INT) ||
                       chr(97 + floor(random() * 26)::INT) ||
                       chr(97 + floor(random() * 26)::INT));

    -- Pick a random email domain
    domain := (ARRAY['gmail.com', 'yahoo.com', 'hotmail.com', 'outlook.com', 'protonmail.com', 'icloud.com'])[1 + floor(random() * 6)::INT];

    -- Pick a random company
    company := (ARRAY['acme', 'globex', 'initech', 'umbrella', 'cyberdyne', 'waystar', 'massive', 'soylent', 'hooli'])[1 + floor(random() * 9)::INT];

    -- Build the contact string
    result := '';

    IF has_personal_phone THEN
        result := result || 'Personal phone: +1' || (floor(random() * 900) + 100)::TEXT ||
                  (floor(random() * 900) + 100)::TEXT ||
                  (floor(random() * 9000) + 1000)::TEXT;
    END IF;

    IF has_work_phone THEN
        IF length(result) > 0 THEN
            result := result || ', ';
        END IF;
        result := result || 'Work phone: +1' || (floor(random() * 900) + 100)::TEXT ||
                  (floor(random() * 900) + 100)::TEXT ||
                  (floor(random() * 9000) + 1000)::TEXT;
    END IF;

    IF has_personal_email THEN
        IF length(result) > 0 THEN
            result := result || ', ';
        END IF;
        result := result || 'Personal email: ' || first_name || '.' || last_name || '@' || domain;
    END IF;

    IF has_work_email THEN
        IF length(result) > 0 THEN
            result := result || ', ';
        END IF;
        result := result || 'Work email: ' || first_name || '@' || company || '.com';
    END IF;

    RETURN result;
END;
$$ LANGUAGE plpgsql;

-- Create a function to generate random names
CREATE OR REPLACE FUNCTION random_name()
    RETURNS VARCHAR(100) AS $$
DECLARE
    first_names TEXT[] := ARRAY[
        'James', 'Mary', 'John', 'Patricia', 'Robert', 'Jennifer', 'Michael', 'Linda',
        'William', 'Elizabeth', 'David', 'Barbara', 'Richard', 'Susan', 'Joseph', 'Jessica',
        'Thomas', 'Sarah', 'Charles', 'Karen', 'Christopher', 'Nancy', 'Daniel', 'Lisa',
        'Matthew', 'Margaret', 'Anthony', 'Betty', 'Mark', 'Sandra', 'Donald', 'Ashley',
        'Steven', 'Kimberly', 'Paul', 'Emily', 'Andrew', 'Donna', 'Joshua', 'Michelle',
        'Kenneth', 'Dorothy', 'Kevin', 'Carol', 'Brian', 'Amanda', 'George', 'Melissa',
        'Edward', 'Deborah', 'Ronald', 'Stephanie', 'Timothy', 'Rebecca', 'Jason', 'Sharon',
        'Jeffrey', 'Laura', 'Ryan', 'Cynthia', 'Jacob', 'Kathleen', 'Gary', 'Amy',
        'Nicholas', 'Shirley', 'Eric', 'Angela', 'Jonathan', 'Helen', 'Stephen', 'Anna',
        'Larry', 'Brenda', 'Justin', 'Pamela', 'Scott', 'Nicole', 'Brandon', 'Emma',
        'Benjamin', 'Samantha', 'Samuel', 'Katherine', 'Gregory', 'Christine', 'Frank', 'Debra',
        'Alexander', 'Rachel', 'Raymond', 'Catherine', 'Patrick', 'Carolyn', 'Jack', 'Janet',
        'Dennis', 'Ruth', 'Jerry', 'Maria', 'Tyler', 'Heather', 'Aaron', 'Diane',
        'Jose', 'Virginia', 'Adam', 'Julie', 'Henry', 'Joyce', 'Nathan', 'Victoria',
        'Douglas', 'Olivia', 'Zachary', 'Kelly', 'Peter', 'Christina', 'Kyle', 'Lauren',
        'Walter', 'Joan', 'Ethan', 'Evelyn', 'Jeremy', 'Judith', 'Harold', 'Megan',
        'Keith', 'Cheryl', 'Christian', 'Andrea', 'Roger', 'Hannah', 'Noah', 'Martha'
        ];

    last_names TEXT[] := ARRAY[
        'Smith', 'Johnson', 'Williams', 'Brown', 'Jones', 'Garcia', 'Miller', 'Davis',
        'Rodriguez', 'Martinez', 'Hernandez', 'Lopez', 'Gonzales', 'Wilson', 'Anderson', 'Thomas',
        'Taylor', 'Moore', 'Jackson', 'Martin', 'Lee', 'Perez', 'Thompson', 'White',
        'Harris', 'Sanchez', 'Clark', 'Ramirez', 'Lewis', 'Robinson', 'Walker', 'Young',
        'Allen', 'King', 'Wright', 'Scott', 'Torres', 'Nguyen', 'Hill', 'Flores',
        'Green', 'Adams', 'Nelson', 'Baker', 'Hall', 'Rivera', 'Campbell', 'Mitchell',
        'Carter', 'Roberts', 'Gomez', 'Phillips', 'Evans', 'Turner', 'Diaz', 'Parker',
        'Cruz', 'Edwards', 'Collins', 'Reyes', 'Stewart', 'Morris', 'Morales', 'Murphy',
        'Cook', 'Rogers', 'Gutierrez', 'Ortiz', 'Morgan', 'Cooper', 'Peterson', 'Bailey',
        'Reed', 'Kelly', 'Howard', 'Ramos', 'Kim', 'Cox', 'Ward', 'Richardson',
        'Watson', 'Brooks', 'Chavez', 'Wood', 'James', 'Bennett', 'Gray', 'Mendoza',
        'Ruiz', 'Hughes', 'Price', 'Alvarez', 'Castillo', 'Sanders', 'Patel', 'Myers',
        'Long', 'Ross', 'Foster', 'Jimenez', 'Powell', 'Jenkins', 'Perry', 'Russell',
        'Sullivan', 'Bell', 'Coleman', 'Butler', 'Henderson', 'Barnes', 'Gonzalez', 'Fisher',
        'Vasquez', 'Simmons', 'Romero', 'Jordan', 'Patterson', 'Alexander', 'Hamilton', 'Graham',
        'Reynolds', 'Griffin', 'Wallace', 'Moreno', 'West', 'Cole', 'Hayes', 'Bryant'
        ];

    middle_initial CHAR;
BEGIN
    -- 30% chance of having a middle initial
    IF random() < 0.3 THEN
        middle_initial := chr(65 + floor(random() * 26)::INT);
        RETURN first_names[1 + floor(random() * array_length(first_names, 1))::INT] || ' ' ||
               middle_initial || '. ' ||
               last_names[1 + floor(random() * array_length(last_names, 1))::INT];
    ELSE
        RETURN first_names[1 + floor(random() * array_length(first_names, 1))::INT] || ' ' ||
               last_names[1 + floor(random() * array_length(last_names, 1))::INT];
    END IF;
END;
$$ LANGUAGE plpgsql;

-- Insert 100,000 customers
INSERT INTO customers_denormalized_order (customer_name, customer_contact)
SELECT
    random_name(),
    random_contact()
FROM
    generate_series(1, 100000);

-- Clean up the temporary functions
DROP FUNCTION IF EXISTS random_contact();
DROP FUNCTION IF EXISTS random_name();