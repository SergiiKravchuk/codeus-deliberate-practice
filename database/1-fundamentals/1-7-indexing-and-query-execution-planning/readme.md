# SQL Indexing & Query Execution Planning Tasks

This project explores how different types of indexes (B-tree and Hash) affect SQL query performance. You'll use a PostgreSQL database running in Docker, write SQL tasks, and verify them with automated tests.

---

## 🚀 Getting Started

### 1. Setup the Database

To spin up the database environment, run the following command in your terminal:

```bash
docker-compose up -d
```

This command starts two containers:

- **PostgreSQL** (database)
- **pgAdmin** (web-based UI for PostgreSQL)

Both are defined in the `docker-compose.yml` file, located in the root of the project (same level as this README).

---

### 2. Connect to the Database

Once the containers are up:

- Open your browser and go to [http://localhost:5050](http://localhost:5050) to access **pgAdmin**, or
- Use any other PostgreSQL client like **psql**, **DBeaver**, or **DataGrip**

⚠️ **Important:** When a task requires analyzing a query using `EXPLAIN ANALYZE`, make sure you're connected to the **Dockerized** database.

---

### 3. Navigate to SQL Tasks

Go to:

```
src/test/resources/queries/
```

Inside, you'll find two folders:

- `b-tree-index/`: tasks related to B-tree indexes
- `hash-index/`: tasks focused on Hash indexes

Each `.sql` file in these folders contains a task to implement and test. Tasks might include:

- Adding/removing indexes
- Analyzing execution plans
- Optimizing query performance

---

### 4. Run Tests

To validate your implementations, run the test suite. Tests are located at:

```
src/test/java/org/codeus/database/fundamentals/indexing_and_query_execution_planning/SqlQueriesTest.java
```

Run the `SqlQueriesTest` class directly from your IDE.

---

## 📌 Notes

- Follow the instructions inside each `.sql` file carefully.
- When asked to use `EXPLAIN ANALYZE`, copy and execute the SQL directly inside the Docker-connected database.
- Some tasks may ask you to create, modify, or drop indexes.
- Pay attention to whether indexes are being used by the planner (look for **Index Scan** vs **Seq Scan**).

---

**Happy learning!** 🔍📈
