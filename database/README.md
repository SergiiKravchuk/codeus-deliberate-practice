## SQL Database Module 

This package contains exercises that cover SQL queries, transactions, indexes views, build-in functions and database normalization. Also. there is an exercise test to evaluate your knowledge. <br> 
Most exercises use PostgreSQL as a target database.

Modules:
- [1 Fundamentals](1-fundamentals)
- 2-advanced

Each module has multiple submodules that are listed in groups below.

## 1 Fundamentals
**!Note**: all hyperlinks below are for use in the IDE.
<details> 
  <summary>1.1 Data querying</summary>

👉 **Implement Data Querying exercises of the module**

1. Check the session's [sql basics codeus.pdf](1-fundamentals%2F1-1-data-querying%2Fsql%20basics%20codeus.pdf) presentation;
2. Check [readme.md](1-fundamentals%2F1-1-data-querying%2Freadme.md);
3. Implement warmup exercises from [warmup](1-fundamentals%2F1-1-data-querying%2Fsrc%2Ftest%2Fresources%2Fwarmup);
4. Check your warmup solution implementation using [WarmUpSqlQueriesTest.java](1-fundamentals%2F1-1-data-querying%2Fsrc%2Ftest%2Fjava%2Forg%2Fcodeus%2Fdatabase%2Ffundamentals%2Fdata_quering%2FWarmUpSqlQueriesTest.java);
5. Implement main exercises from [main](1-fundamentals%2F1-1-data-querying%2Fsrc%2Ftest%2Fresources%2Fmain);
6. Check your solution implementation using [SqlQueriesTest.java](1-fundamentals%2F1-1-data-querying%2Fsrc%2Ftest%2Fjava%2Forg%2Fcodeus%2Fdatabase%2Ffundamentals%2Fdata_quering%2FSqlQueriesTest.java).

💡 The implemented solution can be found on the `master-completed` branch.
</details>
<details> 
  <summary>1.2 Aggregation and grouping</summary>
👉 **Implement Data Aggregation and Grouping exercises of the module**

1. Check the session's [sql_aggregation_grouping.pdf](1-fundamentals%2F1-2-aggregation-and-grouping%2Fsql_aggregation_grouping.pdf) presentation;
2. Check [readme.md](1-fundamentals%2F1-2-aggregation-and-grouping%2Freadme.md);
3. Implement main exercises from [mandatory](1-fundamentals%2F1-2-aggregation-and-grouping%2Fsrc%2Ftest%2Fresources%2Fqueries%2Fmandatory);
4. Check your solution implementation using [SqlQueriesTest.java](1-fundamentals%2F1-2-aggregation-and-grouping%2Fsrc%2Ftest%2Fjava%2Forg%2Fcodeus%2Fdatabase%2Ffundamentals%2Faggregation_and_grouping%2FSqlQueriesTest.java).

🔥 Implement exercises from [optional](1-fundamentals%2F1-2-aggregation-and-grouping%2Fsrc%2Ftest%2Fresources%2Fqueries%2Foptional).

💡 The implemented solution can be found on the `master-completed` branch.
</details>
<details> 
  <summary>1.3 Joins and subqueries</summary>
👉 **Implement Data Joins and Subqueries exercises of the module**

1. Check the session's [joins+cte.pptx.pdf](1-fundamentals%2F1-3-joins-and-subqueries%2Fjoins+cte.pptx.pdf) presentation;
2. Check [readme.md](1-fundamentals%2F1-3-joins-and-subqueries%2Freadme.md);
3. Implement main exercises from [mandatory](1-fundamentals%2F1-3-joins-and-subqueries%2Fsrc%2Ftest%2Fresources%2Fmandatory);
4. Check your solution implementation using [BankingQueriesTest.java](1-fundamentals%2F1-3-joins-and-subqueries%2Fsrc%2Ftest%2Fjava%2Forg%2Fcodeus%2Fdatabase%2Ffundamentals%2Fjoins_and_subqueries%2FBankingQueriesTest.java).

