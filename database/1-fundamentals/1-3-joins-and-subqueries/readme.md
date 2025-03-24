# SQL Joins and CTEs Workshop

This workshop is designed to help you master SQL JOINs and Common Table Expressions (CTEs) using a banking database
system.

## Project Structure

The workshop consists of mandatory and optional tasks, organized by SQL concepts.

### Prerequisites

- Basic SQL knowledge
- PostgreSQL database
- Java development environment (for running tests)

## Tasks

### Mandatory Tasks

#### 1. INNER JOIN

Learn basic table joining with these introductory tasks:

- [Basic Customer Accounts](src/test/resources/mandatory/1_inner_join/m1_basic_customer_accounts.sql)
- [Employee Branch Location](src/test/resources/mandatory/1_inner_join/m2_employee_branch_location.sql)

#### 2. LEFT JOIN

Practice including all records from the left table:

- [Customers with Optional Loans](src/test/resources/mandatory/2_left_join/m3_customers_with_optional_loans.sql)
- [Branches Employee Count](src/test/resources/mandatory/2_left_join/m4_branches_employee_count.sql)

#### 3. RIGHT JOIN

Understand right table inclusion:

- [Accounts with Transactions](src/test/resources/mandatory/3_right_join/m5_accounts_with_transactions.sql)
- [Loans with Branch Info](src/test/resources/mandatory/3_right_join/m6_loans_with_branch_info.sql)

#### 4. Common Table Expressions (CTE)

Master the use of WITH clause and CTEs:

- [Customer Transaction Totals](src/test/resources/mandatory/4_cte/m7_customer_transaction_totals.sql)
- [Active Branches](src/test/resources/mandatory/4_cte/m8_active_branches.sql)

### Optional Advanced Tasks

For those wanting to challenge themselves:

- [Branch Summary](src/test/resources/optional/o9_branch_summary.sql)
- [Active Employees](src/test/resources/optional/o10_active_employees.sql)
- [Customer Products](src/test/resources/optional/o11_customer_products.sql)
- [Rich Customers](src/test/resources/optional/o12_rich_customers.sql)
- [Busy Branches](src/test/resources/optional/o13_busy_branches.sql)
- [Customer Transaction Summary](src/test/resources/optional/o14_customer_transaction_summary.sql)
- [Branch Loan Summary](src/test/resources/optional/o15_branch_loan_summary.sql)
- [Active Accounts Summary](src/test/resources/optional/o16_active_accounts_summary.sql)
- [Employee Performance Summary](src/test/resources/optional/o17_employee_performance_summary.sql)

### Optional Advanced Tasks with Multiple CTEs

For those ready for complex analysis and advanced SQL techniques:

#### [Customer Value Analysis](src/test/resources/optional/o18_customer_product_analysis.sql)

Complex customer scoring system using multiple CTEs:

- Transaction patterns analysis
- Loan behavior evaluation
- Weighted scoring system:
    * 40% average balance
    * 30% total deposits
    * 30% loan amount
- Comprehensive customer metrics
- Advanced NULL handling
- Performance optimized calculations

#### [Branch Performance Metrics](src/test/resources/optional/o19_branch_performance_metrics.sql)

Advanced branch evaluation using multiple CTEs:

- Employee performance metrics
- Transaction efficiency analysis
- Complex performance scoring:
    * 30% total deposits
    * 30% total loan amount
    * 20% transaction count
    * 20% processed loans
- Efficiency ratios calculation
- Edge case handling
- Advanced aggregation techniques

These advanced tasks demonstrate:

- Multiple CTE usage
- Complex calculations
- Weighted scoring systems
- Performance optimization
- Comprehensive error handling
- Business metric calculations
- Advanced data analysis

## Database Schema

The workshop uses a banking system database with the following tables:

- customers
- accounts
- transactions
- loans
- employees
- branches
