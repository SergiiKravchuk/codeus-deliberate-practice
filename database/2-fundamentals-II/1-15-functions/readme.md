# PL/pgSQL Functions

## Objective

This exercise series provides a comprehensive introduction to PL/pgSQL functions in PostgreSQL. 
Tasks are structured with progressively increasing difficulty, including optional challenges for advanced learners. 
You will master:

* Creating user-defined functions with various parameter types (including `OUT`) and return values (`INTEGER`, `NUMERIC`, `TEXT`, `BOOLEAN`, `TABLE`, `SETOF RECORD`, `TEXT[]`).
    <details>
    <summary>Show Example</summary>

    ```sql
    -- Function with IN and OUT parameters, returning INTEGER
    CREATE OR REPLACE FUNCTION add_numbers(
        p_num1 INT, 
        p_num2 INT, 
        OUT p_sum INT
    )
    AS $$
    BEGIN
        p_sum := p_num1 + p_num2;
    END;
    $$ LANGUAGE plpgsql;

    -- Function returning TABLE
    CREATE OR REPLACE FUNCTION get_users_in_department(p_dept_id INT)
    RETURNS TABLE (user_id INT, user_name TEXT)
    AS $$
    BEGIN
        RETURN QUERY SELECT id, name FROM users WHERE department_id = p_dept_id;
    END;
    $$ LANGUAGE plpgsql;

    -- Function returning SETOF RECORD (caller defines structure)
    CREATE OR REPLACE FUNCTION get_dynamic_data(p_table_name TEXT)
    RETURNS SETOF RECORD
    AS $$
    BEGIN
        RETURN QUERY EXECUTE format('SELECT * FROM %I', p_table_name);
    END;
    $$ LANGUAGE plpgsql;
    -- Usage: SELECT * FROM get_dynamic_data('my_table') AS (col1 INT, col2 TEXT);

    -- Function returning TEXT ARRAY
    CREATE OR REPLACE FUNCTION get_tags_for_post(p_post_id INT)
    RETURNS TEXT[]
    AS $$
    DECLARE
        v_tags TEXT[];
    BEGIN
        SELECT array_agg(tag_name) INTO v_tags FROM post_tags WHERE post_id = p_post_id;
        RETURN v_tags;
    END;
    $$ LANGUAGE plpgsql;
    ```
    </details>

* Declaring variables and effectively using `SELECT INTO`.
    <details>
    <summary>Show Example</summary>

    ```sql
    CREATE OR REPLACE FUNCTION get_product_name_and_price(p_product_id INT)
    RETURNS TEXT
    AS $$
    DECLARE
        v_product_name TEXT;
        v_price NUMERIC;
    BEGIN
        SELECT product_name, price 
        INTO v_product_name, v_price 
        FROM products 
        WHERE id = p_product_id;

        IF NOT FOUND THEN
            RETURN 'Product not found';
        END IF;

        RETURN 'Product: ' || v_product_name || ', Price: $' || v_price::TEXT;
    END;
    $$ LANGUAGE plpgsql;
    ```
    </details>

* Implementing conditional logic (`IF/ELSIF/ELSE`, `CASE`).
    <details>
    <summary>Show Example</summary>

    ```sql
    CREATE OR REPLACE FUNCTION get_discount_level(p_purchase_amount NUMERIC)
    RETURNS TEXT
    AS $$
    DECLARE
        v_discount_level TEXT;
    BEGIN
        IF p_purchase_amount >= 1000 THEN
            v_discount_level := 'Gold';
        ELSIF p_purchase_amount >= 500 THEN
            v_discount_level := 'Silver';
        ELSIF p_purchase_amount >= 100 THEN
            v_discount_level := 'Bronze';
        ELSE
            v_discount_level := 'No Discount';
        END IF;
        RETURN v_discount_level;

        -- Alternative using CASE
        /*
        CASE
            WHEN p_purchase_amount >= 1000 THEN v_discount_level := 'Gold';
            WHEN p_purchase_amount >= 500 THEN v_discount_level := 'Silver';
            WHEN p_purchase_amount >= 100 THEN v_discount_level := 'Bronze';
            ELSE v_discount_level := 'No Discount';
        END CASE;
        RETURN v_discount_level;
        */
    END;
    $$ LANGUAGE plpgsql;
    ```
    </details>

