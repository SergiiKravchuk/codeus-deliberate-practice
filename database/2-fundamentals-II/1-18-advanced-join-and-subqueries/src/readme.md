
## Database Schema

All information about the database schema can be found in the `schema.sql` file.
All information about populated data can be found in the `test-data.sql` file.

The banking system schema includes the following core entities:
- **CUSTOMERS**: Customer personal information and contact details
- **ACCOUNTS**: Bank accounts with balances and account types
- **LOANS**: Customer loans with amounts, rates, and status
- **TRANSACTIONS**: Transaction history for all accounts
- **EMPLOYEES**: Bank employee information for contact consolidation exercises

## Exercises and SQL File Mapping

SQL files are located in the `src/test/resources/` directory and are organized by advanced SQL operation types. Each section focuses on specific advanced techniques with real-world business applications.

Tasks are numbered sequentially, aligning with the order of the corresponding tests. Inside every SQL file, a detailed description of the business requirement and expected query logic is provided.

### Task Structure

#### **Section 1: Advanced JOIN Operations**
- **Task 1**: `01_1_full_join_customer_loan_analysis.sql`
  - **Technique**: FULL OUTER JOIN
  - **Business Goal**: Complete customer-loan relationship analysis
  - **Skills**: NULL handling, relationship classification

#### **Section 2: Set Operations for Data Consolidation**
- **Task 2.1**: `02_1_union_contact_consolidation.sql`
  - **Technique**: UNION (deduplicated)
  - **Business Goal**: Consolidated contact list from customers and employees
  - **Skills**: Data source integration, duplicate elimination

- **Task 2.2**: `02_2_union_contact_consolidation_deduplicated.sql`
  - **Technique**: UNION ALL (including duplicates)
  - **Business Goal**: Complete contact audit trail
  - **Skills**: Data preservation, audit requirements

#### **Section 3: Cross-Selling Analysis**
- **Task 3**: `03_1_cross_join_product_matrix.sql`
  - **Technique**: CROSS JOIN
  - **Business Goal**: Product recommendation matrix for cross-selling
  - **Skills**: Cartesian products, opportunity identification

#### **Section 4: Risk Analysis and Compliance**
- **Task 4**: `04_1_except_risk_analysis.sql`
  - **Technique**: EXCEPT (set difference)
  - **Business Goal**: Identify loan prospects among account holders
  - **Skills**: Set operations, risk assessment

#### **Section 5: Advanced Correlated Analysis**
- **Task 5.1**: `05_1_lateral_join_transaction_analysis.sql`
  - **Technique**: LATERAL JOIN
  - **Business Goal**: Account-level transaction analysis
  - **Skills**: Correlated subqueries, pattern analysis

- **Task 5.2**: `05_2_lateral_join_transaction_analysis.sql`
  - **Technique**: LATERAL JOIN with aggregations
  - **Business Goal**: Customer transaction behavior classification
  - **Skills**: Complex aggregations, behavioral analysis

#### **Section 6: Performance Optimization**
- **Task 6.1**: `06_1_performance_comparison_analysis.sql`
  - **Technique**: EXISTS clause
  - **Business Goal**: Find customers with high-value transactions
  - **Skills**: Existence checking, performance optimization

- **Task 6.2**: `06_2_performance_comparison_in.sql`
  - **Technique**: IN clause with subquery
  - **Business Goal**: Same business requirement using IN approach
  - **Skills**: Subquery optimization, NULL handling

- **Task 6.3**: `06_3_performance_comparison_join.sql`
  - **Technique**: JOIN approach
  - **Business Goal**: Same business requirement using JOIN approach
  - **Skills**: JOIN optimization, data retrieval

#### **Section 7: VIP Customer Analysis**
- **Task 7**: `07_1_intersect_vip_analysis.sql`
  - **Technique**: INTERSECT (set intersection)
  - **Business Goal**: Identify VIP customers with both high balances and high loans
  - **Skills**: Set intersection, customer segmentation

## Running the Tests

The provided test framework includes multiple specialized test classes designed to verify the correctness, performance, and business logic of your SQL queries:

### **Test Classes**
- **`JoinOperationsTest`**: Validates JOIN-related exercises (Tasks 1, 3, 5.1, 5.2)
- **`SetOperationsTest`**: Validates set operations (Tasks 2.1, 2.2, 4, 7)
- **`PerformanceComparisonTest`**: Validates performance exercises (Tasks 6.1, 6.2, 6.3)

The test framework automatically:
- **Database Setup**: Starts an embedded PostgreSQL database
- **Schema Creation**: Creates the complete banking system schema
- **Data Loading**: Loads realistic test data with customers, accounts, loans, and transactions
- **Query Execution**: Executes each SQL query from the respective files
- **Business Logic Validation**: Validates results against real business rules
- **Performance Measurement**: Measures and compares query execution times
- **Data Accuracy Verification**: Ensures calculations and aggregations are correct
- **Result Structure Validation**: Confirms proper output format and required fields

**Good luck with your advanced SQL journey!**