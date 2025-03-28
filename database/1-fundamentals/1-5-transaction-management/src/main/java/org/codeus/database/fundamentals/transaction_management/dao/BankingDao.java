package org.codeus.database.fundamentals.transaction_management.dao;

import org.codeus.database.fundamentals.transaction_management.error.AccountNotFoundException;
import org.codeus.database.fundamentals.transaction_management.error.CustomerNotFoundException;
import org.codeus.database.fundamentals.transaction_management.error.DaoOperationException;
import org.codeus.database.fundamentals.transaction_management.error.InsufficientFundsException;
import org.codeus.database.fundamentals.transaction_management.error.InvalidPaymentException;
import org.codeus.database.fundamentals.transaction_management.error.LoanApprovalException;
import org.codeus.database.fundamentals.transaction_management.error.LoanNotFoundException;
import org.codeus.database.fundamentals.transaction_management.model.Account;

import java.math.BigDecimal;

public interface BankingDao {

    /**
     * Creates a new account for a customer with transaction management.
     * <p>
     * This method performs the following steps:
     * <ul>
     * <li>Starts a transaction by setting auto-commit to false.</li>
     * <li>Checks if the customer exists in the database. If the customer does not exist, throws {@code CustomerNotFoundException}.</li>
     * <li>Inserts a new record into the accounts table with the provided account details.</li>
     * <li>Retrieves the generated account ID and sets it to the provided {@code Account} object.</li>
     * <li>Commits the transaction.</li>
     * <li>If any step fails, rolls back the transaction. If the rollback fails, throws {@code DaoOperationException}.</li>
     * </ul>
     *
     * @param account the account to be created
     * @throws CustomerNotFoundException if the customer with the specified ID does not exist
     * @throws DaoOperationException     if there is an error during the transaction or rollback
     */
    void createAccount(Account account);

    /**
     * Withdraws money from an account using READ COMMITTED isolation level.
     * <p>
     * This method performs the following steps:
     * <ul>
     * <li>Sets transaction isolation level to READ COMMITTED to prevent dirty reads.</li>
     * <li>Checks if the account exists and has sufficient funds (FOR UPDATE to lock the row).</li>
     * <li>Updates the account balance by deducting the withdrawal amount.</li>
     * <li>Inserts a transaction record into the transactions table.</li>
     * <li>Commits the transaction.</li>
     * <li>If any step fails, rolls back the entire transaction.</li>
     * </ul>
     *
     * @param accountId the ID of the account to withdraw from
     * @param amount    the amount to withdraw
     * @throws AccountNotFoundException   if the account with the specified ID does not exist
     * @throws InsufficientFundsException if the account balance is less than the withdrawal amount
     * @throws DaoOperationException      if there is an error during the transaction or rollback
     */
    void withdrawMoney(int accountId, BigDecimal amount);

    /**
     * Transfers money between two accounts using REPEATABLE READ isolation level.
     * <p>
     * This method performs the following steps:
     * <ul>
     * <li>Sets transaction isolation level to REPEATABLE READ to prevent non-repeatable reads.</li>
     * <li>Locks and verifies the source account (FOR UPDATE).</li>
     * <li>Checks if the source account has sufficient funds.</li>
     * <li>Locks and verifies the destination account (FOR UPDATE).</li>
     * <li>Updates the source account balance by deducting the transfer amount.</li>
     * <li>Updates the destination account balance by adding the transfer amount.</li>
     * <li>Inserts transaction records for both accounts.</li>
     * <li>Commits the transaction.</li>
     * <li>If any step fails, rolls back the entire transaction.</li>
     * </ul>
     *
     * @param fromAccountId the ID of the source account
     * @param toAccountId   the ID of the destination account
     * @param amount        the amount to transfer
     * @throws AccountNotFoundException   if either account does not exist
     * @throws InsufficientFundsException if the source account has insufficient funds
     * @throws DaoOperationException      if there is an error during the transaction or rollback
     */
    void transferMoney(int fromAccountId, int toAccountId, BigDecimal amount);

    /**
     * Processes a loan payment with savepoints to handle potential failures.
     * <p>
     * This method performs the following steps:
     * <ul>
     * <li>Sets transaction isolation level to SERIALIZABLE to prevent phantom reads.</li>
     * <li>Verifies the loan exists and gets its current balance (FOR UPDATE).</li>
     * //     * todo: maybe not specify FOR UPDATE
     * <li>Creates Savepoint 1 after loan verification.</li>
     * <li>Updates the loan balance by deducting the payment amount.</li>
     * <li>Creates Savepoint 2 after updating the loan balance.</li>
     * <li>Inserts a payment record into the loan_payments table.</li>
     * <li>If the payment record insertion fails due to a NULL constraint, rolls back to Savepoint 2 and retries.</li>
     * <li>If other errors occur, rolls back to Savepoint 1 or rolls back the entire transaction.</li>
     * <li>Commits the transaction if all steps succeed.</li>
     * </ul>
     *
     * @param loanId        the ID of the loan to make a payment on
     * @param paymentAmount the amount to pay
     * @throws LoanNotFoundException   if the loan with the specified ID does not exist
     * @throws InvalidPaymentException if the payment amount is invalid (e.g., exceeds loan balance)
     * @throws DaoOperationException   if there is an error during the transaction or rollback
     */
    // todo: add description doc
    void processLoanPayment(int loanId, BigDecimal paymentAmount, String description);

    /**
     * Processes a loan request with multiple savepoints to handle failures at different stages.
     * <p>
     * This method performs the following steps:
     * <ul>
     * <li>Verifies the customer exists.</li>
     * <li>Creates Savepoint 1 after customer verification.</li>
     * <li>Checks the customer's credit score (simulated).</li>
     * <li>Creates a loan entry in the loans table with calculated interest rate.</li>
     * <li>Creates Savepoint 2 after creating the loan entry.</li>
     * <li>Attempts to deduct a processing fee from one of the customer's accounts.</li>
     * <li>If the customer has no accounts or insufficient funds for the fee, rolls back to Savepoint 2.</li>
     * <li>If loan creation fails, rolls back to Savepoint 1.</li>
     * <li>If customer verification fails, rolls back the entire transaction.</li>
     * <li>Commits the transaction if all steps succeed.</li>
     * </ul>
     *
     * @param customerId the ID of the customer requesting the loan
     * @param loanAmount the requested loan amount
     * @throws CustomerNotFoundException if the customer with the specified ID does not exist
     * @throws LoanApprovalException if the loan cannot be approved (e.g., credit score too low)
     * @throws DaoOperationException if there is an error during the transaction or rollback
     */
//    void processLoanRequest(int customerId, BigDecimal loanAmount);
}