* Utilizing aggregate functions (`SUM`, `COUNT`, `AVG`, `array_agg`) within functions.
    <details>
    <summary>Show Example</summary>

    ```sql
    CREATE OR REPLACE FUNCTION get_order_summary(p_order_id INT)
    RETURNS RECORD -- Or define a specific composite type
    AS $$
    DECLARE
        v_total_items INT;
        v_total_amount NUMERIC;
        v_item_names TEXT[];
    BEGIN
        SELECT 
            COUNT(*), 
            SUM(price * quantity), 
            array_agg(product_name)
        INTO v_total_items, v_total_amount, v_item_names
        FROM order_items
        WHERE order_id = p_order_id;

        RETURN (v_total_items, v_total_amount, v_item_names);
    END;
    $$ LANGUAGE plpgsql;
    ```
    </details>

* Performing robust error handling (`RAISE EXCEPTION`, checking `FOUND`, custom exception blocks).
    <details>
    <summary>Show Example</summary>

    ```sql
    CREATE OR REPLACE FUNCTION transfer_funds(
        p_from_account_id INT, 
        p_to_account_id INT, 
        p_amount NUMERIC
    )
    RETURNS VOID
    AS $$
    DECLARE
        v_from_balance NUMERIC;
    BEGIN
        SELECT balance INTO v_from_balance FROM accounts WHERE id = p_from_account_id;
        IF NOT FOUND THEN
            RAISE EXCEPTION 'Source account % not found.', p_from_account_id;
        END IF;

        IF v_from_balance < p_amount THEN
            RAISE EXCEPTION 'Insufficient funds in account %.', p_from_account_id
                USING HINT = 'Check account balance before attempting transfer.',
                      ERRCODE = 'P0001'; -- Custom error code
        END IF;

        UPDATE accounts SET balance = balance - p_amount WHERE id = p_from_account_id;
        UPDATE accounts SET balance = balance + p_amount WHERE id = p_to_account_id;
        IF NOT FOUND THEN -- Check if target account exists after attempting update
            RAISE EXCEPTION 'Target account % not found. Funds debited but not credited.', p_to_account_id;
            -- In a real scenario, you'd handle transaction rollback here.
        END IF;

    EXCEPTION
        WHEN SQLSTATE 'P0001' THEN -- Catch custom exception
            RAISE NOTICE 'Custom Error: %', SQLERRM;
            -- Re-raise or handle as needed
            RAISE; 
        WHEN OTHERS THEN
            RAISE NOTICE 'An unexpected error occurred: %', SQLERRM;
            RAISE; -- Re-raise the original exception
    END;
    $$ LANGUAGE plpgsql;
    ```
    </details>

* Iterating over query results using `FOR ... LOOP`.
    <details>
    <summary>Show Example</summary>

    ```sql
    CREATE OR REPLACE FUNCTION process_pending_orders()
    RETURNS INT -- Returns count of processed orders
    AS $$
    DECLARE
        r RECORD;
        processed_count INT := 0;
    BEGIN
        FOR r IN SELECT order_id, customer_id FROM orders WHERE status = 'pending' LOOP
            -- Example processing:
            RAISE NOTICE 'Processing order ID % for customer ID %', r.order_id, r.customer_id;
            UPDATE orders SET status = 'processing' WHERE order_id = r.order_id;
            processed_count := processed_count + 1;
        END LOOP;
        RETURN processed_count;
    END;
    $$ LANGUAGE plpgsql;
    ```
    </details>

