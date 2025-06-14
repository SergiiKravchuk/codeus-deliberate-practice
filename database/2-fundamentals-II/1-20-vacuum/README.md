# PostgreSQL VACUUM Exercise

This project is a hands-on exercise to learn and apply PostgreSQL VACUUM operations through practical examples.
You will work through different VACUUM scenarios, from basic dead tuple cleanup to advanced maintenance operations,
while observing the impact on database performance, storage efficiency, and maintenance scheduling.

## Overview

The exercise focuses on a financial transaction system database with tables containing transaction records, loans,
and user data. Through a series of tasks, you will:

1. Perform basic VACUUM operations to clean up dead tuples
2. Use VACUUM with various options (VERBOSE, ANALYZE, FULL, FREEZE)
3. Configure autovacuum settings for optimal performance
4. Monitor vacuum operations and table health
5. Understand when and how to use different VACUUM variants

### Running the Tests

Tests: [SqlQueriesTest](src/test/java/org/codeus/fundamentals/vaccum/SqlQueriesTest.java)

### Learning Resources

Presentation: [sql-vacuum.pdf](sql-vacuum.pdf) - Contains detailed explanations of VACUUM concepts and best practices

## Tasks Overview

<details>
<summary><b>Basic VACUUM Operations</b></summary>

1. **Exercise 1**: Basic dead tuple cleanup
   - Learn fundamental VACUUM syntax
   - Clean up dead tuples from UPDATE/DELETE operations

2. **Exercise 2**: VACUUM with advanced options
   - **2.1 Diagnostic Cleanup**: Get detailed operation output
   - **2.2 Statistics Update**: Combine cleanup with table statistics refresh  
   - **2.3 Comprehensive Cleanup**: Apply multiple options for complete maintenance

</details>

<details>
<summary><b>Advanced VACUUM Operations</b></summary>

3. **Exercise 3**: Complete table rewrite (VACUUM FULL)
   - Reclaim all disk space from heavily bloated tables
   - Understand when VACUUM FULL is necessary

4. **Exercise 4**: Production maintenance scenarios
   - Emergency cleanup procedures
   - Space optimization strategies
   - Scheduled maintenance operations

</details>

<details>
<summary><b>Autovacuum Configuration</b></summary>

5. **Exercise 5**: Configure automatic maintenance
   - Set aggressive autovacuum for transactions (0.5% scale factor, threshold 10)
   - Configure moderate settings for accounts (10% scale factor)
   - Apply relaxed settings for customers (20% scale factor, threshold 25)
   - Balance performance vs. maintenance overhead for different table types

6. **Exercise 6**: Prevent transaction wraparound
   - Use VACUUM FREEZE for transaction ID management
   - Understand XID wraparound prevention

</details>

<details>
<summary><b>Monitoring and Analysis</b></summary>

7. **Exercise 7**: Monitor vacuum operations
   - Check table health and bloat statistics
   - Review maintenance history
   - Analyze storage efficiency

</details>

## Key Takeaways

<details>
<summary><b>As you complete each task, you'll learn:</b></summary>

1. **VACUUM Types**:
   - Basic VACUUM removes dead tuples but doesn't shrink files
   - VACUUM FULL rewrites entire table, reclaiming all space
   - VACUUM FREEZE prevents transaction ID wraparound

2. **Performance Impact**:
   - Regular VACUUM maintains performance by cleaning dead tuples
   - VACUUM FULL is expensive but reclaims maximum space
   - Autovacuum provides automated maintenance scheduling
   - Aggressive autovacuum settings (low scale factors) ensure frequent cleanup
   - Different tables need different maintenance strategies based on activity

3. **Monitoring**:
   - pg_stat_user_tables shows vacuum statistics
   - Table bloat can be monitored and addressed
   - Proper maintenance prevents performance degradation

</details>

Good luck with your VACUUM journey! Remember that proper vacuum maintenance is essential for PostgreSQL performance
and prevents common issues like table bloat and transaction ID wraparound.