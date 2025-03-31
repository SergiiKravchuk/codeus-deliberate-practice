package org.codeus.database.fundamentals.transaction_management.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeus.database.fundamentals.transaction_management.error.AccountNotFoundException;
import org.codeus.database.fundamentals.transaction_management.error.DaoOperationException;
import org.codeus.database.fundamentals.transaction_management.error.ExerciseNotCompletedException;
import org.codeus.database.fundamentals.transaction_management.error.InsufficientFundsException;
import org.codeus.database.fundamentals.transaction_management.error.InvalidPaymentException;
import org.codeus.database.fundamentals.transaction_management.error.LoanNotFoundException;
import org.codeus.database.fundamentals.transaction_management.model.Account;

import java.math.BigDecimal;
import java.sql.Connection;

@Slf4j
@RequiredArgsConstructor
public class BankingDaoImpl implements BankingDao {

    private final Connection connection;

    /**
     * Creates a new account for a customer.
     *
     * @param account the account to be created
     */
    @Override
    public void createAccount(Account account) {
        throw new ExerciseNotCompletedException();
    }

    /**
     * Safely withdraws money from an account using READ COMMITTED isolation.
     *
     * @param accountId The account to withdraw from
     * @param amount    The amount to withdraw
     * @throws AccountNotFoundException   If the account doesn't exist
     * @throws InsufficientFundsException If the account has insufficient funds
     * @throws DaoOperationException      If the database operation fails
     */
    @Override
    public void withdrawMoney(int accountId, BigDecimal amount) {
        throw new ExerciseNotCompletedException();
    }

    /**
     * Transfers money between two accounts using REPEATABLE READ isolation.
     *
     * @param fromAccountId The source account
     * @param toAccountId   The destination account
     * @param amount        The amount to transfer
     * @throws AccountNotFoundException   If either account doesn't exist
     * @throws InsufficientFundsException If the source account has insufficient funds
     * @throws DaoOperationException      If the database operation fails
     */
    public void transferMoney(int fromAccountId, int toAccountId, BigDecimal amount) {
        throw new ExerciseNotCompletedException();
    }

    /**
     * Processes a loan payment with a savepoint to handle potential failures.
     *
     * @param loanId        The loan to make a payment on
     * @param paymentAmount The amount to pay
     * @param description   the description of the payment
     * @throws LoanNotFoundException   If the loan doesn't exist
     * @throws InvalidPaymentException If the payment amount is invalid
     * @throws DaoOperationException   If the database operation fails
     */
    public void processLoanPayment(int loanId, BigDecimal paymentAmount, String description) {
        throw new ExerciseNotCompletedException();
    }
}