🔥 Implement exercises from [optional](1-fundamentals%2F1-3-joins-and-subqueries%2Fsrc%2Ftest%2Fresources%2Foptional).

💡 The implemented solution can be found on the `master-completed` branch.
</details>
<details> 
  <summary>1.4 Data modification and security</summary>
👉 **Implement Data Modification and Security Fundamentals exercises of the module**

1. Check the session's [data_modifications_security_fundamentals.pdf](1-fundamentals%2F1-4-data-modification-and-security%2Fdata_modifications_security_fundamentals.pdf) presentation;
2. Check [readme.md](1-fundamentals%2F1-4-data-modification-and-security%2FREADME.md);
3. Implement main exercises from [queries](1-fundamentals%2F1-fundamentals%2F1-4-data-modification-and-security%2F%2Fsrc%2Ftest%2Fresources%2Fqueries);
4. Check your solution implementation using [DataModificationTest.java](1-fundamentals%2F1-4-data-modification-and-security%2Fsrc%2Ftest%2Fjava%2Forg%2Fcodeus%2Fdatabase%2Ffundamentals%2Fdata_modification%2FDataModificationTest.java).

💡 The implemented solution can be found on the `master-completed` branch.
</details>
<details> 
  <summary>1.5 Transaction management</summary>
👉 **Implement Transaction Management exercises of the module**

1. Check the session's [transaction management.pdf](1-fundamentals%2F1-5-transaction-management%2Ftransaction%20management.pdf) presentation;
2. Check [readme.md](1-fundamentals%2F1-5-transaction-management%2FREADME.md);
3. Implement main exercises from [BankingDaoImpl.java](1-fundamentals%2F1-5-transaction-management%2Fsrc%2Fmain%2Fjava%2Forg%2Fcodeus%2Fdatabase%2Ffundamentals%2Ftransaction_management%2Fdao%2FBankingDaoImpl.java);
4. Check your solution implementation using [BankingDaoTest.java](1-fundamentals%2F1-5-transaction-management%2Fsrc%2Ftest%2Fjava%2Forg%2Fcodeus%2Fdatabase%2Ffundamentals%2Ftransaction_management%2Fdao%2FBankingDaoTest.java).

💡 The implemented solution can be found on the `master-completed` branch.
</details>
<details> 
  <summary>1.6 Transaction concurrency control</summary>
👉 **Implement Transaction Concurrency Control exercises of the module**

1. Check the session's [concurrency-control.pdf](1-fundamentals%2F1-6-transaction-concurrency-control%2Fconcurrency-control.pdf) presentation;
2. Check [readme.md](1-fundamentals%2F1-6-transaction-concurrency-control%2FREADME.md);
3. Implement main exercises from [tasks](1-fundamentals%2F1-6-transaction-concurrency-control%2Fsrc%2Ftest%2Fresources%2Ftasks);
4. Check your solution implementation using [TransactionConcurrencyTest.java](1-fundamentals%2F1-6-transaction-concurrency-control%2Fsrc%2Ftest%2Fjava%2Forg%2Fcodeus%2Fdatabase%2Ffundamentals%2Fconcurrency_control%2FTransactionConcurrencyTest.java)

💡 The implemented solution can be found on the `master-completed` branch.
</details>
<details> 
  <summary>1.7 Indexing and query execution planning</summary>
👉 **Implement Transaction Concurrency Control exercises of the module**

