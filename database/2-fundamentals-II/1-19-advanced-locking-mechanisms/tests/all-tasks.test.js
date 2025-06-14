const { Client } = require("pg");
const { readFileSync } = require("fs");
const path = require("path");

const INIT_SQL = readFileSync(process.env.INIT_SQL_PATH || path.join(__dirname, "../init.sql"), "utf8");

describe("All tests:", () => {
  let client1, client2;

  beforeEach(async () => {
    const admin = new Client();
    await admin.connect();
    await admin.query(INIT_SQL);
    await admin.end();
    client1 = new Client();
    client2 = new Client();
    await client1.connect();
    await client2.connect();
  }, 20000);

  afterEach(async () => {
    if (client1) await client1.query("ROLLBACK").catch(() => {});
    if (client1) await client1.end();
    if (client2) await client2.end();
  }, 20000);

  test("Task 1: Insert is blocked by ACCESS EXCLUSIVE lock", async () => {
    console.log("Task 1: Виконується рішення для задачі");
    const sql = readFileSync(path.join("/tasks", "task1.sql"), "utf8");
    await client1.query(sql);
    await new Promise((resolve) => setTimeout(resolve, 1000));
    const insertQuery = "INSERT INTO accounts (customer_id, account_type) VALUES (1, 'checking')";
    console.log("Task 1: SQL для перевірки (insert):\n", insertQuery);
    const insertPromise = client2
      .query(insertQuery)
      .then(() => "success")
      .catch(() => "error");
    const result = await Promise.race([
      insertPromise,
      new Promise((resolve) => setTimeout(() => resolve("blocked"), 2000)),
    ]);
    console.log("Task 1: Результат вставки:", result);
    expect(result).toBe("blocked");
  }, 20000);

  test("Task 2: Concurrent update on the same row is blocked by row-level lock", async () => {
    console.log("Task 2: Виконується рішення для задачі");
    const sql = readFileSync(path.join("/tasks", "task2.sql"), "utf8");
    await client1.query(sql);
    await new Promise((resolve) => setTimeout(resolve, 1000));
    const updateQuery = "UPDATE accounts SET balance = balance + 25 WHERE id = 1";
    console.log("Task 2: SQL для перевірки (update):\n", updateQuery);
    const updatePromise = client2
      .query(updateQuery)
      .then(() => "success")
      .catch(() => "error");
    const result = await Promise.race([
      updatePromise,
      new Promise((resolve) => setTimeout(() => resolve("blocked"), 2000)),
    ]);
    console.log("Task 2: Результат оновлення:", result);
    expect(result).toBe("blocked");
  }, 20000);

  test("Task 3: NOWAIT returns error if row is already locked", async () => {
    console.log("Task 3: Виконується рішення для задачі");
    await client1.query("BEGIN");
    const lockQuery = "SELECT * FROM accounts WHERE id = 2 FOR UPDATE";
    console.log("Task 3: SQL для перевірки (lock):\n", lockQuery);
    await client1.query(lockQuery);
    const sql = readFileSync(path.join("/tasks", "task3.sql"), "utf8");
    let errorCaught = false;
    try {
      await client2.query(sql);
    } catch (e) {
      errorCaught = true;
      console.log("Task 3: Помилка очікувана:", e.message);
    }
    expect(errorCaught).toBe(true);
  }, 20000);

  test("Task 4: SKIP LOCKED allows workers to process different pending transactions", async () => {
    console.log("Task 4: Виконується рішення для задачі");
    await client1.query("BEGIN");
    const lockQuery = "SELECT * FROM transactions WHERE status = 'pending' FOR UPDATE SKIP LOCKED LIMIT 1";
    console.log("Task 4: SQL для перевірки (lock):\n", lockQuery);
    const res1 = await client1.query(lockQuery);
    const lockedId = res1.rows[0]?.id;

    await client2.query("BEGIN");
    const sql = readFileSync(path.join("/tasks", "task4.sql"), "utf8");
    const res2 = await client2.query(sql);
    expect(res2.rows.length).toBeGreaterThan(0);
    const pickedId = res2.rows[0].id;

    console.log("Task 4: lockedId =", lockedId, ", pickedId =", pickedId);
    expect(lockedId).not.toBe(pickedId);

    await client2.query("ROLLBACK");
  }, 20000);

  test("Task 5: SELECT FOR UPDATE blocks concurrent UPDATE and updates balances correctly", async () => {
    console.log("Task 5: Preparing accounts");
    await client1.query("BEGIN");
    await client1.query("INSERT INTO accounts (id, customer_id, account_type, balance) VALUES (10, 1, 'checking', 1000) ON CONFLICT (id) DO UPDATE SET balance = 1000, customer_id = 1, account_type = 'checking'");
    await client1.query("INSERT INTO accounts (id, customer_id, account_type, balance) VALUES (20, 2, 'savings', 1000) ON CONFLICT (id) DO UPDATE SET balance = 1000, customer_id = 2, account_type = 'savings'");
    await client1.query("COMMIT");

    const sql = readFileSync(path.join("/tasks", "task5.sql"), "utf8");
    const queries = sql
      .split(";")
      .map(q => q.trim())
      .filter(q => q && !q.startsWith("--"));

    await client1.query("BEGIN");
    const res = await client1.query(queries[0]);
    console.log("Task 5: Locked ids:", res.rows.map(r => r.id));

    await new Promise(resolve => setTimeout(resolve, 200));

    const updatePromise = client2.query(`
      BEGIN;
      UPDATE accounts SET balance = 700 WHERE id = 10;
      COMMIT;
    `).then(() => "completed").catch(() => "error");

    const updateResult = await Promise.race([
      updatePromise,
      new Promise(resolve => setTimeout(() => resolve("blocked"), 1500))
    ]);
    console.log("Task 5: Concurrent UPDATE result:", updateResult);
    expect(updateResult).toBe("blocked");

    await client1.query(queries[1]);
    await client1.query(queries[2]);

    const res10 = await client1.query("SELECT balance FROM accounts WHERE id = 10");
    const res20 = await client1.query("SELECT balance FROM accounts WHERE id = 20");
    console.log("Task 5: Balances after update:", { id10: res10.rows[0].balance, id20: res20.rows[0].balance });
    expect(Number(res10.rows[0].balance)).toBe(500);
    expect(Number(res20.rows[0].balance)).toBe(1500);

    await client1.query("ROLLBACK;");
  }, 10000);

  test("Task 6: Advisory lock blocks concurrent update", async () => {
    console.log("Task 6: Виконується рішення для задачі");
    const fileContent = readFileSync(path.join("/tasks", "task6.sql"), "utf8");
    await client1.query(fileContent);

    const lockResult = await client1.query(
      `SELECT COUNT(*) AS cnt FROM pg_locks 
      WHERE locktype = 'advisory' AND classid = 0 AND objid = 12345`
    );
    expect(Number(lockResult.rows[0].cnt)).toBeGreaterThan(0);

    await new Promise((resolve) => setTimeout(resolve, 1000));

    const updateQuery = `
      BEGIN;
      SELECT pg_advisory_lock(12345);
      UPDATE accounts SET balance = balance + 25 WHERE id = 1;
      COMMIT;
    `;
    console.log("Task 6: SQL для перевірки (update):\n", updateQuery);
    const updatePromise = client2.query(updateQuery)
      .then(() => "success")
      .catch(() => "error");

    const result = await Promise.race([
      updatePromise,
      new Promise((resolve) => setTimeout(() => resolve("blocked"), 2000)),
    ]);

    console.log("Task 6: Результат конкурентного оновлення:", result);
    expect(result).toBe("blocked");
  }, 10000);

  test("Task 7: SKIP LOCKED for concurrent transaction processing", async () => {
    console.log("Task 7: Виконується рішення для задачі");
    await client1.query("BEGIN");
    const lockQuery = `
      SELECT * FROM transactions
      WHERE status = 'pending'
      FOR UPDATE SKIP LOCKED
      LIMIT 5
    `;
    console.log("Task 7: SQL для перевірки (lock):\n", lockQuery);
    const res1 = await client1.query(lockQuery);
    const lockedIds = res1.rows.map(r => r.id);

    await client2.query("BEGIN");
    const sql = readFileSync(path.join("/tasks", "task7.sql"), "utf8");
    const res2 = await client2.query(sql);
    expect(res2.rows.length).toBeGreaterThan(0);
    const pickedIds = res2.rows.map(r => r.id);

    const overlap = lockedIds.filter(id => pickedIds.includes(id));
    console.log("Task 7: lockedIds =", lockedIds, ", pickedIds =", pickedIds, ", overlap =", overlap);
    expect(overlap.length).toBe(0);

    await client1.query("ROLLBACK");
    await client2.query("ROLLBACK");
  }, 10000);
});