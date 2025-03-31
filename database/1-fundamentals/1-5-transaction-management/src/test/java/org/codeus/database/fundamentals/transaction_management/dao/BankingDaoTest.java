package org.codeus.database.fundamentals.transaction_management.dao;

import lombok.SneakyThrows;
import org.codeus.database.common.EmbeddedPostgreSqlSetup;
import org.codeus.database.fundamentals.transaction_management.error.AccountNotFoundException;
import org.codeus.database.fundamentals.transaction_management.error.CustomerNotFoundException;
import org.codeus.database.fundamentals.transaction_management.error.DaoOperationException;
import org.codeus.database.fundamentals.transaction_management.error.InsufficientFundsException;
import org.codeus.database.fundamentals.transaction_management.error.InvalidPaymentException;
import org.codeus.database.fundamentals.transaction_management.error.LoanNotFoundException;
import org.codeus.database.fundamentals.transaction_management.model.Account;
import org.codeus.database.fundamentals.transaction_management.model.AccountType;
import org.codeus.database.fundamentals.transaction_management.model.Loan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.description;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.description;

@TestClassOrder(ClassOrderer.OrderAnnotation.class)
class BankingDaoTest extends EmbeddedPostgreSqlSetup {

    private BankingDao instance;
    private Connection spyConnection;

    @SneakyThrows
    @BeforeEach
    void setUp() {
        spyConnection = spy(EmbeddedPostgreSqlSetup.connection);
        instance = new BankingDaoImpl(spyConnection);
        connection.setAutoCommit(true);
    }

    @Nested
    @Order(1)
    @DisplayName("createAccount Tests")
    class CreateAccountTests {

        @Test
        @DisplayName("should create account")
        void createAccount() {
            List<Account> before = selectAccounts();

            Account account = Account.builder()
                    .customerId(1)
                    .accountType(AccountType.CHECKING)
                    .balance(BigDecimal.valueOf(100, 0))
                    .build();

            instance.createAccount(account);

            List<Account> after = selectNewAccounts(before);

            assertEquals(1, after.size(), "Should have created 1 account");
            Account createdAccount = after.get(0);
            assertEquals(account.getCustomerId(), createdAccount.getCustomerId(), "CustomerId should match");
            assertEquals(account.getAccountType(), createdAccount.getAccountType(), "AccountType should match");
            assertEquals(0, account.getBalance().compareTo(createdAccount.getBalance()), "Balance should match");
            assertNotNull(createdAccount.getCreatedAt(), "CreatedAt should be set");
        }

        @Test
        @DisplayName("should throw exception when account exists")
        void createAccount_whenCustomerDoesNotExist_throwException() {
            List<Account> before = selectAccounts();

            Account account = Account.builder()
                    .customerId(-10)
                    .accountType(AccountType.SAVINGS)
                    .balance(BigDecimal.valueOf(1000, 0))
                    .build();

            assertThrows(CustomerNotFoundException.class, () -> instance.createAccount(account));

            List<Account> after = selectAccounts();
            assertEquals(before.size(), after.size(), "Should not have created any new accounts");
        }

        @Test
        @DisplayName("should generate and set id when creating account")
        void createAccount_generateId() {
            List<Account> before = selectAccounts();

            Account account = Account.builder()
                    .customerId(1)
                    .accountType(AccountType.CHECKING)
                    .balance(BigDecimal.valueOf(100, 0))
                    .build();

            instance.createAccount(account);

            List<Account> after = selectNewAccounts(before);

            assertEquals(1, after.size(), "Should have created 1 account");
            Account createdAccount = after.get(0);
            assertNotEquals(0, createdAccount.getAccountId(), "AccountId should be set");
            assertEquals(createdAccount.getAccountId(), account.getAccountId(), "AccountId should match");
        }

        @Test
        @DisplayName("should throw exception when customer does not exist")
        void createAccount_customerNotPresent_throwCustomerNotFoundException() {
            Account account = Account.builder()
                    .customerId(-10)
                    .accountType(AccountType.SAVINGS)
                    .balance(BigDecimal.valueOf(1000, 0))
                    .build();

            assertThrows(CustomerNotFoundException.class, () -> instance.createAccount(account));
        }

