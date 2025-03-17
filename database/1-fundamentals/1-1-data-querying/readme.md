# Base SQL Exercises for Banking System Analysis (Using PostgreSQL)

This document outlines a series of SQL exercises designed to help you practice and improve your foundational SQL skills using a banking system database. These exercises specifically focus on the core concepts of `SELECT`, `WHERE`, `ORDER BY`, and filtering conditions in PostgreSQL.

## Database Schema

The exercises are based on the following database schema:

**Tables:**

* **Customers:** Stores personal and contact details of bank customers.
    * `customer_id` (SERIAL, PRIMARY KEY)
    * `customer_name` (VARCHAR(255), NOT NULL)
    * `city` (VARCHAR(255), NOT NULL)
    * `contact_details` (VARCHAR(255))
* **Accounts:** Represents customer bank accounts (checking, savings).
    * `account_number` (VARCHAR(20), PRIMARY KEY)
    * `customer_id` (INT, REFERENCES Customers(customer_id))
    * `account_type` (VARCHAR(50), NOT NULL)
    * `balance` (DECIMAL(15, 2), NOT NULL)
* **Transactions:** Logs deposits, withdrawals, and transfers.
    * `transaction_id` (SERIAL, PRIMARY KEY)
    * `account_number` (VARCHAR(20), REFERENCES Accounts(account_number))
    * `transaction_type` (VARCHAR(50), NOT NULL)
    * `transaction_date` (DATE, NOT NULL)
    * `amount` (DECIMAL(15, 2), NOT NULL)
* **Loans:** Manages loan details and repayments.
    * `loan_id` (SERIAL, PRIMARY KEY)
    * `customer_id` (INT, REFERENCES Customers(customer_id))
    * `loan_amount` (DECIMAL(15, 2), NOT NULL)
    * `interest_rate` (DECIMAL(5, 2), NOT NULL)
    * `loan_term_months` (INT, NOT NULL)
* **Employees:** Represents bank staff managing customer accounts.
    * `employee_id` (SERIAL, PRIMARY KEY)
    * `employee_name` (VARCHAR(255), NOT NULL)
    * `department` (VARCHAR(255), NOT NULL)
* **Branches:** Physical bank locations.
    * `branch_id` (SERIAL, PRIMARY KEY)
    * `branch_name` (VARCHAR(255), NOT NULL)
    * `location` (VARCHAR(255), NOT NULL)

## Exercises and SQL File Mapping

Below is a list of exercises ordered by increasing difficulty, along with the corresponding SQL file name that contains the solution query. These SQL files are located in the `src/test/resources/warmup` and `src/test/resources/main` directories of the test package.

**Warm-up Exercises:**

1.  **Exercise:** List All Customer Names.
    * **SQL File:** `1_select_all_customer_names_easy_warmup.sql`
    * **Description:** Retrieve a list of all customer names from the `Customers` table.

2.  **Exercise:** List Customers from Kyiv, Ordered by Name.
    * **SQL File:** `2_select_kyiv_customers_ordered_by_name_easy_warmup.sql`
    * **Description:** Retrieve the names of all customers who live in 'Kyiv', ordered alphabetically by their name.

**Main Exercises (Ordered by Difficulty):**

3.  **Exercise:** List customers from 'Kyiv' or 'Lviv'.
    * **SQL File:** `3_select_customers_kyiv_lviv.sql`
    * **Description:** Retrieve a list of all customers who live in either 'Kyiv' or 'Lviv', showing their names.

4.  **Exercise:** List unique customer cities, sorted alphabetically.
    * **SQL File:** `4_select_unique_customer_cities_sorted.sql`
    * **Description:** Get a list of all unique cities where customers reside, sorted in alphabetical order.

5.  **Exercise:** Find savings accounts with a balance between 10000 and 50000.
    * **SQL File:** `5_select_savings_accounts_balance_between.sql`
    * **Description:** Find all savings accounts that have a balance between 10000 and 50000 (inclusive).

6.  **Exercise:** Find current accounts with a balance less than 1000 or greater than 100000.
    * **SQL File:** `6_select_current_accounts_balance_out_of_range.sql`
    * **Description:** Retrieve all current (checking) accounts that have a balance either less than 1000 or greater than 100000.

