package org.codeus.database.fundamentals.transaction_management.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeus.database.fundamentals.transaction_management.error.AccountNotFoundException;
import org.codeus.database.fundamentals.transaction_management.error.CustomerNotFoundException;
import org.codeus.database.fundamentals.transaction_management.error.DaoOperationException;
import org.codeus.database.fundamentals.transaction_management.error.InsufficientFundsException;
import org.codeus.database.fundamentals.transaction_management.error.InvalidPaymentException;
import org.codeus.database.fundamentals.transaction_management.error.LoanNotFoundException;
import org.codeus.database.fundamentals.transaction_management.model.Account;
import org.codeus.database.fundamentals.transaction_management.model.AccountType;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

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
        int customerId = account.getCustomerId();
        AccountType accountType = account.getAccountType();
        BigDecimal initialDeposit = account.getBalance();

        try (PreparedStatement selectCustomer = connection.prepareStatement("SELECT COUNT(*) FROM customers WHERE customer_id = ?;");
             PreparedStatement insertAccount = connection.prepareStatement(
                     "INSERT INTO accounts (customer_id, account_type, balance) VALUES (?, ?, ?);", RETURN_GENERATED_KEYS)) {

            connection.setAutoCommit(false);

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
                log.error("Failed to create account. Rolling back the transaction", e);
                connection.rollback();
            } catch (SQLException ex) {
                log.error("Failed to rollback transaction", ex);
                throw new DaoOperationException(ex);
            }
        }
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
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }

        try {
            connection.setAutoCommit(false);

            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            try (PreparedStatement checkAccount = connection.prepareStatement(
                    "SELECT balance FROM accounts WHERE account_id = ?;")) {

                checkAccount.setInt(1, accountId);
                ResultSet rs = checkAccount.executeQuery();

                if (!rs.next()) {
                    throw new AccountNotFoundException("Account with ID %d not found".formatted(accountId));
                }

                BigDecimal currentBalance = rs.getBigDecimal("balance");
                if (currentBalance.compareTo(amount) < 0) {
                    throw new InsufficientFundsException(
                            "Insufficient funds: balance " + currentBalance + ", withdrawal " + amount);
                }

                try (
                        PreparedStatement updateBalance = connection.prepareStatement(
                                "UPDATE accounts SET balance = balance - ? WHERE account_id = ?");
                        PreparedStatement insertTransaction = connection.prepareStatement(
                                "INSERT INTO transactions (account_id, transaction_type, amount, description) " +
                                "VALUES (?, 'withdrawal', ?, 'Withdrawal from account')")
                ) {
                    updateBalance.setBigDecimal(1, amount);
                    updateBalance.setInt(2, accountId);
                    updateBalance.executeUpdate();

                    insertTransaction.setInt(1, accountId);
                    insertTransaction.setBigDecimal(2, amount);
                    insertTransaction.executeUpdate();

                    connection.commit();
                }
            }
        } catch (SQLException e) {
            try {
                log.error("Failed to withdraw money", e);
                connection.rollback();
            } catch (SQLException ex) {
                throw new DaoOperationException("Failed to rollback transaction", ex);
            }
        }
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
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }

        if (fromAccountId == toAccountId) {
            throw new IllegalArgumentException("Source and destination accounts must be different");
        }

        try {
            connection.setAutoCommit(false);

            connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);

            BigDecimal fromBalance;
            try (PreparedStatement checkFromAccount = connection.prepareStatement(
                    "SELECT balance FROM accounts WHERE account_id = ?;")) {

                checkFromAccount.setInt(1, fromAccountId);
                ResultSet rs = checkFromAccount.executeQuery();

                if (!rs.next()) {
                    throw new AccountNotFoundException("Source account " + fromAccountId + " not found");
                }

                fromBalance = rs.getBigDecimal("balance");
                if (fromBalance.compareTo(amount) < 0) {
                    throw new InsufficientFundsException(
                            "Insufficient funds: balance " + fromBalance + ", transfer " + amount);
                }
            }

            try (PreparedStatement checkToAccount = connection.prepareStatement(
                    "SELECT account_id FROM accounts WHERE account_id = ?;")) {

                checkToAccount.setInt(1, toAccountId);
                ResultSet rs = checkToAccount.executeQuery();

                if (!rs.next()) {
                    throw new AccountNotFoundException("Destination account " + toAccountId + " not found");
                }
            }

            try (PreparedStatement debitAccount = connection.prepareStatement(
                    "UPDATE accounts SET balance = balance - ? WHERE account_id = ?")) {
                debitAccount.setBigDecimal(1, amount);
                debitAccount.setInt(2, fromAccountId);
                debitAccount.executeUpdate();
            }

            try (PreparedStatement creditAccount = connection.prepareStatement(
                    "UPDATE accounts SET balance = balance + ? WHERE account_id = ?")) {
                creditAccount.setBigDecimal(1, amount);
                creditAccount.setInt(2, toAccountId);
                creditAccount.executeUpdate();
            }

            try (PreparedStatement insertTransaction = connection.prepareStatement(
                    "INSERT INTO transactions (account_id, transaction_type, amount, description) " +
                    "VALUES (?, 'transfer', ?, ?)")) {

                insertTransaction.setInt(1, fromAccountId);
                insertTransaction.setBigDecimal(2, amount);
                insertTransaction.setString(3, "Transfer to account " + toAccountId);
                insertTransaction.executeUpdate();

                insertTransaction.setInt(1, toAccountId);
                insertTransaction.setBigDecimal(2, amount);
                insertTransaction.setString(3, "Transfer from account " + fromAccountId);
                insertTransaction.executeUpdate();

                connection.commit();
            }
        } catch (SQLException e) {
            try {
                log.warn("Failed to transfer money. Transaction is being rolled back", e);
                connection.rollback();
            } catch (SQLException ex) {
                throw new DaoOperationException("Failed to rollback transaction", ex);
            }
        }
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
        if (paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }

        Savepoint loanUpdatedSavepoint = null;

        try {
            connection.setAutoCommit(false);

            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

            BigDecimal loanAmount;
            try (PreparedStatement checkLoan = connection.prepareStatement(
                    "SELECT customer_id, loan_amount FROM loans WHERE loan_id = ?;")) {

                checkLoan.setInt(1, loanId);
                ResultSet rs = checkLoan.executeQuery();

                if (!rs.next()) {
                    throw new LoanNotFoundException("Loan with ID " + loanId + " not found");
                }

                loanAmount = rs.getBigDecimal("loan_amount");

                if (paymentAmount.compareTo(loanAmount) > 0) {
                    throw new InvalidPaymentException("Payment amount exceeds loan amount");
                }
            }

            try (PreparedStatement updateLoan = connection.prepareStatement(
                    "UPDATE loans SET loan_amount = loan_amount - ? WHERE loan_id = ?")) {
                updateLoan.setBigDecimal(1, paymentAmount);
                updateLoan.setInt(2, loanId);
                updateLoan.executeUpdate();
            }

            loanUpdatedSavepoint = connection.setSavepoint("UPDATE_LOAN");

            try (PreparedStatement insertPayment = connection.prepareStatement(
                    "INSERT INTO loan_payments (loan_id, payment_amount, description) " +
                    "VALUES (?, ?, ?)")) {

                insertPayment.setInt(1, loanId);
                insertPayment.setBigDecimal(2, paymentAmount);
                insertPayment.setString(3, description);

                insertPayment.executeUpdate();
                connection.commit();
            }
        } catch (SQLException e) {
            try {
                if (loanUpdatedSavepoint != null) {
                    connection.rollback(loanUpdatedSavepoint);

                    try (PreparedStatement insertPayment = connection.prepareStatement(
                            "INSERT INTO loan_payments (loan_id, payment_amount, description) " +
                            "VALUES (?, ?, ?)")) {

                        insertPayment.setInt(1, loanId);
                        insertPayment.setBigDecimal(2, paymentAmount);
                        insertPayment.setString(3, description);
                        insertPayment.executeUpdate();

                        connection.commit();
                    }
                } else {
                    // If we don't have any savepoints, roll back everything
                    log.error("Failed to process loan payment. No savepoint is available. Transaction is being rolled back", e);
                    connection.rollback();
                }
            } catch (SQLException ex) {
                try {
                    log.warn("Failed to process loan payment. Transaction is being rolled back", ex);
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    throw new DaoOperationException("Failed to process loan payment", rollbackEx);
                }
            }
        }
    }
}
