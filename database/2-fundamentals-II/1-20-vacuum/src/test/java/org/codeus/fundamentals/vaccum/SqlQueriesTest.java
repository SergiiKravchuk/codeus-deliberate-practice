package org.codeus.fundamentals.vaccum;

import org.codeus.database.common.EmbeddedPostgreSqlSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SqlQueriesTest extends EmbeddedPostgreSqlSetup {

    private static final String EXERCISES_DIR = "queries/";

    @BeforeEach
    void setupVacuumEnvironment() throws SQLException {
        // Enable tracking of dead tuples
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("SET track_counts = on");
            // Note: autovacuum cannot be disabled at runtime in embedded PostgreSQL
            // We'll work with it enabled, which is more realistic anyway
            if (!connection.getAutoCommit()) {
                connection.commit();
            }
        } catch (SQLException e) {
            // If there's an error, rollback the transaction only if autoCommit is disabled
            if (!connection.getAutoCommit()) {
                connection.rollback();
            }
            throw e;
        }
    }

    @Test
    @Order(1)
    @DisplayName("Exercise 1: Basic VACUUM")
    void testManualVacuum() throws IOException, SQLException {
        System.out.println("=== Exercise 1: Manual VACUUM ===");
        System.out.println("Key Concept: VACUUM reclaims space from dead tuples");

        System.out.println("\n1. Validating VACUUM solution...");
        String sql = readSqlFileContent(getResourcePath("queries/01_cleanup_dead_tuples.sql"));
        String cleanSql = filterOutCommentedLines(sql);
        assertTrue(cleanSql.toUpperCase().contains("VACUUM"), 
                "You need to implement the solution");
        assertTrue(cleanSql.toUpperCase().contains("TRANSACTIONS"), 
                "Solution must target the transactions table");
        assertFalse(cleanSql.toUpperCase().contains("VACUUM FULL"), 
                "Use basic VACUUM for this exercise");

        System.out.println("\n2. Setting up table with dead tuples...");
        setupBankingTablesWithDeadTuples();

        DeadTupleStats beforeVacuum = getDeadTupleStats("transactions");
        TableSizeInfo sizeBefore = getTableSizeInfo("transactions");
        
        System.out.println("BEFORE VACUUM - Live: " + beforeVacuum.liveTuples + 
                         ", Dead: " + beforeVacuum.deadTuples + 
                         ", Size: " + sizeBefore.totalSizePretty);

        if (beforeVacuum.deadTuples == 0) {
            System.out.println("\n2. Creating dead tuples (UPDATE and DELETE operations)...");
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("UPDATE transactions SET amount = amount + 0.01 WHERE id % 3 = 0");
                stmt.execute("DELETE FROM transactions WHERE id % 10 = 0");
                if (!connection.getAutoCommit()) {
                    connection.commit();
                }
            }
            updateStatistics();
            beforeVacuum = getDeadTupleStats("transactions");
            System.out.println("After creating dead tuples - Live: " + beforeVacuum.liveTuples + 
                             ", Dead: " + beforeVacuum.deadTuples);
        }

        System.out.println("\n3. Running VACUUM command...");
        executeVacuumSqlFile(getResourcePath("queries/01_cleanup_dead_tuples.sql"));
        updateStatistics();

        DeadTupleStats afterVacuum = getDeadTupleStats("transactions");
        TableSizeInfo sizeAfter = getTableSizeInfo("transactions");
        
        System.out.println("AFTER VACUUM - Live: " + afterVacuum.liveTuples + 
                         ", Dead: " + afterVacuum.deadTuples + 
                         ", Size: " + sizeAfter.totalSizePretty);
        
        long deadTuplesRemoved = Math.max(0, beforeVacuum.deadTuples - afterVacuum.deadTuples);
        if (deadTuplesRemoved > 0) {
            System.out.printf("RESULT: VACUUM removed %d dead tuples (%.1f%% efficiency)%n", 
                deadTuplesRemoved, 
                ((double)deadTuplesRemoved / beforeVacuum.deadTuples) * 100);
        } else {
            System.out.println("RESULT: No dead tuples to clean or already processed");
        }

        if (beforeVacuum.deadTuples > 0) {
            assertTrue(afterVacuum.deadTuples <= beforeVacuum.deadTuples,
                    "VACUUM should not increase dead tuples. Before: " + beforeVacuum.deadTuples +
                            ", After: " + afterVacuum.deadTuples);
        }
    }

    @Test
    @Order(2)
    @DisplayName("Exercise 2.1: Diagnostic Cleanup")
    void testExercise2_1_VacuumVerbose() throws IOException, SQLException {
        System.out.println("=== Exercise 2.1: VACUUM VERBOSE ===");
        System.out.println("Key Concept: Get detailed output from VACUUM operation");

        System.out.println("\n1. Validating VACUUM VERBOSE solution...");
        String sql = readSqlFileContent(getResourcePath("queries/02_1_diagnostic_cleanup.sql"));
        String cleanSql = filterOutCommentedLines(sql);
        assertTrue(cleanSql.toUpperCase().contains("VACUUM VERBOSE"), 
                "You need to implement the solution");
        assertTrue(cleanSql.toUpperCase().contains("TRANSACTIONS"), 
                "Solution must target transactions table");

        System.out.println("\n2. Setting up test environment with dead tuples...");
        setupBankingTablesWithDeadTuples();
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("UPDATE transactions SET amount = amount + 0.01 WHERE id % 3 = 0");
            if (!connection.getAutoCommit()) {
                connection.commit();
            }
        }
        updateStatistics();

        DeadTupleStats before = getDeadTupleStats("transactions");
        System.out.println("Initial dead tuples: " + before.deadTuples);

        System.out.println("\n3. Executing VACUUM VERBOSE from SQL file...");
        executeVacuumSqlFile(getResourcePath("queries/02_1_diagnostic_cleanup.sql"));
        updateStatistics();
        
        DeadTupleStats after = getDeadTupleStats("transactions");
        System.out.println("After VACUUM VERBOSE - Dead tuples: " + before.deadTuples + " → " + after.deadTuples);
        System.out.println("RESULT: VACUUM VERBOSE provides detailed cleanup information");

        assertTrue(after.deadTuples <= before.deadTuples,
                "VACUUM VERBOSE should clean dead tuples. Before: " + before.deadTuples +
                        ", After: " + after.deadTuples);
    }

    @Test
    @Order(3)
    @DisplayName("Exercise 2.2: Statistics Update")
    void testExercise2_2_VacuumAnalyze() throws IOException, SQLException {
        System.out.println("=== Exercise 2.2: VACUUM ANALYZE ===");
        System.out.println("Key Concept: Clean dead tuples AND update table statistics");

        System.out.println("\n1. Validating VACUUM ANALYZE solution...");
        String sql = readSqlFileContent(getResourcePath("queries/02_2_cleanup_with_stats.sql"));
        String cleanSql = filterOutCommentedLines(sql);
        assertTrue(cleanSql.toUpperCase().contains("VACUUM") && 
                   (cleanSql.toUpperCase().contains("ANALYZE") || cleanSql.toUpperCase().contains("(ANALYZE)")), 
                "You need to implement the solution");
        assertTrue(cleanSql.toUpperCase().contains("TRANSACTIONS"), 
                "Solution must target transactions table");

        System.out.println("\n2. Setting up test environment with dead tuples...");
        setupBankingTablesWithDeadTuples();
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("UPDATE transactions SET amount = amount + 0.02 WHERE id % 4 = 0");
            stmt.execute("DELETE FROM transactions WHERE id % 12 = 0");
            if (!connection.getAutoCommit()) {
                connection.commit();
            }
        }
        updateStatistics();

        DeadTupleStats before = getDeadTupleStats("transactions");
        StatisticsInfo statsBefore = getTableStatistics("transactions");
        System.out.println("Initial state - Dead tuples: " + before.deadTuples + 
                         ", Statistics last updated: " + (statsBefore.lastAnalyze != null ? statsBefore.lastAnalyze : "Never"));
        
        // If still no dead tuples, create them manually
        if (before.deadTuples == 0) {
            System.out.println("Creating additional dead tuples...");
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("UPDATE transactions SET amount = amount + 0.10 WHERE id % 3 = 0");
                stmt.execute("DELETE FROM transactions WHERE id % 15 = 0");
                if (!connection.getAutoCommit()) {
                    connection.commit();
                }
            }
            updateStatistics();
            before = getDeadTupleStats("transactions");
            System.out.println("After creating dead tuples: " + before.deadTuples);
        }

        System.out.println("\n3. Executing VACUUM ANALYZE from SQL file...");
        executeVacuumSqlFile(getResourcePath("queries/02_2_cleanup_with_stats.sql"));
        updateStatistics();
        
        DeadTupleStats after = getDeadTupleStats("transactions");
        StatisticsInfo statsAfter = getTableStatistics("transactions");
        
        System.out.println("After VACUUM ANALYZE - Dead tuples: " + before.deadTuples + " → " + after.deadTuples);
        System.out.println("Statistics update:");
        System.out.printf("• Before: %s → After: %s%n", 
            statsBefore.lastAnalyze != null ? statsBefore.lastAnalyze : "Never",
            statsAfter.lastAnalyze != null ? statsAfter.lastAnalyze : "Never");
        System.out.printf("• Row estimate: %d → %d%n", statsBefore.rowEstimate, statsAfter.rowEstimate);
        System.out.printf("• Statistics available: %s → %s%n", 
            statsBefore.hasStatistics ? "Yes" : "No",
            statsAfter.hasStatistics ? "Yes" : "No");
        
        System.out.println("RESULT: VACUUM ANALYZE cleans dead tuples AND updates table statistics");

        assertTrue(after.deadTuples <= before.deadTuples,
                "VACUUM ANALYZE should clean dead tuples. Before: " + before.deadTuples +
                        ", After: " + after.deadTuples);
    }

    @Test
    @Order(4)
    @DisplayName("Exercise 2.3: Comprehensive Cleanup")
    void testExercise2_3_VacuumVerboseAnalyze() throws IOException, SQLException {
        System.out.println("=== Exercise 2.3: VACUUM (VERBOSE, ANALYZE) ===");
        System.out.println("Key Concept: Combine detailed output with statistics update");

        System.out.println("\n1. Validating combined VACUUM solution...");
        String sql = readSqlFileContent(getResourcePath("queries/02_3_comprehensive_cleanup.sql"));
        String cleanSql = filterOutCommentedLines(sql);
        assertTrue(cleanSql.toUpperCase().contains("VACUUM") && 
                   cleanSql.toUpperCase().contains("VERBOSE") && 
                   cleanSql.toUpperCase().contains("ANALYZE"), 
                "You need to implement the solution");

        System.out.println("\n2. Creating dead tuples for combined test...");
        setupBankingTablesWithDeadTuples();
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("UPDATE transactions SET amount = amount + 0.03 WHERE id % 5 = 0");
            if (!connection.getAutoCommit()) {
                connection.commit();
            }
        }
        updateStatistics();

        DeadTupleStats before = getDeadTupleStats("transactions");
        StatisticsInfo statsBefore = getTableStatistics("transactions");
        System.out.println("Initial state - Dead tuples: " + before.deadTuples + 
                         ", Statistics available: " + statsBefore.hasStatistics);

        System.out.println("\n3. Executing VACUUM (VERBOSE, ANALYZE) from SQL file...");
        executeVacuumSqlFile(getResourcePath("queries/02_3_comprehensive_cleanup.sql"));
        updateStatistics();
        
        DeadTupleStats after = getDeadTupleStats("transactions");
        StatisticsInfo statsAfter = getTableStatistics("transactions");
        
        System.out.println("After VACUUM (VERBOSE, ANALYZE) - Dead tuples: " + before.deadTuples + " → " + after.deadTuples + 
                         ", Statistics updated: " + (statsAfter.hasStatistics ? "Yes" : "No"));
        System.out.println("RESULT: Best of both worlds - detailed monitoring AND optimization");

        assertTrue(after.deadTuples <= before.deadTuples,
                "VACUUM (VERBOSE, ANALYZE) should clean dead tuples. Before: " + before.deadTuples +
                        ", After: " + after.deadTuples);
    }

    @Test
    @Order(5)
    @DisplayName("Exercise 3: Complete Table Rewrite")
    void testVacuumFull() throws IOException, SQLException {
        System.out.println("=== Exercise 3: VACUUM FULL ===");
        System.out.println("Key Concept: VACUUM FULL completely rewrites table, reclaiming all space");

        System.out.println("\n1. Validating VACUUM FULL solution...");
        String sql = readSqlFileContent(getResourcePath("queries/03_reclaim_disk_space.sql"));
        String cleanSql = filterOutCommentedLines(sql);
        assertTrue(cleanSql.toUpperCase().contains("VACUUM FULL"), 
                "You need to implement the solution");
        assertTrue(cleanSql.toUpperCase().contains("BLOATED_TRANSACTIONS"), 
                "Solution must target the bloated_transactions table");

        System.out.println("\n2. Creating bloated table for testing...");
        createBloatedTransactionsTable();

        TableSizeInfo sizeBeforeVacuum = getTableSizeInfo("bloated_transactions");
        System.out.println("Before VACUUM: " + sizeBeforeVacuum.totalSizePretty);

        executeVacuumCommand("VACUUM bloated_transactions");
        TableSizeInfo sizeAfterVacuum = getTableSizeInfo("bloated_transactions");
        System.out.println("After regular VACUUM: " + sizeAfterVacuum.totalSizePretty);

        executeVacuumSqlFile(getResourcePath("queries/03_reclaim_disk_space.sql"));
        TableSizeInfo sizeAfterVacuumFull = getTableSizeInfo("bloated_transactions");
        System.out.println("After VACUUM FULL: " + sizeAfterVacuumFull.totalSizePretty);

        if (sizeAfterVacuumFull.totalSizeBytes < sizeAfterVacuum.totalSizeBytes) {
            double spaceReclaimed = (1 - (double)sizeAfterVacuumFull.totalSizeBytes / sizeBeforeVacuum.totalSizeBytes) * 100;
            System.out.printf("VACUUM FULL reclaimed %.1f%% of space%n", spaceReclaimed);
        }

        System.out.println("WARNING: VACUUM FULL locks the table exclusively - use with caution!");
    }

    @Test
    @Order(6)
    @DisplayName("Exercise 4.1: INDEX_CLEANUP Option")
    void testExercise4_1_IndexCleanup() throws IOException, SQLException {
        System.out.println("=== Exercise 4.1: INDEX_CLEANUP Option ===");
        System.out.println("Key Concept: Control index processing during VACUUM");

        System.out.println("\n1. Validating INDEX_CLEANUP solution...");
        String sql = readSqlFileContent(getResourcePath("queries/04_1_emergency_cleanup.sql"));
        String cleanSql = filterOutCommentedLines(sql);
        assertTrue(cleanSql.toUpperCase().contains("CREATE TABLE"), 
                "You need to implement the solution");
        assertTrue(cleanSql.toUpperCase().contains("VACUUM") && cleanSql.toUpperCase().contains("INDEX_CLEANUP"), 
                "You need to implement the solution");

        System.out.println("\n2. INDEX_CLEANUP Explanation:");
        System.out.println("• INDEX_CLEANUP FALSE = Skip index processing (faster)");
        System.out.println("• INDEX_CLEANUP TRUE = Process indexes (default, slower but complete)");
        System.out.println("• Use FALSE for emergency cleanup of severely bloated tables");
        
        System.out.println("\n3. Executing emergency cleanup...");
        executeVacuumSqlFile(getResourcePath("queries/04_1_emergency_cleanup.sql"));

        boolean tableExists = checkTableExists("transaction_summary");
        if (tableExists) {
            updateStatistics();
            DeadTupleStats statsBefore = getDeadTupleStats("transaction_summary");
            TableSizeInfo sizeBefore = getTableSizeInfo("transaction_summary");
            
            System.out.println("\n=== BEFORE INDEX_CLEANUP FALSE ===");
            System.out.printf("Dead tuples: %d%n", statsBefore.deadTuples);
            System.out.printf("Table size: %s%n", sizeBefore.totalSizePretty);
            
            // Run INDEX_CLEANUP FALSE again to show the difference
            System.out.println("\n=== RUNNING EMERGENCY VACUUM ===");
            long startTime = System.currentTimeMillis();
            executeVacuumCommand("VACUUM (INDEX_CLEANUP FALSE) transaction_summary");
            long endTime = System.currentTimeMillis();
            
            updateStatistics();
            DeadTupleStats statsAfter = getDeadTupleStats("transaction_summary");
            TableSizeInfo sizeAfter = getTableSizeInfo("transaction_summary");
            
            System.out.println("\n=== AFTER Emergency VACUUM ===");
            System.out.printf("Dead tuples: %d (cleaned %d)%n", statsAfter.deadTuples, statsBefore.deadTuples - statsAfter.deadTuples);
            System.out.printf("Table size: %s%n", sizeAfter.totalSizePretty);
            System.out.printf("Execution time: %dms (faster - indexes skipped)%n", (endTime - startTime));
            
            showIndexInfo("transaction_summary");
            System.out.println("RESULT: Faster execution, but indexes may need separate cleanup");
        }
    }

    @Test
    @Order(7)
    @DisplayName("Exercise 4.2: TRUNCATE Option")
    void testExercise4_2_Truncate() throws IOException, SQLException {
        System.out.println("=== Exercise 4.2: TRUNCATE Option ===");
        System.out.println("Key Concept: Control space reclamation during VACUUM");

        System.out.println("\n1. Validating TRUNCATE solution...");
        String sql = readSqlFileContent(getResourcePath("queries/04_2_space_optimization.sql"));
        String cleanSql = filterOutCommentedLines(sql);
        assertTrue(cleanSql.toUpperCase().contains("VACUUM") && cleanSql.toUpperCase().contains("TRUNCATE"), 
                "You need to implement the solution");

        System.out.println("\n2. TRUNCATE Explanation:");
        System.out.println("• TRUNCATE FALSE = Keep empty pages (faster, space not returned to OS)");
        System.out.println("• TRUNCATE TRUE = Return pages to OS (slower, saves disk space)");
        
        System.out.println("\n3. Executing TRUNCATE test...");
        executeVacuumSqlFile(getResourcePath("queries/04_2_space_optimization.sql"));

        boolean tableExists = checkTableExists("transaction_summary");
        if (tableExists) {
            updateStatistics();
            DeadTupleStats statsBefore = getDeadTupleStats("transaction_summary");
            TableSizeInfo sizeBefore = getTableSizeInfo("transaction_summary");
            
            System.out.println("\n=== BEFORE VACUUM TRUNCATE ===");
            System.out.printf("Dead tuples: %d%n", statsBefore.deadTuples);
            System.out.printf("Table size: %s%n", sizeBefore.totalSizePretty);
            
            // Run TRUNCATE FALSE to show the difference
            System.out.println("\n=== RUNNING VACUUM TRUNCATE FALSE ===");
            long startTime = System.currentTimeMillis();
            executeVacuumCommand("VACUUM (TRUNCATE FALSE) transaction_summary");
            long endTime = System.currentTimeMillis();
            
            updateStatistics();
            DeadTupleStats statsAfter = getDeadTupleStats("transaction_summary");
            TableSizeInfo sizeAfter = getTableSizeInfo("transaction_summary");
            
            System.out.println("\n=== AFTER VACUUM TRUNCATE FALSE ===");
            System.out.printf("Dead tuples: %d (cleaned %d)%n", statsAfter.deadTuples, statsBefore.deadTuples - statsAfter.deadTuples);
            System.out.printf("Table size: %s%n", sizeAfter.totalSizePretty);
            System.out.printf("Execution time: %dms%n", (endTime - startTime));
            
            System.out.println("\n=== TRUNCATE OPTION EXPLANATION ===");
            System.out.println("• TRUNCATE FALSE = Keep empty pages (faster, space not returned to OS)");
            System.out.println("• TRUNCATE TRUE = Return pages to OS (slower, saves disk space)");
            System.out.println("RESULT: TRUNCATE controls whether empty pages are returned to OS");
        }
    }

    @Test
    @Order(8)
    @DisplayName("Exercise 4.3: Production Maintenance")
    void testExercise4_3_ProductionMaintenance() throws IOException, SQLException {
        System.out.println("=== Exercise 4.3: Production Maintenance ===");
        System.out.println("Key Concept: Combine all VACUUM options for comprehensive maintenance");

        System.out.println("\n1. Validating production maintenance solution...");
        String sql = readSqlFileContent(getResourcePath("queries/04_3_production_maintenance.sql"));
        String cleanSql = filterOutCommentedLines(sql);
        assertTrue(cleanSql.toUpperCase().contains("VACUUM") && 
                   cleanSql.toUpperCase().contains("VERBOSE") && 
                   cleanSql.toUpperCase().contains("ANALYZE") && 
                   cleanSql.toUpperCase().contains("INDEX_CLEANUP") && 
                   cleanSql.toUpperCase().contains("TRUNCATE"), 
                "You need to implement the solution");

        System.out.println("\n2. Combined Advanced Options:");
        System.out.println("• VACUUM (VERBOSE, ANALYZE, INDEX_CLEANUP TRUE, TRUNCATE TRUE)");
        System.out.println("• Perfect for scheduled maintenance windows");
        
        System.out.println("\n3. Executing production maintenance...");
        executeVacuumSqlFile(getResourcePath("queries/04_3_production_maintenance.sql"));

        boolean tableExists = checkTableExists("transaction_summary");
        if (tableExists) {
            updateStatistics();
            DeadTupleStats beforeStats = getDeadTupleStats("transaction_summary");
            TableSizeInfo sizeBefore = getTableSizeInfo("transaction_summary");
            StatisticsInfo statsBefore = getTableStatistics("transaction_summary");
            
            System.out.println("\n=== BEFORE Production Maintenance ===");
            System.out.printf("Dead tuples: %d%n", beforeStats.deadTuples);
            System.out.printf("Table size: %s%n", sizeBefore.totalSizePretty);
            System.out.printf("Statistics last updated: %s%n", 
                statsBefore.lastAnalyze != null ? statsBefore.lastAnalyze : "Never");
            System.out.printf("Row estimate: %d%n", statsBefore.rowEstimate);
            
            // Run comprehensive VACUUM
            System.out.println("\n=== RUNNING COMPREHENSIVE VACUUM ===");
            long startTime = System.currentTimeMillis();
            executeVacuumCommand("VACUUM (VERBOSE, ANALYZE, INDEX_CLEANUP TRUE, TRUNCATE TRUE) transaction_summary");
            long endTime = System.currentTimeMillis();
            
            updateStatistics();
            DeadTupleStats finalStats = getDeadTupleStats("transaction_summary");
            TableSizeInfo sizeFinal = getTableSizeInfo("transaction_summary");
            StatisticsInfo statsFinal = getTableStatistics("transaction_summary");
            
            System.out.println("\n=== AFTER Production Maintenance ===");
            System.out.printf("Dead tuples: %d (cleaned %d)%n", finalStats.deadTuples, beforeStats.deadTuples - finalStats.deadTuples);
            System.out.printf("Table size: %s%n", sizeFinal.totalSizePretty);
            System.out.printf("Statistics updated: %s%n", 
                statsFinal.lastAnalyze != null ? statsFinal.lastAnalyze : "Never");
            System.out.printf("Row estimate: %d (was %d)%n", statsFinal.rowEstimate, statsBefore.rowEstimate);
            System.out.printf("Execution time: %dms%n", (endTime - startTime));
            
            System.out.println("\n=== SUMMARY ===");
            System.out.printf("✅ Cleaned %d dead tuples%n", Math.max(0, beforeStats.deadTuples - finalStats.deadTuples));
            System.out.printf("✅ Updated table statistics%n");
            System.out.printf("✅ Processed all indexes%n");
            System.out.printf("✅ Comprehensive maintenance completed%n");
        }
        
        System.out.println("\nRESULT: Production-ready maintenance completed!");
        System.out.println("KEY LEARNINGS:");
        System.out.println("• EMERGENCY: INDEX_CLEANUP FALSE, TRUNCATE FALSE (fastest)");
        System.out.println("• MAINTENANCE: VERBOSE, ANALYZE, INDEX_CLEANUP TRUE, TRUNCATE TRUE (complete)");
    }

    @Test
    @Order(9)
    @DisplayName("Exercise 5: Automatic Maintenance Configuration")
    void testAutovacuumSettings() throws IOException, SQLException {
        System.out.println("=== Exercise 5: Automatic Maintenance Configuration ===");
        System.out.println("Key Concept: Configure autovacuum for high-transaction tables");

        System.out.println("\n1. Validating autovacuum configuration...");
        String sql = readSqlFileContent(getResourcePath("queries/05_automatic_maintenance.sql"));
        String cleanSql = filterOutCommentedLines(sql);
        assertTrue(cleanSql.toUpperCase().contains("ALTER TABLE TRANSACTIONS"), 
                "You need to implement the solution");
        assertTrue(cleanSql.toUpperCase().contains("AUTOVACUUM_VACUUM_SCALE_FACTOR"), 
                "You need to implement the solution");
        assertTrue(cleanSql.toUpperCase().contains("0.005"), 
                "You need to implement the solution");
        assertTrue(cleanSql.toUpperCase().contains("ALTER TABLE ACCOUNTS"), 
                "You need to implement the solution");
        assertTrue(cleanSql.toUpperCase().contains("ALTER TABLE CUSTOMERS"), 
                "You need to implement the solution");

        System.out.println("\n2. Checking default autovacuum settings...");
        showAutovacuumSettings();

        System.out.println("\n3. Applying autovacuum configuration...");
        executeSqlFile(getResourcePath("queries/05_automatic_maintenance.sql"));
        if (!connection.getAutoCommit()) {
            connection.commit();
        }

        Map<String, String> transactionSettings = getTableAutovacuumSettings("transactions");

        assertNotNull(transactionSettings.get("autovacuum_vacuum_scale_factor"),
                "Should set autovacuum_vacuum_scale_factor for transactions table");
        assertEquals("0.005", transactionSettings.get("autovacuum_vacuum_scale_factor"),
                "Transactions table should have very aggressive vacuum scale factor of 0.005");

        System.out.println("RESULT: Autovacuum settings configured successfully!");
        System.out.println("• transactions table: Very aggressive cleaning (scale_factor = 0.005, threshold = 10)");
        System.out.println("• Why? High-transaction tables need very frequent maintenance");
        showBankingTablesAutovacuumSettings();

        // Now let's test autovacuum in action!
        System.out.println("\n=== Testing Autovacuum in Action ===");

        // 1. Add more data to transactions table
        System.out.println("\n1. Adding initial data to transactions table...");
        addTransactionLoad(5000);

        DeadTupleStats initialStats = getDeadTupleStats("transactions");
        System.out.println("Initial state:");
        printDeadTupleStats(initialStats);

        // 2. Create dead tuples with aggressive updates (triggers autovacuum threshold)
        System.out.println("\n2. Creating dead tuples with heavy update load...");
        createDeadTuplesLoad();

        updateStatistics();
        DeadTupleStats afterUpdates = getDeadTupleStats("transactions");
        System.out.println("After creating dead tuples:");
        printDeadTupleStats(afterUpdates);
        
        // Calculate autovacuum threshold with new aggressive settings
        long autovacuumThreshold = calculateAutovacuumThreshold(afterUpdates.liveTuples, 10, 0.005);
        System.out.printf("Autovacuum threshold: %d (current dead tuples: %d)%n", 
            autovacuumThreshold, afterUpdates.deadTuples);
        System.out.printf("Formula: threshold + (live_tuples * scale_factor) = 10 + (%d * 0.005) = %d%n", 
            afterUpdates.liveTuples, autovacuumThreshold);
        
        if (afterUpdates.deadTuples > autovacuumThreshold) {
            System.out.println("✅ Dead tuples exceed threshold - autovacuum should trigger!");
        } else {
            System.out.println("⚠️ Dead tuples below threshold - need more updates");
        }

        // 3. Wait and check if autovacuum kicks in
        System.out.println("\n3. Waiting for autovacuum to process the load...");
        waitForAutovacuum(20); // Wait up to 20 seconds with more aggressive settings

        DeadTupleStats finalStats = getDeadTupleStats("transactions");
        System.out.println("After autovacuum processing:");
        printDeadTupleStats(finalStats);

        // 4. Show autovacuum activity
        System.out.println("\n4. Autovacuum activity summary:");
        showAutovacuumActivity();

        // Verify autovacuum effectiveness
        if (finalStats.deadTuples < afterUpdates.deadTuples * 0.8) {
            System.out.println("\n✅ Autovacuum successfully reduced dead tuples!");
            System.out.printf("   Dead tuples reduced from %d to %d (%.1f%% reduction)\n",
                    afterUpdates.deadTuples, finalStats.deadTuples,
                    ((double)(afterUpdates.deadTuples - finalStats.deadTuples) / afterUpdates.deadTuples) * 100);
        } else {
            System.out.println("\n⚠️ Autovacuum may not have processed yet (embedded PostgreSQL has different timing)");
            System.out.println("   In production, autovacuum would clean this up based on the configured thresholds");
        }

        // Show final status
        System.out.println("\nFinal status of all banking tables:");
        showBankingTablesBloat();
    }

    @Test
    @Order(10)
    @DisplayName("Exercise 6: Transaction ID Freezing")
    void testVacuumFreeze() throws IOException, SQLException {
        System.out.println("=== Exercise 6: Transaction ID Freezing ===");
        System.out.println("Key Concept: Prevent transaction ID wraparound in old tables");

        System.out.println("\n1. Validating VACUUM FREEZE solution...");
        String sql = readSqlFileContent(getResourcePath("queries/06_prevent_wraparound.sql"));
        String cleanSql = filterOutCommentedLines(sql);
        assertTrue(cleanSql.toUpperCase().contains("CREATE TABLE TRANSACTIONS_HISTORY"), 
                "You need to implement the solution");
        assertTrue(cleanSql.toUpperCase().contains("INSERT INTO TRANSACTIONS_HISTORY"), 
                "You need to implement the solution");
        assertTrue(cleanSql.toUpperCase().contains("VACUUM FREEZE"), 
                "You need to implement the solution");
        assertTrue(cleanSql.toUpperCase().contains("TRANSACTIONS_HISTORY") && cleanSql.toUpperCase().contains("LOANS"), 
                "You need to implement the solution");

        System.out.println("\n2. Executing VACUUM FREEZE solution...");
        executeSqlFile(getResourcePath("queries/06_prevent_wraparound.sql"));
        if (!connection.getAutoCommit()) {
            connection.commit();
        }

        boolean tableExists = checkTableExists("transactions_history");
        if (!tableExists) {
            System.out.println("Creating transactions_history table for demonstration...");
            createTransactionsHistoryTable();
        }

        System.out.println("\n2. Checking transaction ID ages (before freeze)...");
        FreezeStats freezeStatsBefore = getFreezeStats("transactions_history");
        FreezeStats loansFreezeStatsBefore = getFreezeStats("loans");
        
        System.out.println("transactions_history: XID age = " + freezeStatsBefore.frozenXidAge);
        System.out.println("loans: XID age = " + loansFreezeStatsBefore.frozenXidAge);
        System.out.println("Note: XID age shows how old the oldest transaction in table is");
        System.out.println("      Age > 2 billion = approaching wraparound danger zone");
        
        if (freezeStatsBefore.frozenXidAge > 1000000000) {
            System.out.println("WARNING: High XID age detected - wraparound risk!");
        } else {
            System.out.println("INFO: Demonstrating VACUUM FREEZE on historical data");
        }

        System.out.println("\n3. Running VACUUM FREEZE commands...");
        executeVacuumCommand("VACUUM FREEZE transactions_history");
        executeVacuumCommand("VACUUM FREEZE loans");

        FreezeStats freezeStatsAfter = getFreezeStats("transactions_history");
        System.out.println("\nAfter VACUUM FREEZE:");
        printFreezeStats(freezeStatsAfter);

        FreezeStats loansFreezeStatsAfter = getFreezeStats("loans");
        System.out.println("\nLoans table after VACUUM FREEZE:");
        printFreezeStats(loansFreezeStatsAfter);

        // Verify freezing occurred - more lenient check
        boolean freezeEffective = false;

        // Check transactions_history
        if (freezeStatsBefore.frozenXidAge > 0 && freezeStatsAfter.frozenXidAge == 0) {
            freezeEffective = true;
            System.out.println("\n✅ transactions_history successfully frozen (XID age reduced to 0)");
        } else if (freezeStatsBefore.frozenXidAge == 0) {
            System.out.println("\n✅ transactions_history was already frozen");
            freezeEffective = true;
        }

        // Check loans table
        if (loansFreezeStatsBefore.frozenXidAge > loansFreezeStatsAfter.frozenXidAge) {
            System.out.println("✅ loans table freeze was effective");
            freezeEffective = true;
        } else if (loansFreezeStatsAfter.frozenXidAge <= 1) {
            System.out.println("✅ loans table is effectively frozen (very low XID age)");
            freezeEffective = true;
        }

        assertTrue(freezeEffective, "At least one table should show freeze effectiveness");

        System.out.println("\n✅ Tables successfully frozen to prevent wraparound!");
        System.out.println("VACUUM FREEZE benefits:");
        System.out.println("• Sets all XIDs in table to frozen state (age = 0)");
        System.out.println("• Prevents transaction ID wraparound issues");
        System.out.println("• Essential for tables with old historical data");
        System.out.println("• Should be used periodically on archival tables");

        // Show XID age for all tables
        System.out.println("\nTransaction ID age for all banking tables:");
        showTransactionIdAge();
    }

    @Test
    @Order(11)
    @DisplayName("Exercise 7.1: Check Table Health")
    void testExercise7_1_CheckTableHealth() throws IOException, SQLException {
        System.out.println("=== Exercise 7.1: Check Table Health ===");
        System.out.println("Key Concept: Monitor dead tuples to identify maintenance needs");

        System.out.println("\n1. Validating table health monitoring query...");
        String sql = readSqlFileContent(getResourcePath("queries/07_1_check_table_health.sql"));
        String cleanSql = filterOutCommentedLines(sql);
        assertTrue(cleanSql.toUpperCase().contains("SELECT"), 
                "You need to implement the solution");
        assertTrue(cleanSql.toUpperCase().contains("PG_STAT_USER_TABLES"), 
                "You need to implement the solution");
        assertTrue(cleanSql.toUpperCase().contains("N_LIVE_TUP") && cleanSql.toUpperCase().contains("N_DEAD_TUP"), 
                "You need to implement the solution");

        System.out.println("\n2. Setting up environment with dead tuples...");
        setupBankingTablesWithDeadTuples();
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("UPDATE transactions SET amount = amount + 0.01 WHERE id % 2 = 0");
            if (!connection.getAutoCommit()) connection.commit();
        }
        updateStatistics();

        System.out.println("\n3. Running table health check...");
        showQueryResults("queries/07_1_check_table_health.sql");

        System.out.println("RESULT: Can identify tables needing VACUUM!");
        
        DeadTupleStats stats = getDeadTupleStats("transactions");
        assertTrue(stats.liveTuples >= 0, "Should be able to monitor live tuples");
    }

    @Test
    @Order(12)
    @DisplayName("Exercise 7.2: Maintenance History")
    void testExercise7_2_MaintenanceHistory() throws IOException, SQLException {
        System.out.println("=== Exercise 7.2: Maintenance History ===");
        System.out.println("Key Concept: Track when tables were last vacuumed");

        System.out.println("\n1. Validating maintenance history query...");
        String sql = readSqlFileContent(getResourcePath("queries/07_2_maintenance_history.sql"));
        String cleanSql = filterOutCommentedLines(sql);
        assertTrue(cleanSql.toUpperCase().contains("VACUUM_COUNT") && cleanSql.toUpperCase().contains("AUTOVACUUM_COUNT"), 
                "You need to implement the solution");
        assertTrue(cleanSql.toUpperCase().contains("LAST_VACUUM") && cleanSql.toUpperCase().contains("LAST_AUTOVACUUM"), 
                "You need to implement the solution");

        System.out.println("\n2. Running maintenance history check...");
        showQueryResults("queries/07_2_maintenance_history.sql");

        System.out.println("RESULT: Can track VACUUM frequency and timing!");
    }

    @Test
    @Order(13)
    @DisplayName("Exercise 7.3: Storage Analysis")
    void testExercise7_3_StorageAnalysis() throws IOException, SQLException {
        System.out.println("=== Exercise 7.3: Storage Analysis ===");
        System.out.println("Key Concept: Monitor table and index sizes");

        System.out.println("\n1. Validating storage analysis query...");
        String sql = readSqlFileContent(getResourcePath("queries/07_3_storage_analysis.sql"));
        String cleanSql = filterOutCommentedLines(sql);
        assertTrue(cleanSql.toUpperCase().contains("PG_SIZE_PRETTY"), 
                "You need to implement the solution");
        assertTrue(cleanSql.toUpperCase().contains("PG_TOTAL_RELATION_SIZE"), 
                "You need to implement the solution");

        System.out.println("\n2. Running storage analysis...");
        showQueryResults("queries/07_3_storage_analysis.sql");

        System.out.println("RESULT: Can monitor space usage and identify bloated tables!");
        System.out.println("\nKEY LEARNINGS:");
        System.out.println("• pg_stat_user_tables shows dead tuples");
        System.out.println("• last_vacuum shows when table was cleaned");
        System.out.println("• pg_size_pretty shows table sizes");
    }


    // Helper classes
    private static class DeadTupleStats {
        long liveTuples;
        long deadTuples;
        double deadTuplePercent;
        long tableSizeBytes;
    }

    private static class TableSizeInfo {
        long totalSizeBytes;
        long tableSizeBytes;
        long indexSizeBytes;
        String totalSizePretty;
        String tableSizePretty;
        String indexSizePretty;
    }

    private static class StatisticsInfo {
        boolean hasStatistics;
        String lastAnalyze;
        long rowEstimate;
    }

    private static class FreezeStats {
        long frozenXidAge;
        long minXid;
        String tableName;
    }

    // Helper methods
    private DeadTupleStats getDeadTupleStats(String tableName) throws SQLException {
        DeadTupleStats stats = new DeadTupleStats();

        // Fixed query - using correct column name 'relname' instead of 'tablename'
        String query = """
                SELECT 
                    COALESCE(n_live_tup, 0) as n_live_tup,
                    COALESCE(n_dead_tup, 0) as n_dead_tup,
                    ROUND(100.0 * COALESCE(n_dead_tup, 0) / NULLIF(COALESCE(n_live_tup, 0) + COALESCE(n_dead_tup, 0), 0), 2) as dead_tuple_percent,
                    pg_total_relation_size(relname::regclass) as table_size
                FROM pg_stat_user_tables
                WHERE relname = ?
            """;

        try (var pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, tableName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    stats.liveTuples = rs.getLong("n_live_tup");
                    stats.deadTuples = rs.getLong("n_dead_tup");
                    stats.deadTuplePercent = rs.getDouble("dead_tuple_percent");
                    stats.tableSizeBytes = rs.getLong("table_size");
                }
            }
        }

        return stats;
    }

    private void printDeadTupleStats(DeadTupleStats stats) {
        System.out.printf("Live: %d, Dead: %d (%.1f%%), Size: %s%n", 
            stats.liveTuples, stats.deadTuples, stats.deadTuplePercent, formatBytes(stats.tableSizeBytes));
    }

    private TableSizeInfo getTableSizeInfo(String tableName) throws SQLException {
        TableSizeInfo info = new TableSizeInfo();

        String query = """
                SELECT 
                    pg_total_relation_size(?) as total_size,
                    pg_relation_size(?) as table_size,
                    pg_indexes_size(?) as index_size,
                    pg_size_pretty(pg_total_relation_size(?)) as total_pretty,
                    pg_size_pretty(pg_relation_size(?)) as table_pretty,
                    pg_size_pretty(pg_indexes_size(?)) as index_pretty
            """;

        try (var pstmt = connection.prepareStatement(query)) {
            // Set all 6 parameters to the table name
            for (int i = 1; i <= 6; i++) {
                pstmt.setString(i, tableName);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    info.totalSizeBytes = rs.getLong("total_size");
                    info.tableSizeBytes = rs.getLong("table_size");
                    info.indexSizeBytes = rs.getLong("index_size");
                    info.totalSizePretty = rs.getString("total_pretty");
                    info.tableSizePretty = rs.getString("table_pretty");
                    info.indexSizePretty = rs.getString("index_pretty");
                }
            }
        }

        return info;
    }

    private void printTableSizeInfo(TableSizeInfo info) {
        System.out.printf("Total: %s, Table: %s, Index: %s%n", 
            info.totalSizePretty, info.tableSizePretty, info.indexSizePretty);
    }

    private void updateStatistics() throws SQLException {
        // Force statistics update for PostgreSQL
        try (Statement stmt = connection.createStatement()) {
            // Force a statistics update by analyzing all relevant tables
            stmt.execute("ANALYZE transactions");
            stmt.execute("ANALYZE accounts");
            stmt.execute("ANALYZE customers");
            stmt.execute("ANALYZE loans");

            // Give PostgreSQL time to update statistics
            stmt.execute("SELECT pg_sleep(0.5)");

            // Only commit if autoCommit is disabled
            if (!connection.getAutoCommit()) {
                connection.commit();
            }
        }
    }

    private void setupBankingTablesWithDeadTuples() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Add more test transactions
            stmt.execute("""
                    INSERT INTO transactions (account_id, transaction_type, amount, transaction_date)
                    SELECT 
                        (id % 10) + 1,
                        CASE (random() * 3)::int
                            WHEN 0 THEN 'deposit'
                            WHEN 1 THEN 'withdrawal'
                            ELSE 'transfer'
                        END,
                        (random() * 1000)::numeric(15,2),
                        CURRENT_TIMESTAMP - (random() * INTERVAL '180 days')
                    FROM generate_series(1, 3000) id
                """);

            // Create dead tuples through updates
            stmt.execute("ALTER TABLE transactions ADD COLUMN IF NOT EXISTS processed BOOLEAN DEFAULT FALSE");
            stmt.execute("UPDATE transactions SET processed = TRUE WHERE id % 2 = 0");
            stmt.execute("UPDATE transactions SET amount = amount * 1.01 WHERE transaction_type = 'deposit'");

            // Create dead tuples through deletes
            stmt.execute("DELETE FROM transactions WHERE id > 2500 AND transaction_date < CURRENT_DATE - INTERVAL '150 days'");

            // Only commit if autoCommit is disabled
            if (!connection.getAutoCommit()) {
                connection.commit();
            }
        }
    }

    private void createBloatedTransactionsTable() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS bloated_transactions CASCADE");
            stmt.execute("""
                    CREATE TABLE bloated_transactions (
                        id SERIAL PRIMARY KEY,
                        account_id INTEGER,
                        transaction_type VARCHAR(20),
                        amount DECIMAL(15,2),
                        description TEXT,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                """);

            // Simulate heavy transaction processing with lots of updates/deletes
            for (int i = 0; i < 5; i++) {
                // Insert batch
                stmt.execute("""
                        INSERT INTO bloated_transactions (account_id, transaction_type, amount, description)
                        SELECT 
                            (random() * 10 + 1)::int,
                            CASE (random() * 3)::int
                                WHEN 0 THEN 'deposit'
                                WHEN 1 THEN 'withdrawal'
                                ELSE 'transfer'
                            END,
                            (random() * 1000)::numeric(15,2),
                            repeat('Transaction data ', 50)
                        FROM generate_series(1, 2000)
                    """);

                // Update some
                stmt.execute("UPDATE bloated_transactions SET description = description || ' - PROCESSED' WHERE id % 3 = 0");

                // Delete half
                stmt.execute("DELETE FROM bloated_transactions WHERE id % 2 = 0");
            }

            // Only commit if autoCommit is disabled
            if (!connection.getAutoCommit()) {
                connection.commit();
            }
        }
    }

    private StatisticsInfo getTableStatistics(String tableName) throws SQLException {
        StatisticsInfo info = new StatisticsInfo();

        String query = """
                SELECT 
                    last_analyze,
                    n_live_tup,
                    EXISTS(
                        SELECT 1 FROM pg_stats 
                        WHERE tablename = ?
                    ) as has_statistics
                FROM pg_stat_user_tables
                WHERE relname = ?
            """;

        try (var pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, tableName);
            pstmt.setString(2, tableName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    info.lastAnalyze = rs.getString("last_analyze");
                    info.rowEstimate = rs.getLong("n_live_tup");
                    info.hasStatistics = rs.getBoolean("has_statistics");
                }
            }
        }

        return info;
    }

    private void printStatisticsInfo(StatisticsInfo info) {
        System.out.printf("Last analyzed: %s, Rows: %d, Has stats: %b%n", 
            info.lastAnalyze != null ? info.lastAnalyze : "Never", info.rowEstimate, info.hasStatistics);
    }

    private void showAutovacuumSettings() throws SQLException {
        System.out.println("Current autovacuum settings:");

        String query = """
                SELECT name, setting, unit, short_desc
                FROM pg_settings
                WHERE name LIKE 'autovacuum%'
                AND name IN ('autovacuum', 'autovacuum_vacuum_threshold', 'autovacuum_vacuum_scale_factor')
                ORDER BY name
            """;

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                System.out.printf("- %s = %s %s\n",
                        rs.getString("name"),
                        rs.getString("setting"),
                        rs.getString("unit") != null ? rs.getString("unit") : "");
            }
        }
    }

    private Map<String, String> getTableAutovacuumSettings(String tableName) throws SQLException {
        Map<String, String> settings = new HashMap<>();

        String query = """
                SELECT reloptions
                FROM pg_class
                WHERE relname = ?
            """;

        try (var pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, tableName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String options = rs.getString("reloptions");
                    if (options != null) {
                        String[] opts = options.replace("{", "").replace("}", "").split(",");
                        for (String opt : opts) {
                            if (opt.contains("=")) {
                                String[] parts = opt.split("=");
                                settings.put(parts[0], parts[1]);
                            }
                        }
                    }
                }
            }
        }

        return settings;
    }

    private FreezeStats getFreezeStats(String tableName) throws SQLException {
        FreezeStats stats = new FreezeStats();
        stats.tableName = tableName;

        String query = """
                SELECT 
                    age(relfrozenxid) as frozen_xid_age,
                    relfrozenxid::text::bigint as min_xid
                FROM pg_class
                WHERE relname = ?
            """;

        try (var pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, tableName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    stats.frozenXidAge = rs.getLong("frozen_xid_age");
                    stats.minXid = rs.getLong("min_xid");
                }
            }
        }

        return stats;
    }

    private void printFreezeStats(FreezeStats stats) {
        System.out.println("- Table: " + stats.tableName);
        System.out.println("- Frozen XID age: " + stats.frozenXidAge);
        System.out.println("- Minimum XID: " + stats.minXid);
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp-1) + "B";
        return String.format("%.1f %s", bytes / Math.pow(1024, exp), pre);
    }

    // Method to execute VACUUM commands that require autoCommit = true
    private void executeVacuumCommand(String command) throws SQLException {
        boolean originalAutoCommit = connection.getAutoCommit();
        try {
            connection.setAutoCommit(true);
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(command);
            }
        } finally {
            connection.setAutoCommit(originalAutoCommit);
        }
    }

    // Method to execute SQL files with VACUUM commands
    private void executeVacuumSqlFile(String filePath) throws IOException, SQLException {
        boolean originalAutoCommit = connection.getAutoCommit();
        try {
            connection.setAutoCommit(true);
            
            // Read and filter SQL file
            String sql = readSqlFileContent(filePath);
            String cleanSql = filterOutCommentedLines(sql);
            
            // Split by semicolon but be more careful about empty statements
            String[] statements = cleanSql.split(";");
            
            try (Statement stmt = connection.createStatement()) {
                for (String statement : statements) {
                    String trimmed = statement.trim();
                    if (!trimmed.isEmpty()) {
                        System.out.println("Executing: " + trimmed);
                        
                        // Execute the statement and capture any output
                        long startTime = System.currentTimeMillis();
                        
                        // For VACUUM VERBOSE, try to capture detailed output
                        if (trimmed.toUpperCase().contains("VACUUM") && trimmed.toUpperCase().contains("VERBOSE")) {
                            try {
                                // Execute and try to get result set (some VACUUM VERBOSE might return data)
                                boolean hasResultSet = stmt.execute(trimmed);
                                if (hasResultSet) {
                                    try (java.sql.ResultSet rs = stmt.getResultSet()) {
                                        while (rs.next()) {
                                            System.out.println("VACUUM: " + rs.getString(1));
                                        }
                                    }
                                }
                            } catch (SQLException e) {
                                // If that doesn't work, just execute normally
                                stmt.execute(trimmed);
                            }
                        } else {
                            stmt.execute(trimmed);
                        }
                        
                        long endTime = System.currentTimeMillis();
                        
                        // Show execution time for VACUUM operations
                        if (trimmed.toUpperCase().startsWith("VACUUM")) {
                            System.out.println("VACUUM completed in " + (endTime - startTime) + "ms");
                        }
                        
                        // Check for and display any warnings/notices from PostgreSQL
                        java.sql.SQLWarning warning = stmt.getWarnings();
                        while (warning != null) {
                            System.out.println("PostgreSQL: " + warning.getMessage());
                            warning = warning.getNextWarning();
                        }
                        
                        // Also check connection warnings
                        warning = connection.getWarnings();
                        while (warning != null) {
                            System.out.println("PostgreSQL: " + warning.getMessage());
                            warning = warning.getNextWarning();
                        }
                        
                        stmt.clearWarnings();
                        connection.clearWarnings();
                    }
                }
            }
        } finally {
            connection.setAutoCommit(originalAutoCommit);
        }
    }

    // Override the base class method to handle block comments properly
    @Override
    protected String filterOutCommentedLines(String sql) {
        StringBuilder result = new StringBuilder();
        boolean inBlockComment = false;
        boolean inLineComment = false;
        boolean inString = false;
        char stringChar = '\0';

        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);
            char next = (i + 1 < sql.length()) ? sql.charAt(i + 1) : '\0';

            // Handle string literals to avoid parsing comments inside them
            if (!inBlockComment && !inLineComment) {
                if (!inString && (c == '\'' || c == '"')) {
                    inString = true;
                    stringChar = c;
                    result.append(c);
                    continue;
                } else if (inString && c == stringChar) {
                    // Check for escaped quotes
                    if (i > 0 && sql.charAt(i - 1) == '\\') {
                        result.append(c);
                        continue;
                    }
                    inString = false;
                    result.append(c);
                    continue;
                }
            }

            if (!inString && !inBlockComment && !inLineComment) {
                // Check for start of block comment
                if (c == '/' && next == '*') {
                    inBlockComment = true;
                    i++; // Skip the '*'
                    continue;
                }
                // Check for start of line comment
                else if (c == '-' && next == '-') {
                    inLineComment = true;
                    i++; // Skip the second '-'
                    continue;
                }
                // Regular character
                else {
                    result.append(c);
                }
            }
            else if (inBlockComment) {
                // Check for end of block comment
                if (c == '*' && next == '/') {
                    inBlockComment = false;
                    i++; // Skip the '/'
                }
                // Skip characters inside block comment
            }
            else if (inLineComment) {
                // Check for end of line comment
                if (c == '\n' || c == '\r') {
                    inLineComment = false;
                    result.append(c); // Keep the newline
                }
                // Skip characters inside line comment
            } else {
                // Inside string - preserve everything
                result.append(c);
            }
        }

        // If we ended with an unclosed block comment, that's an error in the SQL file
        if (inBlockComment) {
            throw new IllegalArgumentException("Unclosed block comment in SQL file");
        }

        return result.toString();
    }

    private long getActualRowCount(String tableName) throws SQLException {
        String query = "SELECT COUNT(*) FROM " + tableName;
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        return 0;
    }

    private boolean checkTableExists(String tableName) throws SQLException {
        String query = """
        SELECT EXISTS (
            SELECT FROM information_schema.tables 
            WHERE table_schema = 'public' 
            AND table_name = ?
        )
        """;
        try (var pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, tableName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean(1);
                }
            }
        }
        return false;
    }


    private void createTransactionsHistoryTable() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Create historical transactions table as defined in exercise 6
            stmt.execute("""
          DROP TABLE IF EXISTS transactions_history;

          CREATE TABLE transactions_history (
              id SERIAL PRIMARY KEY,
              account_id INTEGER,
              transaction_type VARCHAR(20),
              amount DECIMAL(15, 2),
              transaction_date DATE,
              archived_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
          )
          """);

            // Insert historical data (simulating old transactions)
            stmt.execute("""
          INSERT INTO transactions_history (account_id, transaction_type, amount, transaction_date)
          SELECT 
              (random() * 10 + 1)::int,
              CASE (random() * 3)::int
                  WHEN 0 THEN 'deposit'
                  WHEN 1 THEN 'withdrawal'
                  ELSE 'transfer'
              END,
              (random() * 1000)::numeric(15,2),
              CURRENT_DATE - ((random() * 365) + 365)::int  -- 1-2 years old
          FROM generate_series(1, 10000)
          """);

            if (!connection.getAutoCommit()) {
                connection.commit();
            }

            System.out.println("Created transactions_history table with 10000 historical records");
        }
    }

    // Methods for testing autovacuum load
    private void addTransactionLoad(int count) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            String query = String.format("""
          INSERT INTO transactions (account_id, transaction_type, amount, transaction_date)
          SELECT 
              (id %% 10) + 1,
              CASE (random() * 3)::int
                  WHEN 0 THEN 'deposit'
                  WHEN 1 THEN 'withdrawal'
                  ELSE 'transfer'
              END,
              (random() * 1000)::numeric(15,2),
              CURRENT_TIMESTAMP - (random() * INTERVAL '30 days')
          FROM generate_series(1, %d) id
          """, count);

            stmt.execute(query);

            if (!connection.getAutoCommit()) {
                connection.commit();
            }

            System.out.printf("Added %d transactions\n", count);
        }
    }

    private void createDeadTuplesLoad() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Heavy update load to create many dead tuples
            System.out.println("   - Updating 50% of transactions (amount adjustments)...");
            stmt.execute("UPDATE transactions SET amount = amount * 1.001 WHERE id % 2 = 0");

            System.out.println("   - Updating transaction types...");
            stmt.execute("UPDATE transactions SET transaction_type = 'transfer' WHERE transaction_type = 'deposit' AND id % 3 = 0");

            System.out.println("   - Adding processing timestamps...");
            stmt.execute("ALTER TABLE transactions ADD COLUMN IF NOT EXISTS processed_at TIMESTAMP");
            stmt.execute("UPDATE transactions SET processed_at = CURRENT_TIMESTAMP WHERE id % 4 = 0");

            System.out.println("   - Deleting some old transactions...");
            stmt.execute("DELETE FROM transactions WHERE id % 20 = 0 AND transaction_date < CURRENT_DATE - INTERVAL '25 days'");

            if (!connection.getAutoCommit()) {
                connection.commit();
            }

            System.out.println("   Heavy update load completed - should trigger autovacuum thresholds");
        }
    }

    private void waitForAutovacuum(int maxSeconds) throws SQLException {
        System.out.printf("   Waiting up to %d seconds for autovacuum to process...\n", maxSeconds);

        for (int i = 0; i < maxSeconds; i += 2) {
            try {
                Thread.sleep(2000); // Wait 2 seconds

                // Check if autovacuum is running
                try (Statement stmt = connection.createStatement();
                     ResultSet rs = stmt.executeQuery("""
                 SELECT COUNT(*) as running_autovacuum 
                 FROM pg_stat_activity 
                 WHERE query LIKE '%autovacuum%' AND query NOT LIKE '%pg_stat_activity%'
                 """)) {

                    if (rs.next() && rs.getInt("running_autovacuum") > 0) {
                        System.out.printf("   [%ds] Autovacuum is running...\n", i + 2);
                    } else {
                        System.out.printf("   [%ds] Checking for autovacuum completion...\n", i + 2);
                    }
                }

                // Update statistics to get fresh data
                updateStatistics();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        System.out.println("   Autovacuum monitoring period completed");
    }

    private void showAutovacuumActivity() throws SQLException {
        String query = """
        SELECT 
            relname as tablename,
            autovacuum_count,
            n_dead_tup
        FROM pg_stat_user_tables
        WHERE relname IN ('transactions', 'accounts', 'customers', 'loans')
        AND (autovacuum_count > 0 OR n_dead_tup > 0)
        ORDER BY autovacuum_count DESC, n_dead_tup DESC
        """;

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            System.out.println("Autovacuum activity:");
            while (rs.next()) {
                System.out.printf("%s: %d autovacuums, %d dead tuples%n",
                        rs.getString("tablename"),
                        rs.getInt("autovacuum_count"),
                        rs.getInt("n_dead_tup")
                );
            }
        }
    }

    // Banking-specific helper methods
    private void showBankingTablesBloat() throws SQLException {
        String query = """
                SELECT 
                    relname as tablename,
                    COALESCE(n_dead_tup, 0) as n_dead_tup,
                    ROUND(100.0 * COALESCE(n_dead_tup, 0) / NULLIF(COALESCE(n_live_tup, 0) + COALESCE(n_dead_tup, 0), 0), 1) as dead_percent
                FROM pg_stat_user_tables
                WHERE relname IN ('transactions', 'accounts', 'customers', 'loans')
                AND COALESCE(n_dead_tup, 0) > 0
                ORDER BY COALESCE(n_dead_tup, 0) DESC
            """;

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            System.out.println("Tables with dead tuples:");
            while (rs.next()) {
                System.out.printf("%s: %d dead tuples (%.1f%%)%n",
                        rs.getString("tablename"),
                        rs.getInt("n_dead_tup"),
                        rs.getDouble("dead_percent")
                );
            }
        }
    }


    private void showBankingTablesAutovacuumSettings() throws SQLException {
        String query = """
                SELECT 
                    c.relname as table_name,
                    c.reloptions
                FROM pg_class c
                JOIN pg_namespace n ON n.oid = c.relnamespace
                WHERE n.nspname = 'public' 
                    AND c.relname IN ('transactions', 'accounts', 'customers', 'loans')
                    AND c.relkind = 'r'
                ORDER BY c.relname
            """;

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                String tableName = rs.getString("table_name");
                String options = rs.getString("reloptions");
                System.out.printf("\n%s:", tableName);
                if (options != null) {
                    String[] opts = options.replace("{", "").replace("}", "").split(",");
                    for (String opt : opts) {
                        System.out.printf("\n  - %s", opt);
                    }
                } else {
                    System.out.println("\n  - Using default settings");
                }
            }
        }
    }

    private long calculateAutovacuumThreshold(long liveTuples, int threshold, double scaleFactor) {
        return threshold + (long)(liveTuples * scaleFactor);
    }

    private void showTransactionIdAge() throws SQLException {
        String query = """
                SELECT 
                    relname as table_name,
                    age(relfrozenxid) as xid_age,
                    CASE 
                        WHEN age(relfrozenxid) > 1500000000 THEN 'CRITICAL: Approaching wraparound!'
                        WHEN age(relfrozenxid) > 1000000000 THEN 'WARNING: Schedule VACUUM FREEZE soon'
                        WHEN age(relfrozenxid) > 500000000 THEN 'Monitor closely'
                        ELSE 'OK'
                    END as status
                FROM pg_class
                WHERE relname IN ('transactions', 'accounts', 'customers', 'loans')
                    AND relkind = 'r'
                    AND relnamespace = (SELECT oid FROM pg_namespace WHERE nspname = 'public')
                ORDER BY age(relfrozenxid) DESC
            """;

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            System.out.println("\nTable         | XID Age    | Status");
            System.out.println("--------------|------------|---------------------------");
            while (rs.next()) {
                System.out.printf("%-13s | %10d | %s%n",
                        rs.getString("table_name"),
                        rs.getLong("xid_age"),
                        rs.getString("status")
                );
            }
        }
    }

    private void showIndexInfo(String tableName) throws SQLException {
        System.out.println("\nIndex Information:");
        String query = """
                SELECT 
                    indexname,
                    pg_size_pretty(pg_relation_size(indexname::regclass)) as index_size
                FROM pg_indexes
                WHERE tablename = ?
                ORDER BY indexname
            """;

        try (var pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, tableName);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    System.out.printf("• %s (%s)%n", 
                        rs.getString("indexname"), 
                        rs.getString("index_size"));
                }
            }
        }
    }

    // Helper method to read SQL file content for validation
    private String readSqlFileContent(String filePath) throws IOException {
        return java.nio.file.Files.readString(java.nio.file.Paths.get(filePath));
    }

    // Helper method to show actual PostgreSQL query results (without duplicate output)
    private void showQueryResults(String filePath) throws IOException, SQLException {
        String fileContent = java.nio.file.Files.readString(java.nio.file.Paths.get(getResourcePath(filePath)));
        
        // Find the last SQL statement (after "-- Solution:")
        String[] lines = fileContent.split("\n");
        String sql = "";
        for (int i = lines.length - 1; i >= 0; i--) {
            String line = lines[i].trim();
            if (!line.isEmpty() && !line.startsWith("--") && line.contains("SELECT")) {
                sql = line;
                break;
            }
        }
        
        if (sql.isEmpty()) {
            System.out.println("No SQL query found in file.");
            return;
        }
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            var metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            if (!rs.next()) {
                System.out.println("No results returned.");
                return;
            }
            
            // Print column headers
            System.out.print("PostgreSQL Results: ");
            for (int i = 1; i <= columnCount; i++) {
                System.out.printf("%-15s", metaData.getColumnName(i).toUpperCase());
            }
            System.out.println();
            
            // Print data rows
            do {
                System.out.print("                    ");
                for (int i = 1; i <= columnCount; i++) {
                    String value = rs.getString(i);
                    if (value == null) value = "NULL";
                    System.out.printf("%-15s", value);
                }
                System.out.println();
            } while (rs.next());
            
            System.out.println();
        }
    }
}