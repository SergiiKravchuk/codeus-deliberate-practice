package org.codeus.fundamentals.stored_procedures;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class SqlQueriesTest extends EmbeddedPostgreSqlSetup {
    private static final String TASK_DIR = "tasks/";

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class TransferFundsTest {
        private static final String TASK_1_SQL_PATH = TASK_DIR + "01_transfer-funds-between-accounts.sql";

        @AfterAll
        public static void rollbackBlockChanges() throws SQLException {
            connection.rollback();
        }

        @Test
        @Order(1)
        void O1_testSuccessfulTransferFunds() throws SQLException, IOException {
            executeQueryWithoutResultFromFile(TASK_1_SQL_PATH);

            int senderId = createSavingAccount(new BigDecimal("200.00"));
            int receiverId = createSavingAccount(new BigDecimal("50.00"));

            CallableStatement stmt = connection.prepareCall("CALL transfer_funds(?, ?, ?)");
            stmt.setInt(1, senderId);
            stmt.setInt(2, receiverId);
            stmt.setBigDecimal(3, new BigDecimal("100.00"));
            stmt.execute();


            // Verify balances
            BigDecimal senderBalance = getBalance(senderId);
            BigDecimal receiverBalance = getBalance(receiverId);
            assertEquals(new BigDecimal("100.00"), senderBalance);
            assertEquals(new BigDecimal("150.00"), receiverBalance);

            // Verify transaction logs
            ResultSet tx = connection.createStatement().executeQuery("""
                    SELECT COUNT(*) FROM transactions 
                    WHERE account_id IN (%d,%d) AND transaction_type IN ('transfer','deposit')
                    """.formatted(senderId, receiverId));
            assertTrue(tx.next());
            assertTrue(tx.getInt(1) >= 2);
        }

        @Test
        @Order(10)
        void O1_testSenderDoesNotExist() throws SQLException, IOException {
            executeQueryWithoutResultFromFile(TASK_1_SQL_PATH);

            int receiverId = createSavingAccount(new BigDecimal("100.00"));

            SQLException ex = assertThrows(SQLException.class, () -> {
                CallableStatement stmt = connection.prepareCall("CALL transfer_funds(?, ?, ?)");
                stmt.setInt(1, 999999); // non-existent sender
                stmt.setInt(2, receiverId);
                stmt.setBigDecimal(3, new BigDecimal("50.00"));
                stmt.execute();
            });

            assertTrue(ex.getMessage().contains("Sender account 999999 not found."));
        }

        @Test
        @Order(15)
        void O1_testReceiverDoesNotExist() throws SQLException, IOException {
            executeQueryWithoutResultFromFile(TASK_1_SQL_PATH);

            int senderId = createSavingAccount(new BigDecimal("100.00"));

            SQLException ex = assertThrows(SQLException.class, () -> {
                CallableStatement stmt = connection.prepareCall("CALL transfer_funds(?, ?, ?)");
                stmt.setInt(1, senderId);
                stmt.setInt(2, 999999); // non-existent receiver
                stmt.setBigDecimal(3, new BigDecimal("50.00"));
                stmt.execute();
            });

            assertTrue(ex.getMessage().contains("Receiver account 999999 not found."));
        }
    }

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class ReverseTransactionTest {
        private static final String TASK_2_SQL_PATH = TASK_DIR + "02_reverse-transaction.sql";

        @AfterAll
        public static void rollbackBlockChanges() throws SQLException {
            connection.rollback();
        }

        @Test
        @Order(1)
        void O1_testSuccessfulReverseTransaction() throws SQLException, IOException {
            executeQueryWithoutResultFromFile(TASK_2_SQL_PATH);

            int accountId = createSavingAccount(new BigDecimal("200.00"));

            // Insert initial transaction (simulate deposit)
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO transactions(account_id, amount, transaction_type, target_account_id) VALUES (?, ?, 'deposit', NULL) RETURNING id");
            ps.setInt(1, accountId);
            ps.setBigDecimal(2, new BigDecimal("100.00"));
            ResultSet rs = ps.executeQuery();
            rs.next();
            int txId = rs.getInt(1);

            // Call reverse_transaction procedure
            CallableStatement stmt = connection.prepareCall("CALL reverse_transaction(?)");
            stmt.setInt(1, txId);
            stmt.execute();

            // Verify balance is reverted
            BigDecimal balance = getBalance(accountId);
            assertEquals(new BigDecimal("100.00"), balance);

            // Verify reversal transaction logged
            ResultSet tx = connection.createStatement().executeQuery(
                    "SELECT * FROM transactions WHERE account_id = " + accountId + " AND transaction_type = 'reversal' ORDER BY id DESC LIMIT 1");
            assertTrue(tx.next());
            assertEquals(new BigDecimal("-100.00"), tx.getBigDecimal("amount"));
        }

        @Test
        @Order(10)
        void O2_testTransactionNotFound() throws SQLException, IOException {
            executeQueryWithoutResultFromFile(TASK_2_SQL_PATH);

            SQLException ex = assertThrows(SQLException.class, () -> {
                CallableStatement stmt = connection.prepareCall("CALL reverse_transaction(?)");
                stmt.setInt(1, 999999); // non-existent transaction id
                stmt.execute();
            });

            assertTrue(ex.getMessage().contains("Transaction 999999 not found."));
        }

        @Test
        @Order(15)
        void O3_testAlreadyReversedTransaction() throws SQLException, IOException {
            executeQueryWithoutResultFromFile(TASK_2_SQL_PATH);

            int accountId = createSavingAccount(new BigDecimal("200.00"));

            // Insert reversal transaction to simulate already reversed transaction
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO transactions(account_id, amount, transaction_type, target_account_id) VALUES (?, ?, 'reversal', NULL) RETURNING id");
            ps.setInt(1, accountId);
            ps.setBigDecimal(2, new BigDecimal("-100.00"));
            ResultSet rs = ps.executeQuery();
            rs.next();
            int txId = rs.getInt(1);

            SQLException ex = assertThrows(SQLException.class, () -> {
                CallableStatement stmt = connection.prepareCall("CALL reverse_transaction(?)");
                stmt.setInt(1, txId);
                stmt.execute();
            });

            assertTrue(ex.getMessage().contains("is already a reversal"));
        }
    }

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class LogLargeDepositsTest {
        private static final String TASK_3_SQL_PATH = TASK_DIR + "03_log-large-deposits.sql";

        @AfterEach
        public void rollbackBlockChanges() throws SQLException {
            connection.rollback();
        }

        @Test
        @Order(1)
        void O1_testLogsDepositsAboveThreshold() throws SQLException, IOException {
            executeQueryWithoutResultFromFile(TASK_3_SQL_PATH);
            connection.setAutoCommit(false);

            int accountId = createSavingAccount(new BigDecimal("500.00"));

            // Insert some deposits
            insertTransaction(accountId, new BigDecimal("300.00"), "deposit");
            insertTransaction(accountId, new BigDecimal("50.00"), "deposit");
            insertTransaction(accountId, new BigDecimal("400.00"), "deposit");

            // Call the procedure with threshold 250.00
            CallableStatement stmt = connection.prepareCall("CALL log_large_deposits(?)");
            stmt.setBigDecimal(1, new BigDecimal("250.00"));
            stmt.execute();

            // Verify audit table contains only large deposits
            ResultSet rs = connection.createStatement().executeQuery("""
                        SELECT COUNT(*) FROM large_deposit_audit
                    """);
            assertTrue(rs.next());
            assertEquals(2, rs.getInt(1));
        }

        @Test
        @Order(5)
        void O2_testNoDuplicatesLogged() throws SQLException, IOException {
            executeQueryWithoutResultFromFile(TASK_3_SQL_PATH);
            connection.setAutoCommit(false);

            int accountId = createSavingAccount(new BigDecimal("600.00"));
            int txId = insertTransaction(accountId, new BigDecimal("350.00"), "deposit");

            // First call
            CallableStatement stmt1 = connection.prepareCall("CALL log_large_deposits(?)");
            stmt1.setBigDecimal(1, new BigDecimal("300.00"));
            stmt1.execute();

            // Second call with same threshold (shouldn't log again)
            CallableStatement stmt2 = connection.prepareCall("CALL log_large_deposits(?)");
            stmt2.setBigDecimal(1, new BigDecimal("300.00"));
            stmt2.execute();

            // Ensure only one audit record exists
            ResultSet rs = connection.createStatement().executeQuery("""
                        SELECT COUNT(*) FROM large_deposit_audit WHERE transaction_id = %d
                    """.formatted(txId));
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1));
        }

        @Test
        @Order(10)
        void O3_testNoDepositsAboveThreshold() throws SQLException, IOException {
            executeQueryWithoutResultFromFile(TASK_3_SQL_PATH);

            int accountId = createSavingAccount(new BigDecimal("200.00"));

            insertTransaction(accountId, new BigDecimal("50.00"), "deposit");
            insertTransaction(accountId, new BigDecimal("80.00"), "deposit");

            CallableStatement stmt = connection.prepareCall("CALL log_large_deposits(?)");
            stmt.setBigDecimal(1, new BigDecimal("500.00"));
            stmt.execute();

            // Ensure audit table is empty
            ResultSet rs = connection.createStatement().executeQuery("SELECT COUNT(*) FROM large_deposit_audit");
            assertTrue(rs.next());
            assertEquals(0, rs.getInt(1));
        }
    }

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class ResetBalancesTest {
        private static final String TASK_4_SQL_PATH = TASK_DIR + "04_reset-all-balances.sql";

        @AfterEach
        public void rollbackBlockChanges() throws SQLException {
            connection.rollback();
        }

        @Test
        @Order(1)
        void O1_testResetAllBalancesSuccessfully() throws SQLException, IOException {
            executeQueryWithoutResultFromFile(TASK_4_SQL_PATH);

            int acc1 = createSavingAccount(new BigDecimal("100.00"));
            int acc2 = createSavingAccount(new BigDecimal("300.00"));
            int acc3 = createSavingAccount(new BigDecimal("50.00"));

            CallableStatement stmt = connection.prepareCall("CALL reset_balances(?)");
            stmt.setBigDecimal(1, new BigDecimal("200.00"));
            stmt.execute();

            assertEquals(new BigDecimal("200.00"), getBalance(acc1));
            assertEquals(new BigDecimal("200.00"), getBalance(acc2));
            assertEquals(new BigDecimal("200.00"), getBalance(acc3));
        }

        @Test
        @Order(5)
        void O2_testResetToZeroBalance() throws SQLException, IOException {
            executeQueryWithoutResultFromFile(TASK_4_SQL_PATH);

            int acc = createSavingAccount(new BigDecimal("999.99"));

            CallableStatement stmt = connection.prepareCall("CALL reset_balances(?)");
            stmt.setBigDecimal(1, new BigDecimal("0.00"));
            stmt.execute();

            assertEquals(new BigDecimal("0.00"), getBalance(acc));
        }

        @Test
        @Order(10)
        void O3_testNegativeBalanceThrowsException() throws SQLException, IOException {
            executeQueryWithoutResultFromFile(TASK_4_SQL_PATH);

            SQLException ex = assertThrows(SQLException.class, () -> {
                CallableStatement stmt = connection.prepareCall("CALL reset_balances(?)");
                stmt.setBigDecimal(1, new BigDecimal("-50.00"));
                stmt.execute();
            });

            assertTrue(ex.getMessage().contains("Default balance cannot be negative"));
        }
    }

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class AssignBranchManagerTest {
        private static final String TASK_5_SQL_PATH = TASK_DIR + "05_assign-branch-manager.sql";

        @AfterAll
        public static void rollbackBlockChanges() throws SQLException {
            connection.rollback();
        }

        @Test
        @Order(1)
        void O1_assignManagerSuccessfully() throws SQLException, IOException {
            executeQueryWithoutResultFromFile(TASK_5_SQL_PATH);

            int branchId = createBranch("Central Branch", "London");
            int employeeId = createEmployee("John", "Doe", "Manager", new BigDecimal("8000.00"), null);

            CallableStatement stmt = connection.prepareCall("CALL assign_branch_manager(?, ?)");
            stmt.setInt(1, branchId);
            stmt.setInt(2, employeeId);
            stmt.execute();

            ResultSet rs = connection.createStatement().executeQuery(
                    "SELECT manager_id FROM branches WHERE id = " + branchId);
            assertTrue(rs.next());
            assertEquals(employeeId, rs.getInt("manager_id"));
        }

        @Test
        @Order(5)
        void O2_assignToNonexistentBranch() throws SQLException, IOException {
            executeQueryWithoutResultFromFile(TASK_5_SQL_PATH);

            int employeeId = createEmployee("Jane", "Smith", "Manager", new BigDecimal("8000.00"), null);

            SQLException ex = assertThrows(SQLException.class, () -> {
                CallableStatement stmt = connection.prepareCall("CALL assign_branch_manager(?, ?)");
                stmt.setInt(1, 999999);
                stmt.setInt(2, employeeId);
                stmt.execute();
            });

            assertTrue(ex.getMessage().contains("Branch 999999 does not exist"));
        }

        @Test
        @Order(10)
        void O3_assignNonexistentEmployee() throws SQLException, IOException {
            executeQueryWithoutResultFromFile(TASK_5_SQL_PATH);

            int branchId = createBranch("North Branch", "Liverpool");

            SQLException ex = assertThrows(SQLException.class, () -> {
                CallableStatement stmt = connection.prepareCall("CALL assign_branch_manager(?, ?)");
                stmt.setInt(1, branchId);
                stmt.setInt(2, 888888);
                stmt.execute();
            });

            assertTrue(ex.getMessage().contains("Employee 888888 does not exist"));
        }

        @Test
        @Order(15)
        void O4_branchAlreadyHasManager() throws SQLException, IOException {
            executeQueryWithoutResultFromFile(TASK_5_SQL_PATH);

            int branchId = createBranch("East Branch", "London");
            int emp1 = createEmployee("Alice", "Casio", "Manager", new BigDecimal("8000.00"), null);
            int emp2 = createEmployee("Bob", "Martin", "Manager", new BigDecimal("8000.00"), null);

            // First assignment
            CallableStatement stmt1 = connection.prepareCall("CALL assign_branch_manager(?, ?)");
            stmt1.setInt(1, branchId);
            stmt1.setInt(2, emp1);
            stmt1.execute();

            // Second attempt (should fail)
            SQLException ex = assertThrows(SQLException.class, () -> {
                CallableStatement stmt2 = connection.prepareCall("CALL assign_branch_manager(?, ?)");
                stmt2.setInt(1, branchId);
                stmt2.setInt(2, emp2);
                stmt2.execute();
            });

            assertTrue(ex.getMessage().contains("Branch " + branchId + " already has a manager"));
        }

        @Test
        @Order(20)
        void O5_employeeAlreadyManagesAnotherBranch() throws SQLException, IOException {
            executeQueryWithoutResultFromFile(TASK_5_SQL_PATH);

            int empId = createEmployee("Charlie", "Solo", "Manager", new BigDecimal("8000.00"), null);
            int branch1 = createBranch("First Branch", "London");
            int branch2 = createBranch("Second Branch", "London");

            CallableStatement stmt1 = connection.prepareCall("CALL assign_branch_manager(?, ?)");
            stmt1.setInt(1, branch1);
            stmt1.setInt(2, empId);
            stmt1.execute();

            SQLException ex = assertThrows(SQLException.class, () -> {
                CallableStatement stmt2 = connection.prepareCall("CALL assign_branch_manager(?, ?)");
                stmt2.setInt(1, branch2);
                stmt2.setInt(2, empId);
                stmt2.execute();
            });

            assertTrue(ex.getMessage().contains("Employee " + empId + " is already managing another branch"));
        }
    }

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class ApproveLoanApplicationTest {
        private static final String TASK_6_SQL_PATH = TASK_DIR + "06_approve-loan-application.sql";

        @AfterEach
        public void rollbackBlockChanges() throws SQLException {
            connection.rollback();
        }

        @Test
        @Order(1)
        void O1_approvePendingLoanSuccessfully() throws SQLException, IOException {
            executeQueryWithoutResultFromFile(TASK_6_SQL_PATH);

            int branchId = createBranch("Loan Branch", "Chicago");
            int managerId = createEmployee("Emma", "Stone", "Manager", new BigDecimal("9000.00"), branchId);
            assignManager(branchId, managerId);

            int customerId = createCustomer("Will", "Smith", "will@example.com", "1234567890", "LA");
            int loanId = createLoan(customerId, branchId, new BigDecimal("5000.00"), new BigDecimal("5.5"), 12, "pending");

            CallableStatement stmt = connection.prepareCall("CALL approve_loan_application(?, ?)");
            stmt.setInt(1, loanId);
            stmt.setInt(2, managerId);
            stmt.execute();

            ResultSet rs = connection.createStatement().executeQuery(
                    "SELECT status, approved_by FROM loans WHERE id = " + loanId);
            assertTrue(rs.next());
            assertEquals("approved", rs.getString("status"));
            assertEquals(managerId, rs.getInt("approved_by"));
        }

        @Test
        @Order(5)
        void O2_loanDoesNotExist() throws SQLException, IOException {
            executeQueryWithoutResultFromFile(TASK_6_SQL_PATH);

            int managerId = createEmployee("Lana", "Grey", "Manager", new BigDecimal("8500.00"), null);

            SQLException ex = assertThrows(SQLException.class, () -> {
                CallableStatement stmt = connection.prepareCall("CALL approve_loan_application(?, ?)");
                stmt.setInt(1, 999999);
                stmt.setInt(2, managerId);
                stmt.execute();
            });

            assertTrue(ex.getMessage().contains("Loan 999999 does not exist"));
        }

        @Test
        @Order(10)
        void O3_loanNotPending() throws SQLException, IOException {
            executeQueryWithoutResultFromFile(TASK_6_SQL_PATH);

            int branchId = createBranch("Closed Loans", "Dallas");
            int managerId = createEmployee("Bruce", "Wayne", "Manager", new BigDecimal("12000.00"), branchId);
            assignManager(branchId, managerId);

            int customerId = createCustomer("Clark", "Kent", "clark@example.com", "5551234567", "Metropolis");
            int loanId = createLoan(customerId, branchId, new BigDecimal("8000.00"), new BigDecimal("4.5"), 24, "closed");

            SQLException ex = assertThrows(SQLException.class, () -> {
                CallableStatement stmt = connection.prepareCall("CALL approve_loan_application(?, ?)");
                stmt.setInt(1, loanId);
                stmt.setInt(2, managerId);
                stmt.execute();
            });

            assertTrue(ex.getMessage().contains("Loan " + loanId + " is not pending"));
        }

        @Test
        @Order(15)
        void O4_wrongManagerAssigned() throws SQLException, IOException {
            executeQueryWithoutResultFromFile(TASK_6_SQL_PATH);

            int branchId = createBranch("Mismatch Branch", "Seattle");
            int correctManager = createEmployee("Elon", "Musk", "Manager", new BigDecimal("10000.00"), branchId);
            int wrongManager = createEmployee("Mark", "Zuckerberg", "Manager", new BigDecimal("9500.00"), null);
            assignManager(branchId, correctManager);

            int customerId = createCustomer("Jeff", "Bezos", "jeff@example.com", "3216549870", "Amazon HQ");
            int loanId = createLoan(customerId, branchId, new BigDecimal("7000.00"), new BigDecimal("6.0"), 18, "pending");

            SQLException ex = assertThrows(SQLException.class, () -> {
                CallableStatement stmt = connection.prepareCall("CALL approve_loan_application(?, ?)");
                stmt.setInt(1, loanId);
                stmt.setInt(2, wrongManager);
                stmt.execute();
            });

            assertTrue(ex.getMessage().contains("Employee " + wrongManager + " is not the manager of branch"));
        }
    }

    private int createLoan(int customerId, int branchId, BigDecimal amount, BigDecimal interestRate, int termMonths, String status) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO loans(customer_id, branch_id, amount, interest_rate, term_months, status) " +
                        "VALUES (?, ?, ?, ?, ?, ?) RETURNING id");
        stmt.setInt(1, customerId);
        stmt.setInt(2, branchId);
        stmt.setBigDecimal(3, amount);
        stmt.setBigDecimal(4, interestRate);
        stmt.setInt(5, termMonths);
        stmt.setString(6, status);
        ResultSet rs = stmt.executeQuery();
        rs.next();
        return rs.getInt("id");
    }


    private int createCustomer(String firstName, String lastName, String email, String phone, String address) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO customers(first_name, last_name, email, phone, address) " +
                        "VALUES (?, ?, ?, ?, ?) RETURNING id"
        );
        stmt.setString(1, firstName);
        stmt.setString(2, lastName);
        stmt.setString(3, email);
        stmt.setString(4, phone);
        stmt.setString(5, address);
        ResultSet rs = stmt.executeQuery();
        rs.next();
        return rs.getInt("id");
    }

    private void assignManager(int branchId, int managerId) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(
                "UPDATE branches SET manager_id = ? WHERE id = ?"
        );
        stmt.setInt(1, managerId);
        stmt.setInt(2, branchId);
        stmt.executeUpdate();
    }

    private int createEmployee(String firstName, String lastName, String position, BigDecimal salary, Integer branchId) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("""
                    INSERT INTO employees (first_name, last_name, position, salary, branch_id)
                    VALUES (?, ?, ?, ?, ?)
                    RETURNING id
                """);
        stmt.setString(1, firstName);
        stmt.setString(2, lastName);
        stmt.setString(3, position);
        stmt.setBigDecimal(4, salary);

        if (branchId != null) {
            stmt.setInt(5, branchId);
        } else {
            stmt.setNull(5, java.sql.Types.INTEGER);
        }

        ResultSet rs = stmt.executeQuery();
        assertTrue(rs.next());
        return rs.getInt("id");
    }


    private int createBranch(String name, String location) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("""
                    INSERT INTO branches (name, location)
                    VALUES (?, ?)
                    RETURNING id
                """);
        stmt.setString(1, name);
        stmt.setString(2, location);

        ResultSet rs = stmt.executeQuery();
        assertTrue(rs.next());
        return rs.getInt("id");
    }

    private int createSavingAccount(BigDecimal balance) {
        return createAccount(1, "savings", balance);
    }

    private int createAccount(int customerId, String accountType, BigDecimal balance) {
        String sql = """
                    INSERT INTO accounts (customer_id, account_type, balance)
                    VALUES (?, ?, ?)
                    RETURNING id
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.setString(2, accountType);
            ps.setBigDecimal(3, balance);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            } else {
                throw new SQLException("Account creation failed.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create account", e);
        }
    }

    private int insertTransaction(int accountId, BigDecimal amount, String type) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO transactions(account_id, amount, transaction_type) VALUES (?, ?, ?) RETURNING id");
        ps.setInt(1, accountId);
        ps.setBigDecimal(2, amount);
        ps.setString(3, type);
        ResultSet rs = ps.executeQuery();
        rs.next();
        return rs.getInt(1);
    }


    private BigDecimal getBalance(int accountId) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT balance FROM accounts WHERE id = ?"
        )) {
            ps.setInt(1, accountId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getBigDecimal("balance");
            } else {
                throw new SQLException("Account not found");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve balance", e);
        }
    }
}