* Creating Set-Returning Functions (SRFs) that return multiple rows, including dynamically structured ones.
    <details>
    <summary>Show Example</summary>

    ```sql
    -- SRF returning a fixed structure (RETURNS TABLE)
    CREATE OR REPLACE FUNCTION get_active_users()
    RETURNS TABLE (user_id INT, email TEXT, last_login TIMESTAMP)
    AS $$
    BEGIN
        RETURN QUERY 
        SELECT id, user_email, last_login_time 
        FROM users 
        WHERE is_active = TRUE;
    END;
    $$ LANGUAGE plpgsql;

    -- SRF returning dynamic structure (RETURNS SETOF RECORD)
    -- Caller must define the structure with an AS clause
    CREATE OR REPLACE FUNCTION get_report_data(p_report_name TEXT)
    RETURNS SETOF RECORD
    AS $$
    BEGIN
        IF p_report_name = 'sales_summary' THEN
            RETURN QUERY SELECT product_id, SUM(quantity_sold) AS total_sold FROM sales GROUP BY product_id;
        ELSIF p_report_name = 'employee_list' THEN
            RETURN QUERY SELECT employee_id, first_name, last_name FROM employees;
        ELSE
            RAISE EXCEPTION 'Unknown report name: %', p_report_name;
        END IF;
    END;
    $$ LANGUAGE plpgsql;
    -- Usage: 
    -- SELECT * FROM get_report_data('sales_summary') AS (prod_id INT, sold_qty NUMERIC);
    -- SELECT * FROM get_report_data('employee_list') AS (emp_id INT, f_name TEXT, l_name TEXT);
    ```
    </details>

* Joining data from multiple tables within functions.
    <details>
    <summary>Show Example</summary>

    ```sql
    CREATE OR REPLACE FUNCTION get_customer_order_details(p_customer_id INT)
    RETURNS TABLE (
        order_id INT, 
        order_date TIMESTAMP, 
        product_name TEXT, 
        quantity INT, 
        price NUMERIC
    )
    AS $$
    BEGIN
        RETURN QUERY 
        SELECT o.id, o.order_date, p.name, oi.quantity, oi.unit_price
        FROM orders o
        JOIN order_items oi ON o.id = oi.order_id
        JOIN products p ON oi.product_id = p.id
        WHERE o.customer_id = p_customer_id
        ORDER BY o.order_date DESC, p.name;
    END;
    $$ LANGUAGE plpgsql;
    ```
    </details>

* Formatting complex output strings.
    <details>
    <summary>Show Example</summary>

    ```sql
    CREATE OR REPLACE FUNCTION format_address(
        p_street TEXT, 
        p_city TEXT, 
        p_state TEXT, 
        p_zip_code TEXT
    )
    RETURNS TEXT
    AS $$
    BEGIN
        RETURN format('%s, %s, %s %s', 
                      COALESCE(p_street, 'N/A'), 
                      COALESCE(p_city, 'N/A'), 
                      COALESCE(p_state, 'N/A'), 
                      COALESCE(p_zip_code, 'N/A'));
    END;
    $$ LANGUAGE plpgsql;
    ```
    </details>

* Basic dynamic SQL using `EXECUTE format()`.
    <details>
    <summary>Show Example</summary>

    ```sql
    CREATE OR REPLACE FUNCTION count_rows_in_table(p_table_name TEXT)
    RETURNS BIGINT
    AS $$
    DECLARE
        v_row_count BIGINT;
        v_query TEXT;
    BEGIN
        -- %I is for identifiers (table/column names) - provides quoting
        v_query := format('SELECT COUNT(*) FROM %I', p_table_name); 
        RAISE NOTICE 'Executing query: %', v_query;
        EXECUTE v_query INTO v_row_count;
        RETURN v_row_count;
    EXCEPTION
        WHEN OTHERS THEN
            RAISE WARNING 'Error counting rows in table %: %', p_table_name, SQLERRM;
            RETURN NULL; -- Or re-raise
    END;
    $$ LANGUAGE plpgsql;
    ```
    </details>

## Scenario

You will develop a suite of utility functions for a banking system, applying core PL/pgSQL concepts to solve practical problems. We will use our established database schema including `customers`, `accounts`, `transactions`, `loans`, etc.

## Tasks

Implement the following functions in their respective SQL files located in the `src/test/resources/queries/` directory. Students should write the complete function definition from `CREATE OR REPLACE FUNCTION...` down to `LANGUAGE plpgsql;` based on the "Function Signature to implement" and "Logic" provided in the header comments of each template file.