7.  **Exercise:** List all customers with checking accounts.
    * **SQL File:** `7_list_customers_with_checking_accounts_base.sql`
    * **Description:** Retrieve a list of all customers who have at least one checking account.

8.  **Exercise:** Find transactions above $10,000, sorted by date.
    * **SQL File:** `8_find_transactions_above_10000_sorted_by_date_base.sql`
    * **Description:** Retrieve all transactions with an amount greater than $10,000, sorted chronologically by transaction date.

9.  **Exercise:** Find customers whose names start with 'M' and contain 'а'.
    * **SQL File:** `9_select_customers_m_and_a.sql`
    * **Description:** Find customers whose names begin with the letter 'M' (case-insensitive) and also contain the letter 'а' (case-insensitive) anywhere in their name.

10. **Exercise:** List customers not from 'Kharkiv'.
    * **SQL File:** `10_select_customers_ukraine_not_kharkiv.sql`
    * **Description:** Retrieve a list of customers who do not live in the city 'Kharkiv'.

11. **Exercise:** List accounts sorted first by account type (savings then checking), and then by balance in descending order within each type.
    * **SQL File:** `11_select_accounts_sorted_by_type_and_balance.sql`
    * **Description:** Retrieve all accounts, ordering them first by account type ('savings' accounts before 'checking' accounts), and within each account type, sort by balance in descending order.

12. **Exercise:** Search for transactions with descriptions similar to 'deposit' using LIKE (case-sensitive).
    * **SQL File:** `12_search_transaction_descriptions_like_base.sql` *(Conceptual - using `transaction_type` as a proxy)*
    * **Description:** Find transactions with `transaction_type` similar to 'deposit' (case-sensitive).

13. **Exercise:** Search for transactions with descriptions similar to 'deposit' using ILIKE (case-insensitive).
    * **SQL File:** `13_search_transaction_descriptions_ilike_base.sql`
    * **Description:** Find transactions with `transaction_type` similar to 'deposit' (case-insensitive).

14. **Exercise:** Search for transactions where the `transaction_type` starts with the word 'deposit' (case-insensitive).
    * **SQL File:** `14_search_transaction_deposit_iliike_base.sql`
    * **Description:** Find transactions where `transaction_type` starts with 'deposit' (case-insensitive).

15. **Exercise:** Search for transactions where the `transaction_type` contains the word 'draw' (case-sensitive).
    * **SQL File:** `15_search_transaction_draw_like_base.sql`
    * **Description:** Find transactions where `transaction_type` contains 'draw' (case-sensitive).

16. **Exercise:** Customers with savings accounts in 'Kyiv' or 'Lviv'.
    * **SQL File:** `16_customers_with_savings_accounts_kyiv_lviv_base.sql`
    * **Description:** Retrieve a list of customers who have savings accounts and live in either 'Kyiv' or 'Lviv', ordered by city and then customer name.

17. **Exercise:** Find high-value transactions in September 2023.
    * **SQL File:** `17_high_value_transactions_september_2023_base.sql`
    * **Description:** Retrieve all transactions that occurred in September 2023 and have an amount greater than $8,000. Display the transaction date, account number, transaction type, and amount. Order the results by transaction date, then by amount in descending order.

## Running the Tests

The provided `SqlQueriesTest.java` JUnit test class is designed to verify the correctness of your SQL queries. To run the tests:

1.  Ensure you have Maven or Gradle set up for your Java project.
2.  Add the `embedded-postgres` dependency to your `pom.xml` (Maven) or `build.gradle` (Gradle) file.
3.  Place the `SqlQueriesTest.java` file in `src/test/java/tuesday/`.
4.  Place `schema.sql`, `test-data.sql`, and the `queries/` directory with all the SQL query files in `src/test/resources/`.
5.  Run the `SqlQueriesTest` class as a JUnit test in your IDE or using Maven/Gradle command line (e.g., `mvn test` or `gradle test`).

The tests will automatically:

* Start an embedded PostgreSQL database.
* Create the database schema.
* Load test data.
* Execute each SQL query from the `queries/` directory.
* Assert the results against expected outcomes.

This setup allows for automated validation of your SQL queries as you work through the exercises. Good luck!