package org.codeus.database.fundamentals.transaction_management.dao;

import org.codeus.database.fundamentals.transaction_management.error.CustomerNotFoundException;
import org.codeus.database.fundamentals.transaction_management.error.DaoOperationException;
import org.codeus.database.fundamentals.transaction_management.model.Account;
import org.codeus.database.fundamentals.transaction_management.model.AccountType;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

//@RequiredArgsConstructor
public class BankingDaoImpl {

    //    private final DataSource dataSource;
    private final Connection connection;

    public BankingDaoImpl(Connection connection) {
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new DaoOperationException(e);
        }

        this.connection = connection;
    }

    /*
        Create a New Account with Transaction Management

    Requirements:
    Start a transaction.
    Check if the customer exists.
    Insert a new row into the accounts table.
    If initialDeposit > 0, insert a corresponding record into the transactions table.
    If any step fails (e.g., invalid customerId), rollback the entire transaction.

    Concepts Covered:
    JDBC transaction management.
    Foreign key validation.
    Atomicity: Ensuring no partial updates.
         */

    public void createAccount(Account account) {
        int customerId = account.getCustomerId();
        AccountType accountType = account.getAccountType();
        BigDecimal initialDeposit = account.getBalance();

        try (PreparedStatement selectCustomer = connection.prepareStatement("SELECT COUNT(*) FROM customers WHERE customer_id = ?;");
             PreparedStatement insertAccount = connection.prepareStatement(
                     "INSERT INTO accounts (customer_id, account_type, balance) VALUES (?, ?, ?);", RETURN_GENERATED_KEYS)) {

            selectCustomer.setInt(1, customerId);
            if (selectCustomer.execute() && selectCustomer.getResultSet().next()) {
                ResultSet resultSet = selectCustomer.getResultSet();
                int customerCount = resultSet.getInt(1);
                if (customerCount == 0)
                    throw new CustomerNotFoundException("Customer with id %d not found".formatted(customerId));

                insertAccount.setInt(1, customerId);
                insertAccount.setString(2, accountType.getType());
                insertAccount.setBigDecimal(3, initialDeposit);
                insertAccount.executeUpdate();

                ResultSet generatedKeys = insertAccount.getGeneratedKeys();
                if (generatedKeys.next()) {
                    account.setAccountId(generatedKeys.getInt(1));
                } else {
                    throw new DaoOperationException("Failed to obtain account id");
                }
            }

            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new DaoOperationException(ex);
            }
        }
    }

    /*
    idea for the method: create customer along with account
    savepoint after creating customer
    in case of failure during account creation, rollback to savepoint
    if customer is failed to be created, rollback the entire transaction
     */
    // todo: create method. check if there is another similar method already


    /*
     Safe Balance Check Before Withdrawal (Avoid Dirty Reads)
Scenario:
A customer wants to withdraw money from their account. The system must:

Read the current balance from the account.
If the balance is sufficient, deduct the amount.
Insert a withdrawal transaction record.
Commit the transaction.
To prevent dirty reads, the implementation must use at least READ COMMITTED isolation.

Implementation Steps:
Use READ COMMITTED or higher to avoid reading an uncommitted balance change from another transaction.
Ensure the balance is consistent before making a deduction.
Test Case:
In one transaction, start a withdrawal and don’t commit it.
In another transaction, try to read the balance.
The balance should not show uncommitted changes.
     */
    public void withdrawMoney(int accountId, BigDecimal amount) {
    }

    /*
    Transfer Money Between Accounts with Savepoints

A customer transfers money from their account at Bank A to an account in Bank B. The steps are:

1. Deduct the transfer amount from the customer’s account in Bank A.
2. Send a fund transfer request to Bank B.
3. Credit the receiving account in Bank B.
4. Log the transaction in both banks.
If step 4 (transaction logging) fails, rollback to step 3.
If step 3 (crediting Bank B) fails, rollback everything (Bank A should not deduct if Bank B does not credit).

Implementation Steps:
- sp1: After deducting from Bank A.
- sp2: After sending the transfer request to Bank B.
- sp3: After crediting Bank B’s account.
Rollback sp3 if logging fails.
Rollback everything if Bank B fails to credit the account.
     */
    /*
    (Avoid Non-Repeatable Reads)
Scenario:
A customer transfers money between two accounts. The system must:

Read the fromAccount balance.
Deduct the amount.
Read the toAccount balance.
Add the amount to toAccount.
Insert a transaction record.
To prevent non-repeatable reads, the implementation must use at least REPEATABLE READ.

Implementation Steps:
Use REPEATABLE READ to ensure balances do not change during the transaction.
Ensure that the same balance values are used across multiple reads.
Test Case:
In one transaction, read the fromAccount balance twice.
In another transaction, update the balance after the first read but before the second.
The second read should return the same value as the first.
     */
    public void transferMoney(int fromAccountId, int toAccountId, BigDecimal amount) {
    }

    /*
    Simulate a Failed Loan Payment Transaction
Requirements:

Start a transaction.
Savepoint 1: Verify loan exists and balance is sufficient.
Deduct paymentAmount from the loan balance.
Savepoint 2: Insert a payment record (assume loan_payments table exists).
Intentionally cause a failure (e.g., inserting a NULL value).
Demonstrate full rollback on failure.

todo: my comment: wouldn't need savepoints here. Just rollback the entire transaction if any step fails

Concepts Covered:
Ensuring all steps succeed before committing.
Handling unexpected errors gracefully.
     */
    /*
    (Avoid Phantom Reads)
Scenario:
A customer makes a loan payment. The system must:

Read all pending loan payments for the customer.
Apply the payment amount to the oldest loan first.
Update the remaining balance of the loan.
Insert a payment transaction record.
To prevent phantom reads, the implementation must use SERIALIZABLE.

Implementation Steps:
Use SERIALIZABLE to ensure that the list of pending payments does not change while processing payments.
Prevent new loan payments from appearing mid-transaction.
Test Case:
In one transaction, read all pending payments.
In another transaction, insert a new pending loan payment before the first transaction commits.
The first transaction should not see the new payment.
     */
    public void processLoanPayment(int loanId, BigDecimal paymentAmount) {
    }

    /*
    Exercise 2: Multi-Step Loan Approval with Single Savepoint
Scenario:
A new loan application is processed. The process consists of:

1. Checking the customer’s credit score.
2. Creating a loan entry if eligible.
3. Deducting an initial processing fee from their account.
If step 3 fails, rollback to step 2 (so loan exists but no fee is deducted).
If step 2 fails, rollback everything (loan should not exist if it wasn’t successfully created).

Implementation Steps
- sp1: After credit score verification.
- sp2: After inserting the loan entry.
If fee deduction fails, rollback to sp2.
If loan creation fails, rollback everything.
     */
    public void processLoanRequest(int customerId, BigDecimal loanAmount) {
    }

    /*
    Exercise 4: Employee Payroll Processing (Multiple Savepoints)
Scenario:
The bank processes employee payroll in three stages:

Deduct the salary from the bank’s payroll account.
Deposit salary into each employee’s account.
Log payroll transactions for compliance.
If step 3 (logging transactions) fails, rollback to step 2.
If step 2 (depositing salary) fails for any employee, rollback only their transaction, not the entire batch.

Implementation Steps:
sp1: After deducting from the payroll account.
sp2: After depositing salaries for each employee.
Rollback sp2 for individual employees if their transaction fails.
Rollback sp1 if bulk salary deposit fails.
     */
    public void processEmployeePayroll() {
    }

    /*
    Concurrent Transactions – Isolation Level Testing

    Requirements:
Run two concurrent transactions with different isolation levels:
Thread 1: Reads an account balance.
Thread 2: Updates the balance.
Thread 1: Reads the balance again.
Compare behaviors across READ COMMITTED, REPEATABLE READ, and SERIALIZABLE.

Concepts Covered:
Preventing dirty reads, non-repeatable reads, and phantom reads
     */
    public void testIsolationLevel(Connection connection, String isolationLevel) {
    }

    /*
    Simulate a Deadlock in Loan Processing

    Requirements:
Thread 1:
Lock loan1 (SELECT ... FOR UPDATE).
Wait for 2 seconds.
Lock loan2.
Thread 2 (parallel):
Lock loan2.
Wait for 2 seconds.
Lock loan1.
Detect deadlock and rollback.

Concepts Covered:
Circular lock dependencies.
Deadlock resolution with retries
     */
    // todo: deadlocks are in the next day exercises. Should we include them here? remove if too complicated
    public void simulateDeadlock(int loan1, int loan2) {
    }

    /*
    Grant User-Specific Privileges

Requirements:

Dynamically assign permissions:
HR_MANAGER: Can modify employees.
BRANCH_MANAGER: Can modify branches.
READ_ONLY_USER: Can only read data.
Execute GRANT statements via JDBC.
Concepts Covered:

Role-based access control.
Using JDBC for database security.
     */
    public void createUserWithRole(String username, String role) {
    }

    /*
    Demonstrating Anomalies

    Requirements:

Non-Repeatable Read:
Session 1: Read an account balance.
Session 2: Update the balance.
Session 1: Read the balance again.
Phantom Read:
Session 1: Count loans for a customer.
Session 2: Insert a new loan.
Session 1: Count loans again.
Concepts Covered:

Effects of different isolation levels.
     */
    public void demonstrateAnomalies(Connection connection) {
    }

}
// todo: add tests to handle deadlocks and race conditions