1. Check the session's [concurrency-control.pdf](1-fundamentals%2F1-6-transaction-concurrency-control%2Fconcurrency-control.pdf) presentation;
2. Check [readme.md](1-fundamentals%2F1-6-transaction-concurrency-control%2FREADME.md);
3. Implement main exercises from [tasks](1-fundamentals%2F1-6-transaction-concurrency-control%2Fsrc%2Ftest%2Fresources%2Ftasks);
4. Check your solution implementation using [TransactionConcurrencyTest.java](1-fundamentals%2F1-6-transaction-concurrency-control%2Fsrc%2Ftest%2Fjava%2Forg%2Fcodeus%2Fdatabase%2Ffundamentals%2Fconcurrency_control%2FTransactionConcurrencyTest.java)

💡 The implemented solution can be found on the `master-completed` branch.
</details>
<details> 
  <summary>1.8 Views</summary>
👉 **Implement Table Views exercises of the module**

1. Check the session's [views.pdf](1-fundamentals%2F1-8-views%2Fcodeus_view_april_25.pdf) presentation;
2. Check [readme.md](1-fundamentals%2F1-8-views%2Freadme.md);
3. Implement main exercises from [views_and_materialized_views](1-fundamentals%2F1-8-views%2Fsrc%2Ftest%2Fresources%2Fviews_and_materialized_views);
4. Check your solution implementation using [ViewsAndMaterializedViewsTest.java](1-fundamentals%2F1-8-views%2Fsrc%2Ftest%2Fjava%2Forg%2Fcodeus%2Fdatabase%2Ffundamentals%2Fviews%2FViewsAndMaterializedViewsTest.java).

💡 The implemented solution can be found on the `master-completed` branch.
</details>
<details> 
  <summary>1.9 Data normalization</summary>
👉 **Implement Norm Forms exercises of the module**

1. Check the session's [sql-normalization.pdf](1-fundamentals%2F1-9-data-normalization%2Fsql-normalization.pdf) presentation;
2. Check [README.md](1-fundamentals%2F1-9-data-normalization%2FREADME.md);
3. Implement main exercises from [resources](1-fundamentals%2F1-9-data-normalization%2Fsrc%2Ftest%2Fresources) directory;
4. Check your solution implementation using [SqlQueriesTest.java](1-fundamentals%2F1-9-data-normalization%2Fsrc%2Ftest%2Fjava%2Forg%2Fcodeus%2Fdatabase%2Ffundamentals%2Fdata_normalization%2FSqlQueriesTest.java).

💡 The implemented solution can be found on the `master-completed` branch.
</details>
<details> 
  <summary>1.10 Data analytics</summary>
👉 **Implement Data Analytics (using built-in functions) exercises of the module**

1. Check the session's [window functions.pdf](1-fundamentals%2F1-10-data-analytics%2Fwindow%20functions.pdf) presentation;
2. Check [README.md](1-fundamentals%2F1-10-data-analytics%2FREADME.md)
3. Implement main exercises from [queries](1-fundamentals%2F1-10-data-analytics%2Fsrc%2Ftest%2Fresources%2Fqueries);
4. Check your solution implementation using [SqlWindowFunctionTests.java](1-fundamentals%2F1-10-data-analytics%2Fsrc%2Ftest%2Fjava%2Forg%2Fcodeus%2Fdatabase%2Ffundamentals%2Fdata_analytics%2FSqlWindowFunctionTests.java).

💡 The implemented solution can be found on the `master-completed` branch.
</details>
<details> 
  <summary>1.11 Fundamentals I recap</summary>
👉 **Solve problems and verify your knowledge**

1. Check [readme.md](1-fundamentals%2F1-11-fundamentals-I-recap%2Freadme.md);
2. Solve problems from [queries](1-fundamentals%2F1-11-fundamentals-I-recap%2Fsrc%2Ftest%2Fresources%2Fqueries);
3. Check your solution implementation using [SqlQueriesTest.java](1-fundamentals%2F1-11-fundamentals-I-recap%2Fsrc%2Ftest%2Fjava%2Forg%2Fcodeus%2Fdatabase%2Ffundamentals%2Frecap%2FSqlQueriesTest.java).

💡 The implemented solution can be found on the `master-completed` branch.
</details>


