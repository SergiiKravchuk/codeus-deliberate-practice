-- Task 2.2: Complete Contact Audit (including duplicates)
-- Show ALL contact entries including duplicates for data quality analysis
SELECT
    email,
    'CUSTOMER' AS contact_type,
    CONCAT(first_name, ' ', last_name) AS full_name,
    1 AS record_count
FROM customers
UNION ALL
SELECT
    CONCAT(first_name, '.', last_name, '@bankinternal.com') AS email,
    'EMPLOYEE' AS contact_type,
    CONCAT(first_name, ' ', last_name) AS full_name,
    1 AS record_count
FROM employees
ORDER BY email, contact_type;