        @Test
        @DisplayName("should wrap sql exceptions into custom exceptions")
        void createAccount_wrapSqlExceptions() throws SQLException {
            Account account = Account.builder()
                    .customerId(1)
                    .accountType(AccountType.SAVINGS)
                    .balance(BigDecimal.valueOf(1000, 0))
                    .build();

            doThrow(SQLException.class).when(spyConnection).commit();
            doThrow(SQLException.class).when(spyConnection).rollback();

            assertThrows(DaoOperationException.class, () -> instance.createAccount(account));

            verify(spyConnection, description("Autocommit should be disabled")).setAutoCommit(false);
            verify(spyConnection, description("Transaction should be rolled back when exception is caught")).rollback();
        }
    }

    @Nested
    @Order(2)
    @DisplayName("withdrawMoney Tests")
    class WithdrawMoneyTests {

        @Test
        @DisplayName("should withdraw money from account")
        @SneakyThrows
        void withdrawMoney() {
            int accountId = 1;

            BigDecimal withdrawalAmount = BigDecimal.valueOf(250, 0);
            instance.withdrawMoney(accountId, withdrawalAmount);

            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT balance FROM accounts WHERE account_id = ?")) {
                statement.setInt(1, accountId);
                ResultSet rs = statement.executeQuery();
                assertTrue(rs.next(), "Account should exist");
                BigDecimal newBalance = rs.getBigDecimal("balance");
                assertEquals(0, BigDecimal.valueOf(750).compareTo(newBalance), "Balance should be reduced by withdrawal amount");
            }

            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT COUNT(*) FROM transactions WHERE account_id = ? AND transaction_type = 'withdrawal' AND amount = ?")) {
                statement.setInt(1, accountId);
                statement.setBigDecimal(2, withdrawalAmount);
                ResultSet rs = statement.executeQuery();
                assertTrue(rs.next(), "Transaction count should be available");
                assertEquals(1, rs.getInt(1), "Should have one withdrawal transaction");
            }

