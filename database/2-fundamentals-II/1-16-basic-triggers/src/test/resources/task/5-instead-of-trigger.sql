-- ====================================================================================================================
-- TASK 5: Create an INSTEAD OF trigger for a view-based customer account summary
-- ====================================================================================================================
-- First, create a view `customer_account_summary_view` that shows customer information with their total balance
CREATE VIEW customer_account_summary_view AS
SELECT
    c.id as customer_id,
    c.first_name,
    c.last_name,
    c.email,
    COUNT(a.id) as account_count,
    COALESCE(SUM(a.balance), 0) as total_balance
FROM customers c
         LEFT JOIN accounts a ON c.id = a.customer_id
GROUP BY c.id, c.first_name, c.last_name, c.email;

-- TODO:
-- 1. Create a function named `redirect_customer_account_summary_view_updates` that will:
--    - Handle UPDATE operations on the `customer_account_summary_view` view
--    - Update the underlying `customers` table with the new `first_name`, `last_name`, and `email`
--    - Ignore the `account_count` and `total_balance` fields (as they are calculated fields)
CREATE OR REPLACE FUNCTION redirect_customer_account_summary_view_updates()
    RETURNS TRIGGER AS $$
BEGIN
    UPDATE customers
    SET
        first_name = NEW.first_name,
        last_name = NEW.last_name,
        email = NEW.email
    WHERE id = OLD.customer_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 2. Create a trigger named `trg_instead_of_update_customer_summary` that will:
--    - Execute INSTEAD OF UPDATE on the `customer_account_summary_view` view
--    - Fire FOR EACH ROW
--    - Call the `redirect_customer_account_summary_view_updates` function
CREATE TRIGGER trg_instead_of_update_customer_summary
    INSTEAD OF UPDATE ON customer_account_summary_view
    FOR EACH ROW
EXECUTE FUNCTION redirect_customer_account_summary_view_updates();

-- 3. Create a function named `redirect_customer_account_summary_view_deletes` that will:
--    - Handle DELETE operations on the `customer_account_summary_view` view
--    - Delete the customer from the `customers` table (cascading deletes will handle accounts)
CREATE OR REPLACE FUNCTION redirect_customer_account_summary_view_deletes()
    RETURNS TRIGGER AS $$
BEGIN
    DELETE FROM customers
    WHERE id = OLD.customer_id;

    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

-- 4. Create a trigger named `trg_instead_of_delete_customer_summary` that will:
--    - Execute INSTEAD OF DELETE on the `customer_account_summary_view` view
--    - Fire FOR EACH ROW
--    - Call the `redirect_customer_account_summary_view_deletes` function
CREATE TRIGGER trg_instead_of_delete_customer_summary
    INSTEAD OF DELETE ON customer_account_summary_view
    FOR EACH ROW
EXECUTE FUNCTION redirect_customer_account_summary_view_deletes();