# Data Modification SQL Exercises for Banking System Analysis (Using PostgreSQL)

This document outlines a series of SQL exercises designed to help you practice 
and improve your foundational SQL skills in data modification using a banking system database.

## Database Schema

The exercises are based on the following database schema (see `resources/schema.sql` for details):

**Tables:**

* **Customers:** Stores personal and contact details of bank customers.
    * `customer_id` (SERIAL, PRIMARY KEY)
    * `first_name` (VARCHAR(50), NOT NULL)
    * `last_name` (VARCHAR(50), NOT NULL)
    * `email` (VARCHAR(100), UNIQUE NOT NULL)
    * `phone` (VARCHAR(20), UNIQUE NOT NULL)
    * `address` (TEXT, NOT NULL)
    * `created_at` (TIMESTAMP DEFAULT CURRENT_TIMESTAMP)
* **Employees:** Represents bank staff managing customer accounts.
    * `id` (SERIAL, PRIMARY KEY)
    * `first_name` (VARCHAR(50), NOT NULL)
    * `last_name` (VARCHAR(50), NOT NULL)
    * `position` (VARCHAR(50), NOT NULL)
    * `salary` (DECIMAL(10, 2), NOT NULL)
    * `created_at` (TIMESTAMP DEFAULT CURRENT_TIMESTAMP)
* **Challenges:** Tasks list.
    * `id` (SERIAL, PRIMARY KEY)
    * `challenge_name` (VARCHAR(55))
    * `challenge_task` (jsonb)
* **Orders:** Orders attached to employees.
    * `id` (SERIAL, PRIMARY KEY)
    * `order_name` (VARCHAR(55))
    * `amount` (INT)
    * `employee_id` (INT REFERENCES employees(id))

## Exercises and SQL File Mapping

The exercises contain of sql files where you should write SQL commands.
These SQL files are located in the `src/test/resources/queries`.
There are 3 groups of tasks to practice with data modification commands: insert, update and delete.
Each task has a comment. Please read comments as they show what table should be used and what should be done.
You can write your SQL commands below the comments or instead of them.

## Running the Tests

The provided `DataModificationTest.java` JUnit test class is designed to verify the correctness of your SQL queries.
The location is: `src/test/java/org/codeus/database/fundamentals/DataModificationTest.java`.
One @Test works with one SQL script. Make all them green.

The tests will automatically:

* Start an embedded PostgreSQL database.
* Create the required database schema for the task.
* Load the required test data.
* Execute each SQL query from the `queries/` directory.
* Assert the results against expected outcomes.

This setup allows for automated validation of your SQL queries as you work through the exercises. Good luck!