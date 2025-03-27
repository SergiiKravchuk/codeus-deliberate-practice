# SQL Exercises for Banking System Analysis (Using PostgreSQL)

This document outlines a series of SQL exercises designed to help you practice and improve your fundamental SQL skills using Aggregation functions and Grouping. 
These exercises specifically focus on the core concepts of `COUNT()`, `SUM()`, `AVG()`, `MIN()`, `MAX()`, `GROUPING BY` and `HAVING` in PostgreSQL.

## Database Schema

All information about DB schema could be found in `schema.sql` file.

All information about populated data could be found in `test-data.sql` file.


## Exercises and SQL File Mapping

SQL files are located in the `src/test/resources/queries/` directory and are divided into two main sections: Mandatory and Optional. 
Each section is further organized into subtopics.

Tasks are numbered sequentially, aligning with the order of the corresponding tests. 
Inside every sql file - description of the query is located.


## Running the Tests

The provided `SqlQueriesTest.java` JUnit test class is designed to verify the correctness of your SQL queries. To run the tests:

1.  Ensure you have Maven or Gradle set up for your Java project.
2.  Download all required dependencies. 
3.  Run the `SqlQueriesTest` class as a JUnit test in your IDE or using Maven/Gradle command line (e.g., `mvn test` or `gradle test`).

The tests will automatically:

* Start an embedded PostgreSQL database.
* Create the database schema.
* Load test data.
* Execute each SQL query from the `queries/` directory.
* Assert the results against expected outcomes.

This setup allows for automated validation of your SQL queries as you work through the exercises. Good luck!