### Task 1: Get Customer Email by ID (`01_get_customer_email.sql`)

* **Objective:** Basic function structure, parameter handling, a simple `SELECT INTO` for a single variable, and `FOUND` variable usage.
* **Signature to implement:** `get_customer_email(p_customer_id INT) RETURNS TEXT`
* **Logic:**
    1. Declare a `TEXT` variable `v_email`.
    2. Retrieve `email` for `p_customer_id` from `customers` into `v_email`.
    3. If `NOT FOUND`, raise exception `Customer not found: %`.
    4. Return `v_email`.

### Task 2: Get Customer Full Name (`02_get_customer_full_name.sql`)

* **Objective:** `SELECT INTO` for multiple variables, string concatenation, error handling.
* **Signature to implement:** `get_customer_full_name(p_customer_id INT) RETURNS TEXT`
* **Logic:**
    1. Declare `TEXT` variables `v_first_name`, `v_last_name`, `v_full_name`.
    2. Retrieve `first_name`, `last_name` for `p_customer_id` into respective variables.
    3. If `NOT FOUND`, raise exception `Customer not found: %`.
    4. Concatenate names into `v_full_name` (e.g., `trim(COALESCE(v_first_name, '') || ' ' || COALESCE(v_last_name, ''))`). If result is empty, return `NULL`.
    5. Return `v_full_name`.

### Task 3: Check if Balance Exceeds Threshold (`03_check_if_balance_exceeds_threshold.sql`)

* **Objective:** Boolean return type, simple database query with a condition.
* **Signature to implement:** `check_if_balance_exceeds_threshold(p_account_id INT, p_threshold NUMERIC) RETURNS BOOLEAN`
* **Logic:** Retrieve `balance` for `p_account_id`. Return `TRUE` if `balance > p_threshold`, otherwise `FALSE`. Handle account not found (raise an exception: `Account not found: %`).

### Task 4: Get Account Balance (`04_get_account_balance.sql`)

* **Objective:** Using `SELECT INTO` and the `FOUND` special variable.
* **Signature to implement:** `get_account_balance(p_account_id INT) RETURNS NUMERIC`
* **Logic:** Retrieve `balance` for `p_account_id`. If `NOT FOUND`, raise `Account not found: %`.

### Task 5: Calculate Interest for a Given Balance (`05_calculate_interest_for_balance.sql`)

* **Objective:** Conditional logic (`CASE` or `IF`) without database interaction, input validation.
* **Signature to implement:** `calculate_interest_for_balance(p_balance NUMERIC) RETURNS NUMERIC`
* **Logic:** If `p_balance` is `NULL` or `< 0`, return `0.00`. Apply tiered interest rates (0.5% for <1000; 1.0% for <5000; 1.5% for <20000; 2.0% for >=20000).

### Task 6: Get Customer's Total Balance (`06_get_customer_total_balance.sql`)

* **Objective:** Using `SELECT EXISTS` for pre-check, `SUM` aggregate, and `COALESCE`.
* **Signature to implement:** `get_customer_total_balance(p_customer_id INT) RETURNS NUMERIC`
* **Logic:** Check if customer exists (raise `Customer not found: %` if not). Return `COALESCE(SUM(balance), 0.00)` for all accounts of the customer.

### Task 7: Count Transactions by Type for Account (`07_count_transactions_by_type_for_account.sql`)

* **Objective:** Using `COUNT(*)` aggregate with multiple `WHERE` conditions.
* **Signature to implement:** `count_transactions_by_type_for_account(p_account_id INT, p_transaction_type TEXT) RETURNS INT`
* **Logic:** Count transactions for `p_account_id` matching `p_transaction_type`. If account not found, return `0`.

### Task 8: Get Transactions for Account (SRF) (`08_get_transactions_for_account.sql`)

* **Objective:** Creating a Set-Returning Function (SRF) using `RETURNS TABLE`.
* **Signature to implement:** `get_transactions_for_account(p_account_id INT) RETURNS TABLE (transaction_id INT, transaction_type TEXT, amount NUMERIC, transaction_date TIMESTAMP, description TEXT)`
* **Logic:** Use `RETURN QUERY` to select specified transaction details for `p_account_id`, ordered by `transaction_date` DESC.

