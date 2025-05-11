# Data Types in PostgreSQL

This module contains exercises on complex data types such as: ENUM, ARRAY, RANGE, COMPOSITE and DOMAIN.

---

### Goal
Solve all exercise and make all tests pass in the [SqlQueriesTest.java](src%2Ftest%2Fjava%2Forg%2Fcodeus%2Ffundamentals%2Fdata_types%2FSqlQueriesTest.java).

#### Extra 🔥
Play around with type constraints violations, you can use [playground.sql](src%2Ftest%2Fresources%2Fplayground.sql) and `org.codeus.fundamentals.data_types.SqlQueriesTest#playground` test for that.


### Tips
* run all tests or all tests in nested class because tests for one block are dependent.
* solve exercises in the provided order, again each exercise within block depend on previous ones (e.g. exercise 1-4 depends on exercise 1-1)
* pay attention to proposed type names

### Useful links
* PostgreSQL 17 Docs Data Types: https://www.postgresql.org/docs/17/datatype.html
* PostgreSQL 17 Docs Alter Types: https://www.postgresql.org/docs/current/sql-altertype.html
* PostgreSQL 17 Docs Arrays/Ranges operators: https://www.postgresql.org/docs/17/functions-range.html
* PostgreSQL 17 Docs Operators: https://www.postgresql.org/docs/current/typeconv-oper.html


### Exercises:
#### [1-enum-type](src%2Ftest%2Fresources%2Fexercises%2F1-enum-type)
#### [2-array-type](src%2Ftest%2Fresources%2Fexercises%2F2-array-type)
#### [3-range-type](src%2Ftest%2Fresources%2Fexercises%2F3-range-type)
#### [4-composite-type](src%2Ftest%2Fresources%2Fexercises%2F4-composite-type)
#### [5-domain-type](src%2Ftest%2Fresources%2Fexercises%2F5-domain-type)

<details> 
<summary>Hints (click to expand)</summary>
<details> 
<summary>Pattern for us_phone type (click to expand)</summary>
^[0-9]{10}$
</details>
</details>

**Good luck!** 🍀