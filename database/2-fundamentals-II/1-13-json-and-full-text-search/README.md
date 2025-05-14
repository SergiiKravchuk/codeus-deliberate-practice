# Full-text search and JSON practice (Using PostgreSQL)

This document outlines a series of SQL exercises designed to help you practice 
and improve your foundational SQL skills in full text search and json manipulation.

## Database Schema

The exercises are based on the following database schema (see `resources/schema.sql` for details):

**Tables:**

* **Quotes:** Stores person name with their quote.
    * `id` (SERIAL, PRIMARY KEY)
    * `person` (TEXT)
    * `quote` (TEXT)
* **Posts:** Represents post title with text body
    * `id` (SERIAL, PRIMARY KEY)
    * `title` (TEXT)
    * `body` (TEXT)
* **History Facts:** Title and jsonb details for the history facts.
    * `id` (SERIAL, PRIMARY KEY)
    * `title` (TEXT)
    * `details` (jsonb)

## Exercises and SQL File Mapping

The exercises contain of sql files where you should write SQL commands.
These SQL files are located in the `src/test/resources/queries`.
There are 2 groups of tasks to practice with full text search and json selects.
Each task has a comment. Please read comments as they show what table should be used and what should be done.
You can write your SQL commands below the comments or instead of them.

## Running the Tests

The provided `SqlQueriesTest.java` JUnit test class is designed to verify the correctness of your SQL queries.
The location is: `src/test/java/org/codeus/fundamentals/json_and_full_text_search/SqlQueriesTest.java`.
One @Test works with one SQL script. Make all them green.

The tests will automatically:

* Start an embedded PostgreSQL database.
* Create the required database schema for the task.
* Load the required test data.
* Execute each SQL query from the `queries/` directory.
* Assert the results against expected outcomes.

This setup allows for automated validation of your SQL queries as you work through the exercises. Good luck!