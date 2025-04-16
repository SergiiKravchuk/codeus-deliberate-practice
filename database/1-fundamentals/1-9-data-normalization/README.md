# Database Normalization Exercise

This project is a hands-on exercise to learn and apply database normalization concepts through practical examples.
You will work through the normalization process step by step, from 1NF to 3NF, while observing the impact on data
integrity, storage efficiency, and query operations.

## Overview

The exercise focuses on a banking system database with customer information, contact details, and reliability scores.
Through a series of tasks, you will:

1. Identify violations of normal forms
2. Fix anomalies by transforming tables
3. Observe the impact of normalization on data integrity and storage
4. Test your implementations

### Running the Tests

Tests: [SqlQueriesTest](src/test/java/SqlQueriesTest.java)
Also, you can run separately tests for each Normal Form, for this you can run from nested
classes [FirstNF, SecondNF, ThirdNF]

## Tips for Success

1. **Read the Console Output**: It contains valuable explanations and insights
2. **Compare Table Sizes**: Notice how normalization affects storage
3. **Step-by-Step**: Complete tasks in order, as each builds on the previous
4. **Ask Questions**: If you get stuck, ask about the specific normal form violation

## Tasks Overview

<details> 
<summary><b>First Normal Form (1NF)</b></summary>

1. **Task 1.1**: Modify tables to achieve row order independence
    - Problem: Reliability scores depend on row position (violates 1NF)
    - Solution: Add explicit `reliability_score` column

2. **Task 1.2**: Add primary key support
    - Problem: No way to uniquely identify customers
    - Solution: Add SSN as primary key

3. **Task 1.3.1**: Remove repeating groups (basic approach)
    - Problem: Contact information stored as a single concatenated string
    - Solution: Split into separate columns (still not fully 1NF)

4. **Task 1.3.2**: Properly remove repeating groups
    - Problem: Limited fixed contact types per customer
    - Solution: Create flexible structure with contact_type/contact_value pairs

5. **Optional Tasks**: Practice SELECT, UPDATE, DELETE operations in normalized to 1NF and denormalized tables

</details>

<details> 
<summary><b>Second Normal Form (2NF)</b></summary>

1. **Task 2.1**: Address deletion anomaly
    - Problem: Cannot update reliability score without affecting all customer data
    - Observation: Deletion anomalies in current structure

2. **Task 2.2**: Address update anomaly
    - Problem: Inconsistent reliability scores for same person
    - Observation: Update anomalies in current structure

3. **Task 2.3**: Migrate to 2NF
    - Problem: Non-key attributes depend on part of the composite key
    - Solution: Split into customers and customer_contacts tables

</details>

<details> 
<summary><b>Third Normal Form (3NF)</b></summary>

1. **Task 3.1.1**: Add numerical reliability score
    - Problem: Need numeric representation of reliability scores
    - Implementation: Add and map reliability_score_num

2. **Task 3.1.2**: Observe data integrity loss
    - Problem: Transitive dependency between ssn, reliability_score and reliability_score_num
    - Observation: Data integrity issues

3. **Task 3.2**: Update to 3NF
    - Problem: Transitive dependencies
    - Solution: Extract reliability scores to separate table

4. **Task 3.3**: Verify data integrity
    - Observation: Properly normalized structure maintains data integrity

</details>

## Key Takeaways

<details> 
<summary><b>As you complete each task, you'll observe:</b></summary>

1. **Storage Impact**:
    - Normalization sometimes increases storage (1NF)
    - But can decrease storage by removing redundancy (3NF)

2. **Data Integrity**:
    - 1NF prevents duplicate records and ensures atomic values
    - 2NF prevents update/delete anomalies related to functional dependencies
    - 3NF prevents transitive dependencies preserving "the whole key and nothing but the key"

3. **Query Complexity**:
    - 1NF often simplifies updates/deletes to specific data
    - 2NF/3NF require joins for some queries, but ensure consistency

</details>

Good luck with your normalization journey! Remember that properly normalized databases lead to better data integrity,
reduced redundancy, and more maintainable systems.