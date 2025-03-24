package org.codeus.database.fundamentals.transaction_management.dao;

import org.codeus.database.fundamentals.transaction_management.error.AccountNotFoundException;
import org.codeus.database.fundamentals.transaction_management.error.CustomerNotFoundException;
import org.codeus.database.fundamentals.transaction_management.error.DaoOperationException;
import org.codeus.database.fundamentals.transaction_management.error.InsufficientFundsException;
import org.codeus.database.fundamentals.transaction_management.error.InvalidPaymentException;
import org.codeus.database.fundamentals.transaction_management.error.LoanNotFoundException;
import org.codeus.database.fundamentals.transaction_management.model.Account;

import java.math.BigDecimal;

public interface BankingDao {

    /**
     * Creates a new account for a customer.
     * <p>
     * This method performs the following steps:
     * <ul>
     * <li>Starts a transaction by setting auto-commit to false.</li>
     * <li>Checks if the customer exists in the <code>customers</code> table. If the customer does not exist, throws {@link CustomerNotFoundException}.</li>
     * <li>Inserts a new record into the <code>accounts</code> table with the provided account details.</li>
     * <li>Retrieves the generated account ID and sets it to the provided {@code account} object.</li>
     * <li>Commits the transaction.</li>
     * <li>If any step fails, rolls back the transaction. If the rollback fails, throws {@link DaoOperationException}.</li>
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
     * <li>Validates if the input <code>amount</code> is less than or equal to 0. If true, throws {@link IllegalArgumentException}.</li>
     * <li>Starts a transaction by setting auto-commit to false.</li>
     * <li>Explicitly sets transaction isolation level to READ COMMITTED to prevent dirty reads.</li>
     * <li>Checks if the account exists in the <code>accounts</code> table and has sufficient funds.</li>
     * <ul>
     *     <li>Throws {@link AccountNotFoundException} in case the account does not exist.</li>
     *     <li>Throws {@link InsufficientFundsException} in case the account does not have enough money on the balance.</li>
     * </ul>
     * <li>Updates the account balance in the <code>accounts</code> table by deducting the withdrawal amount.</li>
     * <li>Inserts a transaction record into the <code>transactions</code> table.</li>
     * <li>Commits the transaction.</li>
     * <li>If any step fails, rolls back the transaction. If the rollback fails, throws {@link DaoOperationException}.</li>
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
     * <li>Validates if the input <code>amount</code> is less than or equal to 0. If true, throws {@link IllegalArgumentException}.</li>
     * <li>Validate if <code>fromAccountId</code> equals to <code>toAccountId</code>. If true, throws {@link IllegalArgumentException}</li>
     * <li>Starts a transaction by setting auto-commit to false.</li>
     * <li>Sets transaction isolation level to REPEATABLE READ to prevent non-repeatable reads.</li>
     * <li>Checks if the <code>fromAccountId</code> exists in the <code>accounts</code> table and has sufficient funds.</li>
     * <ul>
     *     <li>Throws {@link AccountNotFoundException} in case the account does not exist.</li>
     *     <li>Throws {@link InsufficientFundsException} in case the account does not have enough money on the balance.</li>
     * </ul>
     * <li>Checks if the <code>toAccountId</code> exists in the <code>accounts</code> table. If not than throws {@link AccountNotFoundException}</li>
     * <li>Updates the source account balance in the <code>accounts</code> table by deducting the transfer amount.</li>
     * <li>Updates the destination account balance in the <code>accounts</code> table by adding the transfer amount.</li>
     * <li>Inserts transaction records for both accounts into the <code>transaction</code> table. Transaction type
     *  should be <code>'transfer'</code>, amount should be equal to the input <code>amount</code>,
     *  description should be <code>'Transfer to/from' + accountId</code></li>
     * <li>Commits the transaction.</li>
     * <li>If any step fails, rolls back the transaction. If the rollback fails, throws {@link DaoOperationException}.</li>
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
     * Processes a loan payment with a savepoint to handle potential failures.
     * <p>
     * This method performs the following steps:
     * <ul>
     * <li>Validates if the input <code>amount</code> is less than or equal to 0. If true, throws {@link IllegalArgumentException}.</li>
     * <li>Starts a transaction by setting auto-commit to false.</li>
     * <li>Sets transaction isolation level to SERIALIZABLE to prevent phantom reads.</li>
     * <li>Verifies the loan exists in the <code>loans</code> table (throws {@link LoanNotFoundException} if it doesn't)
     * and gets its current balance.</li>
     * <li>Throws {@link InvalidPaymentException} if <code>paymentAmount</code> is larger than the loan amount.</li>
     * <li>Updates the loan balance in the <code>loans</code> table by deducting the payment amount.</li>
     * <li>Creates Savepoint after updating the loan balance.</li>
     * <li>Inserts a payment record into the <code>loan_payments</code> table.</li>
     * <li>Commits the transaction.</li>
     * <li>If {@link java.sql.SQLException} was caught:
     * <ul>
     *     <li>If the savepoint is present, rolls back to this savepoint and retries inserting into the
     *     <code>loan_payments</code> table. If the retry succeeds, commits the transaction and ends the method execution.</li>
     *     <li>If the savepoint is not present, rolls back the entire transaction.</li>
     * </ul>
     * <li>If any error occurred while handling the exception, tries rolling back the entire transaction. If the rollback
     * fails, throws {@link DaoOperationException}.</li>
     * </ul>
     *
     * @param loanId        the ID of the loan to make a payment on
     * @param paymentAmount the amount to pay
     * @param description   the description of the payment
     * @throws LoanNotFoundException   if the loan with the specified ID does not exist
     * @throws InvalidPaymentException if the payment amount is invalid (e.g., exceeds loan balance)
     * @throws DaoOperationException   if there is an error during the transaction or rollback
     */
    void processLoanPayment(int loanId, BigDecimal paymentAmount, String description);
}