### Task 9: Get Account Activity Summary (`09_get_account_activity_summary.sql`)

* **Objective:** Using `FOR ... LOOP` to iterate over query results and perform string concatenation.
* **Signature to implement:** `get_account_activity_summary(p_account_id INT, p_transaction_limit INT) RETURNS TEXT`
* **Logic:** Check account existence. Loop through recent transactions (up to `p_transaction_limit`), build a formatted summary string. Handle "No transactions found."

### Task 10: Get Customer Account Numbers as Array (`10_get_customer_account_numbers_array.sql`)

* **Objective:** Working with array data types and the `array_agg` function.
* **Signature to implement:** `get_customer_account_numbers_array(p_customer_id INT) RETURNS TEXT[]`
* **Logic:** Check customer existence. Return an array of `account_number`s for the customer using `array_agg`. Return an empty array (`{}`) if no accounts or customer not found.

### Task 11: Format Loan Details String (`11_format_loan_details.sql`)

* **Objective:** Joining multiple tables, using `RECORD` type, advanced string formatting, `COALESCE` for NULL handling.
* **Signature to implement:** `format_loan_details(p_loan_id INT) RETURNS TEXT`
* **Logic:** Fetch details for `p_loan_id` by joining `loans` and `customers`. Construct a comprehensive formatted string. Handle `Loan not found.`.

### Task 12: Determine Account Status Detailed (`12_determine_account_status_detailed.sql`)

* **Objective:** Implementing more complex conditional logic based on multiple fields.
* **Signature to implement:** `determine_account_status_detailed(p_account_id INT) RETURNS TEXT`
* **Logic:** Retrieve `balance` and `status` from `accounts`. Based on these, return "Closed", "Frozen", "Overdrawn", "Empty", or "Active with Positive Balance". Handle `Account not found: %`.

### Task 13: Get Account Details with OUT Parameters (`13_get_account_details_with_out_params.sql`)

* **Objective:** Using `OUT` parameters to return multiple values from a function.
* **Function Signature to implement:**
    ```sql
    FUNCTION get_account_details_with_out_params(
        p_account_id INT,
        OUT o_account_number TEXT,
        OUT o_account_type TEXT,
        OUT o_balance NUMERIC,
        OUT o_is_active BOOLEAN
    )
    ```
* **Logic:** Query `accounts` for `p_account_id`. Assign retrieved values to `OUT` parameters. Handle `Account not found: %`.

### Task 14: Get Filtered Transactions (Dynamic SRF with SETOF RECORD) (`14_get_filtered_transactions_dynamic_srf.sql`)

* **Objective:** Create a Set-Returning Function using `SETOF RECORD` where the calling query defines the output structure.
* **Signature to implement:** `get_filtered_transactions_dynamic_srf(p_account_id INT, p_min_amount NUMERIC DEFAULT 0) RETURNS SETOF RECORD`
* **Logic:** Use `RETURN QUERY` to select transactions for `p_account_id` where `amount > p_min_amount`. Caller defines output columns.

### Task 15: Get Column Value Dynamically (EXECUTE format) (`15_get_column_value_from_table_dynamic.sql`)

* **Objective:** Introduce dynamic SQL using `EXECUTE` and `format()`. **Use with extreme caution.**
* **Signature to implement:** `get_column_value_from_table_dynamic(p_table_name TEXT, p_column_name TEXT, p_id_column_name TEXT, p_id_value INT) RETURNS TEXT`
* **Logic:** Construct and execute a dynamic query using `format()` (with `%I` and `%L`). Handle `NOT FOUND`. Emphasize SQL injection risks and need for input validation in real scenarios.

---

## Database Schema & Data

We will use the established, more comprehensive schema (`customers`, `accounts`, `transactions`, `loans`, etc.) and corresponding `test-data.sql`.

## Running the Tests

The JUnit test class `FunctionsTest.java` will verify your functions. Ensure your Java environment is set up, then run the test class.

Good luck!
