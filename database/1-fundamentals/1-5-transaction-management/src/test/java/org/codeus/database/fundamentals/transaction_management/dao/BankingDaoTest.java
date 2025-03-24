package org.codeus.database.fundamentals.transaction_management.dao;

import lombok.SneakyThrows;
import org.codeus.database.fundamentals.transaction_management.error.CustomerNotFoundException;
import org.codeus.database.fundamentals.transaction_management.error.DaoOperationException;
import org.codeus.database.fundamentals.transaction_management.model.Account;
import org.codeus.database.fundamentals.transaction_management.model.AccountType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.description;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class BankingDaoTest extends EmbeddedPostgreSqlSetup {

    private BankingDaoImpl instance;
    private Connection spyConnection;

    @BeforeEach
    void setUp() {
        spyConnection = spy(EmbeddedPostgreSqlSetup.connection);
        instance = new BankingDaoImpl(spyConnection);
//        spyConnection.setAutoCommit(true);
    }

    @AfterEach
    void tearDown() {
//        reset(spyConnection);
    }

    @Nested
    @DisplayName("createAccount Tests")
    class CreateAccountTests {

        @Test
        @DisplayName("should create account")
        void createAccount() {
            List<Account> before = selectAccounts();

            Account account = Account.builder()
                    .customerId(createCustomer())
                    .accountType(AccountType.CHECKING)
                    .balance(BigDecimal.valueOf(100, 2))
                    .build();

            instance.createAccount(account);

            List<Account> after = selectNewAccounts(before);

            assertEquals(1, after.size(), "Should have created 1 account");
            Account createdAccount = after.get(0);
            assertEquals(account.getCustomerId(), createdAccount.getCustomerId(), "CustomerId should match");
            assertEquals(account.getAccountType(), createdAccount.getAccountType(), "AccountType should match");
            assertEquals(account.getBalance(), createdAccount.getBalance(), "Balance should match");
            assertNotNull(createdAccount.getCreatedAt(), "CreatedAt should be set");
        }

        @Test
        @DisplayName("should throw exception when account exists")
        void createAccount_whenCustomerDoesNotExist_throwException() {
            List<Account> before = selectAccounts();

            Account account = Account.builder()
                    .customerId(-10)
                    .accountType(AccountType.SAVINGS)
                    .balance(BigDecimal.valueOf(1000, 2))
                    .build();

            assertThrows(CustomerNotFoundException.class, () -> instance.createAccount(account));

            List<Account> after = selectNewAccounts(before);
            assertTrue(after.isEmpty(), "Should not have created any new accounts");
        }

        @Test
        @DisplayName("should generate and set id when creating account")
        void createAccount_generateId() {
            List<Account> before = selectAccounts();

            Account account = Account.builder()
                    .customerId(createCustomer())
                    .accountType(AccountType.CHECKING)
                    .balance(BigDecimal.valueOf(100, 2))
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
                    .balance(BigDecimal.valueOf(1000, 2))
                    .build();

            assertThrows(CustomerNotFoundException.class, () -> instance.createAccount(account));
        }

        @Test
        @DisplayName("should wrap sql exceptions into custom exceptions")
        void createAccount_wrapSqlExceptions() throws SQLException {
            Account account = Account.builder()
                    .customerId(createCustomer())
                    .accountType(AccountType.SAVINGS)
                    .balance(BigDecimal.valueOf(1000, 2))
                    .build();

            doThrow(SQLException.class).when(spyConnection).commit();
            doThrow(SQLException.class).when(spyConnection).rollback();

            assertThrows(DaoOperationException.class, () -> instance.createAccount(account));

            verify(spyConnection, description("Autocommit should be disabled")).setAutoCommit(false);
            verify(spyConnection, description("Transaction should be rolled back when exception is caught")).rollback();
        }
    }

    @SneakyThrows
    private List<Account> selectAccounts() {
        // todo: probably add logs
        try (Statement statement = connection.createStatement()) {
            if (!statement.execute("SELECT * FROM accounts"))
                // todo: change the exception
                throw new RuntimeException("Failed to select from accounts");

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

    private List<Account> selectNewAccounts(List<Account> existing) {
        return selectAccounts().stream()
                .filter(account -> !existing.contains(account))
                .toList();
    }

    @SneakyThrows
    private int createCustomer() {
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO customers(first_name, last_name, email) VALUES ('John', 'Doe', 'john@doe.com')",
//                Statement.RETURN_GENERATED_KEYS)) {
                new String[]{"customer_id"})) {
            if (statement.executeUpdate() != 1) {
                throw new RuntimeException("Failed to insert customer");
            }

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (!generatedKeys.next()) {
                throw new RuntimeException("Failed to get generated keys");
            }

            return generatedKeys.getInt(1);
        }
        // todo: rollback on error
    }

    @Test
    void withdrawMoney() {
    }

    @Test
    void transferMoney() {
    }

    @Test
    void processLoanPayment() {
    }

    @Test
    void processLoanRequest() {
    }

    @Test
    void processEmployeePayroll() {
    }

    @Test
    void testIsolationLevel() {
    }

    @Test
    void simulateDeadlock() {
    }

    @Test
    void createUserWithRole() {
    }

    @Test
    void demonstrateAnomalies() {
    }
}