            verify(spyConnection, description("Autocommit should be disabled")).setAutoCommit(false);
            verify(spyConnection, description("READ_COMMITED isolation level should be set"))
                    .setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        }

        @Test
        @DisplayName("should throw exception when withdrawing from non-existent account")
        void withdrawMoney_nonExistentAccount() {
            BigDecimal amount = BigDecimal.valueOf(100);
            assertThrows(AccountNotFoundException.class, () -> instance.withdrawMoney(999, amount));
        }

        @Test
        @DisplayName("should throw exception when withdrawing with insufficient funds")
        @SneakyThrows
        void withdrawMoney_insufficientFunds() {
            Account account = getAccountWithInsufficientFunds();
            int accountId = account.getAccountId();

            BigDecimal withdrawalAmount = BigDecimal.valueOf(100, 0);
            assertThrows(InsufficientFundsException.class,
                    () -> instance.withdrawMoney(accountId, withdrawalAmount));

            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT balance FROM accounts WHERE account_id = ?")) {
                statement.setInt(1, accountId);
                ResultSet rs = statement.executeQuery();
                assertTrue(rs.next(), "Account should exist");
                BigDecimal balance = rs.getBigDecimal("balance");
                assertEquals(0, BigDecimal.valueOf(50, 0).compareTo(balance), "Balance should remain unchanged");
            }
        }

        private Account getAccountWithInsufficientFunds() {
            return Account.builder()
                    .accountId(6)
                    .customerId(5)
                    .accountType(AccountType.CHECKING)
                    .balance(BigDecimal.valueOf(50, 0))
                    .build();
        }
    }

    @Nested
    @Order(3)
    @DisplayName("transferMoney Tests")
    class TransferMoneyTests {

        @Test
        @DisplayName("should transfer money between accounts")
        @SneakyThrows
        void transferMoney() {
            Account sourceAccount = selectAccount(1);
            Account destAccount = selectAccount(2);

            BigDecimal transferAmount = BigDecimal.valueOf(200, 0);
            instance.transferMoney(sourceAccount.getAccountId(), destAccount.getAccountId(), transferAmount);

            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT balance FROM accounts WHERE account_id = ?")) {
                statement.setInt(1, sourceAccount.getAccountId());
                ResultSet rs = statement.executeQuery();
                assertTrue(rs.next(), "Source account should exist");
                BigDecimal sourceBalance = rs.getBigDecimal("balance");
                assertEquals(sourceAccount.getBalance().subtract(transferAmount), sourceBalance, "Source balance should be reduced");

                statement.setInt(1, destAccount.getAccountId());
                rs = statement.executeQuery();
                assertTrue(rs.next(), "Destination account should exist");
                BigDecimal destBalance = rs.getBigDecimal("balance");
                assertEquals(destAccount.getBalance().add(transferAmount), destBalance, "Destination balance should be increased");
            }

            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT COUNT(*) FROM transactions WHERE transaction_type = 'transfer' AND amount = ?")) {
                statement.setBigDecimal(1, transferAmount);
                ResultSet rs = statement.executeQuery();
                assertTrue(rs.next(), "Transaction count should be available");
                assertEquals(2, rs.getInt(1), "Should have two transfer transactions (source and destination)");
            }

            verify(spyConnection, description("Autocommit should be disabled")).setAutoCommit(false);
            verify(spyConnection, description("READ_COMMITED isolation level should be set")).setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
        }

        @Test
        @DisplayName("should rollback transaction when SQLException occurs")
        @SneakyThrows
        void transferMoney_whenSQLException_rollbackTransaction() {
            Account sourceAccount = selectAccount(1);
            Account destAccount = selectAccount(2);

            doThrow(SQLException.class).when(spyConnection).commit();

            BigDecimal transferAmount = BigDecimal.valueOf(200, 0);
            assertDoesNotThrow(() -> instance.transferMoney(sourceAccount.getAccountId(), destAccount.getAccountId(), transferAmount),
                    "Should rollback transaction and not throw any exception when SQLException occurs");

            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT balance FROM accounts WHERE account_id = ?")) {
                statement.setInt(1, sourceAccount.getAccountId());
                ResultSet rs = statement.executeQuery();
                assertTrue(rs.next(), "Source account should exist");
                BigDecimal sourceBalance = rs.getBigDecimal("balance");
                assertEquals(sourceAccount.getBalance(), sourceBalance, "Source balance should remain unchanged");

                statement.setInt(1, destAccount.getAccountId());
                rs = statement.executeQuery();
                assertTrue(rs.next(), "Destination account should exist");
                BigDecimal destBalance = rs.getBigDecimal("balance");
                assertEquals(destAccount.getBalance(), destBalance, "Destination balance should remain unchanged");
            }

            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT COUNT(*) FROM transactions WHERE transaction_type = 'transfer' AND amount = ?")) {
                statement.setBigDecimal(1, transferAmount);
                ResultSet rs = statement.executeQuery();
                assertTrue(rs.next(), "Transaction count should be available");
                assertEquals(0, rs.getInt(1), "Should have no transfer transactions");
            }

            verify(spyConnection, description("Autocommit should be disabled")).setAutoCommit(false);
            verify(spyConnection, description("READ_COMMITED isolation level should be set")).setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            verify(spyConnection, description("Transaction should be rolled back when exception is caught")).rollback();
        }

        @Test
        @DisplayName("should rollback transaction when SQLException occurs during rollback")
        @SneakyThrows
        void transferMoney_whenSQLExceptionDuringRollback_throwDaoOperationException() {
            doThrow(SQLException.class).when(spyConnection).commit();
            doThrow(SQLException.class).when(spyConnection).rollback();

            BigDecimal transferAmount = BigDecimal.valueOf(200, 0);
            assertThrows(DaoOperationException.class, () -> instance.transferMoney(1, 2, transferAmount),
                    "Should throw DaoOperationException when SQLException occurs during rollback");

            verify(spyConnection, description("Autocommit should be disabled")).setAutoCommit(false);
            verify(spyConnection, description("READ_COMMITED isolation level should be set")).setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            verify(spyConnection, description("Transaction should be rolled back when exception is caught")).rollback();
        }

        @Test
        @DisplayName("should throw exception when source account does not exist")
        void transferMoney_whenSourceAccountDoesNotExist_throwAccountNotFoundException() {
            BigDecimal transferAmount = BigDecimal.valueOf(200, 0);
            assertThrows(AccountNotFoundException.class, () -> instance.transferMoney(999, 2, transferAmount),
                    "Should throw exception when source account does not exist");
        }

        @Test
        @DisplayName("should throw exception when destination account does not exist")
        void transferMoney_whenDestinationAccountDoesNotExist_throwAccountNotFoundException() {
            BigDecimal transferAmount = BigDecimal.valueOf(200, 0);
            assertThrows(AccountNotFoundException.class, () -> instance.transferMoney(1, 999, transferAmount),
                    "Should throw exception when destination account does not exist");
        }

        @Test
        @DisplayName("should throw exception when source account has insufficient funds")
        void transferMoney_whenSourceAccountHasInsufficientFunds_throwInsufficientFundsException() {
            BigDecimal transferAmount = BigDecimal.valueOf(200, 0);
            assertThrows(InsufficientFundsException.class, () -> instance.transferMoney(6, 2, transferAmount),
                    "Should throw exception when source account has insufficient funds");
        }

        @ParameterizedTest
        @DisplayName("should throw exception when transfer amount is zero or below")
        @ValueSource(longs = {0L, -100L})
        void transferMoney_whenLowMoney_throwIllegalArgumentException(long amount) {
            BigDecimal transferAmount = BigDecimal.valueOf(amount);
            assertThrows(IllegalArgumentException.class, () -> instance.transferMoney(1, 2, transferAmount),
                    "Should throw exception when transfer amount is zero or below");
        }

        @Test
        @DisplayName("should throw exception when accounts are equal")
        void transferMoney_whenAccountsEqual_throwIllegalArgumentException() {
            BigDecimal amount = BigDecimal.valueOf(100, 0);

            assertThrows(IllegalArgumentException.class, () -> instance.transferMoney(1, 1, amount),
                    "Should throw exception when source and destination accounts are the same");
        }
    }

    @Nested
    @Order(4)
    @DisplayName("processLoanPayment Tests")
    class ProcessLoanPaymentTests {
        @Test
        @DisplayName("should process loan payment")
        @SneakyThrows
        void processLoanPayment() {
            int loanId = 1;
            Loan loan = selectLoan(loanId);
            BigDecimal initialLoanAmount = loan.getLoanAmount();

            BigDecimal paymentAmount = BigDecimal.valueOf(200, 0);
            instance.processLoanPayment(loanId, paymentAmount, "Loan payment");

            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT loan_amount FROM loans WHERE loan_id = ?")) {
                statement.setInt(1, loanId);
                ResultSet rs = statement.executeQuery();
                assertTrue(rs.next(), "Loan should exist");
                BigDecimal newLoanAmount = rs.getBigDecimal("loan_amount");
                assertEquals(0, initialLoanAmount.subtract(newLoanAmount).compareTo(paymentAmount), "Balance should be reduced by payment amount");
            }

            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT COUNT(*) FROM loan_payments WHERE loan_id = ? AND payment_amount = ?")) {
                statement.setInt(1, loanId);
                statement.setBigDecimal(2, paymentAmount);
                ResultSet rs = statement.executeQuery();
                assertTrue(rs.next(), "Payment record count should be available");
                assertEquals(1, rs.getInt(1), "Should have one payment record");
            }

            verify(spyConnection, description("Autocommit should be disabled")).setAutoCommit(false);
            verify(spyConnection, description("SERIALIZABLE isolation level should be set")).setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        }

        @Test
        @DisplayName("should throw exception when loan does not exist")
        void processLoanPayment_whenLoanDoesNotExist_throwLoanNotFoundException() {
            BigDecimal paymentAmount = BigDecimal.valueOf(100);
            assertThrows(LoanNotFoundException.class, () -> instance.processLoanPayment(999, paymentAmount, "Loan payment"),
                    "Should throw exception when loan does not exist");
        }

        @Test
        @DisplayName("should throw exception when payment amount exceeds loan balance")
        void processLoanPayment_whenPaymentAmountExceedsBalance_throwInvalidPaymentException() {
            int loanId = 1;
            BigDecimal paymentAmount = BigDecimal.valueOf(10000, 0);

            assertThrows(InvalidPaymentException.class, () -> instance.processLoanPayment(loanId, paymentAmount, "Loan payment"),
                    "Should throw exception when payment amount exceeds loan balance");
        }

        @ParameterizedTest
        @DisplayName("should throw exception when payment amount is below zero")
        @ValueSource(longs = {0L, -100L})
        void processLoanPayment_whenAmountBelowZero_throwIllegalArgumentException(long amount) {
            int loanId = 1;

            BigDecimal paymentAmount = BigDecimal.valueOf(amount);
            assertThrows(IllegalArgumentException.class, () -> instance.processLoanPayment(loanId, paymentAmount, "Loan payment"),
                    "Should throw exception when payment amount is below zero");
        }

        @Test
        @DisplayName("should rollback to savepoint and retry loan payments insert when SQLException occurs")
        @SneakyThrows
        void processLoanPayment_whenSQLException_shouldRollbackToSavepointAndRetryLoanPaymentsInsert() {
            int loanId = 1;
            Loan loan = selectLoan(loanId);
            BigDecimal initialLoanAmount = loan.getLoanAmount();

            doThrow(SQLException.class)
                    .doCallRealMethod().when(spyConnection).commit();

            BigDecimal paymentAmount = BigDecimal.valueOf(200, 0);
            instance.processLoanPayment(loanId, paymentAmount, "Loan payment");

            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT loan_amount FROM loans WHERE loan_id = ?")) {
                statement.setInt(1, loanId);
                ResultSet rs = statement.executeQuery();
                assertTrue(rs.next(), "Loan should exist");
                BigDecimal newLoanAmount = rs.getBigDecimal("loan_amount");
                assertEquals(0, initialLoanAmount.subtract(newLoanAmount).compareTo(paymentAmount), "Balance should be reduced by payment amount");
            }

            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT COUNT(*) FROM loan_payments WHERE loan_id = ? AND payment_amount = ?")) {
                statement.setInt(1, loanId);
                statement.setBigDecimal(2, paymentAmount);
                ResultSet rs = statement.executeQuery();
                assertTrue(rs.next(), "Payment record count should be available");
                assertEquals(1, rs.getInt(1), "Should have one payment record");
            }

            verify(spyConnection, description("Autocommit should be disabled")).setAutoCommit(false);
            verify(spyConnection, description("SERIALIZABLE isolation level should be set")).setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            verify(spyConnection, description("Should rollback to savepoint when it is present")).rollback(any(Savepoint.class));
            verify(spyConnection, description(
                    times(2), "Should retry loan payments insert when SQLException occurs"))
                    .commit();
        }

        @Test
        @DisplayName("should rollback entire transaction when SQLException occurs and savepoint is not present")
        @SneakyThrows
        void processLoanPayment_whenSavepointNotPresent_shouldRollbackEntireTransaction() {
            lenient().doThrow(SQLException.class).when(spyConnection).prepareStatement(anyString());
            lenient().doThrow(SQLException.class).when(spyConnection).prepareStatement(anyString(), anyInt());
            lenient().doThrow(SQLException.class).when(spyConnection).createStatement();

            int loanId = 1;
            Loan loan = selectLoan(loanId);
            BigDecimal paymentAmount = BigDecimal.valueOf(200, 0);

            assertDoesNotThrow(() -> instance.processLoanPayment(loanId, paymentAmount, "Loan payment"));

            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT loan_amount FROM loans WHERE loan_id = ?")) {
                statement.setInt(1, loanId);
                ResultSet rs = statement.executeQuery();
                assertTrue(rs.next(), "Loan should exist");
                BigDecimal loanAmount = rs.getBigDecimal("loan_amount");
                assertEquals(0, loan.getLoanAmount().compareTo(loanAmount), "Loan amount should remain unchanged");
            }

            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT COUNT(*) FROM loan_payments WHERE loan_id = ? and payment_amount = ?")) {
                statement.setInt(1, loanId);
                statement.setBigDecimal(2, paymentAmount);
                ResultSet rs = statement.executeQuery();
                assertTrue(rs.next(), "Payment record count should be available");
                assertEquals(0, rs.getInt(1), "Should have no payment records");
            }
        }

        @Test
        @DisplayName("should rollback entire transaction when SQLException occurs while retrying loan payments insert")
        @SneakyThrows
        void processLoanPayment_whenSQLExceptionDuringRetry_shouldRollbackEntireTransaction() {
            int loanId = 1;
            Loan loan = selectLoan(loanId);

            doThrow(SQLException.class).when(spyConnection).commit();

            BigDecimal paymentAmount = BigDecimal.valueOf(200, 0);
            instance.processLoanPayment(loanId, paymentAmount, "Loan payment");

            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT loan_amount FROM loans WHERE loan_id = ?")) {
                statement.setInt(1, loanId);
                ResultSet rs = statement.executeQuery();
                assertTrue(rs.next(), "Loan should exist");
                BigDecimal loanAmount = rs.getBigDecimal("loan_amount");
                assertEquals(0, loan.getLoanAmount().compareTo(loanAmount), "Loan amount should remain unchanged");
            }

            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT COUNT(*) FROM loan_payments WHERE loan_id = ? and payment_amount = ?")) {
                statement.setInt(1, loanId);
                statement.setBigDecimal(2, paymentAmount);
                ResultSet rs = statement.executeQuery();
                assertTrue(rs.next(), "Payment record count should be available");
                assertEquals(0, rs.getInt(1), "Should have no payment records");
            }

            verify(spyConnection, description("Autocommit should be disabled")).setAutoCommit(false);
            verify(spyConnection, description("SERIALIZABLE isolation level should be set")).setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            verify(spyConnection, description("Should rollback to savepoint when it is present")).rollback(any(Savepoint.class));
            verify(spyConnection, description(
                    times(2), "Should retry loan payments insert when SQLException occurs"))
                    .commit();
            verify(spyConnection, description("Should rollback entire transaction")).rollback();
        }

        @Test
        @DisplayName("should throw DaoOperationException when SQLException occurs during rollback")
        @SneakyThrows
        void processLoanPayment_whenSQLExceptionDuringRollback_throwDaoOperationException() {
            int loanId = 1;

            doThrow(SQLException.class).when(spyConnection).commit();
            lenient().doThrow(SQLException.class).when(spyConnection).rollback(any(Savepoint.class));
            doThrow(SQLException.class).when(spyConnection).rollback();

            BigDecimal paymentAmount = BigDecimal.valueOf(200, 0);
            assertThrows(DaoOperationException.class, () -> instance.processLoanPayment(loanId, paymentAmount, "Loan payment"));

            verify(spyConnection, description("Autocommit should be disabled")).setAutoCommit(false);
            verify(spyConnection, description("SERIALIZABLE isolation level should be set")).setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            verify(spyConnection, description("Should rollback to savepoint when it is present")).rollback(any(Savepoint.class));
            verify(spyConnection, description(
                    atLeastOnce(), "Should rollback entire transaction"))
                    .rollback();
        }
    }

    @SneakyThrows
    private List<Account> selectAccounts() {
        try (Statement statement = connection.createStatement()) {
            if (!statement.execute("SELECT * FROM accounts"))
                throw new DaoOperationException("Failed to select from accounts");

            ResultSet resultSet = statement.getResultSet();
            List<Account> accounts = new ArrayList<>();
            while (resultSet.next()) {
                int accountId = resultSet.getInt("account_id");
                int customerId = resultSet.getInt("customer_id");
                AccountType accountType = AccountType.fromString(resultSet.getString("account_type"));
                BigDecimal balance = resultSet.getBigDecimal("balance");
                Timestamp createdAt = resultSet.getTimestamp("created_at");

                accounts.add(new Account(accountId, customerId, accountType, balance, createdAt));
            }

            return accounts;
        }
    }

    @SneakyThrows
    private Account selectAccount(int id) {
        try (PreparedStatement select = connection.prepareStatement("SELECT * FROM accounts WHERE account_id = ?")) {
            select.setInt(1, id);
            ResultSet resultSet = select.executeQuery();
            if (!resultSet.next()) {
                throw new AccountNotFoundException("Account with id %d not found".formatted(id));
            }

            int accountId = resultSet.getInt("account_id");
            int customerId = resultSet.getInt("customer_id");
            AccountType accountType = AccountType.fromString(resultSet.getString("account_type"));
            BigDecimal balance = resultSet.getBigDecimal("balance");
            Timestamp createdAt = resultSet.getTimestamp("created_at");

            return new Account(accountId, customerId, accountType, balance, createdAt);
        }
    }

    @SneakyThrows
    private Loan selectLoan(int id) {
        try (PreparedStatement select = connection.prepareStatement("SELECT * FROM loans WHERE loan_id = ?")) {
            select.setInt(1, id);
            ResultSet resultSet = select.executeQuery();
            if (!resultSet.next()) {
                throw new LoanNotFoundException("Loan with id %d not found".formatted(id));
            }

            int loanId = resultSet.getInt("loan_id");
            int customerId = resultSet.getInt("customer_id");
            String loanType = resultSet.getString("loan_type");
            BigDecimal loanAmount = resultSet.getBigDecimal("loan_amount");
            BigDecimal interestRate = resultSet.getBigDecimal("interest_rate");
            LocalDate loanStartDate = resultSet.getDate("loan_start_date").toLocalDate();
            LocalDate loanEndDate = resultSet.getDate("loan_end_date").toLocalDate();

            return Loan.builder()
                    .loanId(loanId)
                    .customerId(customerId)
                    .loanType(loanType)
                    .loanAmount(loanAmount)
                    .interestRate(interestRate)
                    .loanStartDate(loanStartDate)
                    .loanEndDate(loanEndDate)
                    .build();
        }
    }

    private List<Account> selectNewAccounts(List<Account> existing) {
        return selectAccounts().stream()
                .filter(account -> !existing.contains(account))
                .toList();
    }

}
