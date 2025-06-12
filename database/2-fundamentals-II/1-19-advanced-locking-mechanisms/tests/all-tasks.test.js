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
    const sql = readFileSync(path.join("/tasks", "task1.sql"), "utf8");
    await client1.query(sql);
    await new Promise((resolve) => setTimeout(resolve, 1000));
    const insertPromise = client2
      .query("INSERT INTO accounts (customer_id, account_type) VALUES (1, 'checking')")
      .then(() => "success")
      .catch(() => "error");
    const result = await Promise.race([
      insertPromise,
      new Promise((resolve) => setTimeout(() => resolve("blocked"), 2000)),
    ]);
    expect(result).toBe("blocked");
  }, 20000);


  test("Task 2: Concurrent update on the same row is blocked by row-level lock", async () => {
    const sql = readFileSync(path.join("/tasks", "task2.sql"), "utf8");
    await client1.query(sql);
    await new Promise((resolve) => setTimeout(resolve, 1000));
    const updatePromise = client2
      .query("UPDATE accounts SET balance = balance + 25 WHERE id = 1")
      .then(() => "success")
      .catch(() => "error");
    const result = await Promise.race([
      updatePromise,
      new Promise((resolve) => setTimeout(() => resolve("blocked"), 2000)),
    ]);
    expect(result).toBe("blocked");
  }, 20000);


  test("Task 3: NOWAIT returns error if row is already locked", async () => {
    await client1.query("BEGIN");
    await client1.query("SELECT * FROM accounts WHERE id = 2 FOR UPDATE");
    const sql = readFileSync(path.join("/tasks", "task3.sql"), "utf8");
    let errorCaught = false;
    try {
      await client2.query(sql);
    } catch (e) {
      errorCaught = true;
    }
    expect(errorCaught).toBe(true);
  }, 20000);


  test("Task 4: SKIP LOCKED allows workers to process different pending transactions", async () => {
    await client1.query("BEGIN");
    const res1 = await client1.query(
      "SELECT * FROM transactions WHERE status = 'pending' FOR UPDATE SKIP LOCKED LIMIT 1"
    );
    const lockedId = res1.rows[0]?.id;

    await client2.query("BEGIN");
    const sql = readFileSync(path.join("/tasks", "task4.sql"), "utf8");
    const res2 = await client2.query(sql);
    expect(res2.rows.length).toBeGreaterThan(0);
    const pickedId = res2.rows[0].id;

    expect(lockedId).not.toBe(pickedId);

    await client2.query("ROLLBACK");
  }, 20000);


  test("Task 5: Concurrent transfer is blocked by row-level locks", async () => {
    await client1.query("UPDATE accounts SET balance = 1000 WHERE id = 10");
    await client1.query("UPDATE accounts SET balance = 1000 WHERE id = 20");
    const sql = readFileSync(path.join("/tasks", "task5.sql"), "utf8");
    await client1.query(sql);

    await new Promise((resolve) => setTimeout(resolve, 1000));

    const transferPromise = client2
      .query(`
        BEGIN;
        SELECT * FROM accounts WHERE id IN (10, 20) ORDER BY id FOR UPDATE;
        UPDATE accounts SET balance = balance - 500 WHERE id = 10 AND balance >= 500;
        UPDATE accounts SET balance = balance + 500 WHERE id = 20;
      `)
      .then(() => "success")
      .catch(() => "error");

    const result = await Promise.race([
      transferPromise,
      new Promise((resolve) => setTimeout(() => resolve("blocked"), 2000)),
    ]);
    expect(result).toBe("blocked");
  }, 20000);

  test("Task 6: Row-level lock for safe transaction status update", async () => {
    const sql = readFileSync(path.join("/tasks", "task6.sql"), "utf8");
    await client1.query(sql);

    await new Promise((resolve) => setTimeout(resolve, 1000));

    const updatePromise = client2
      .query("UPDATE transactions SET status = 'failed' WHERE id = 1")
      .then(() => "success")
      .catch(() => "error");

    const result = await Promise.race([
      updatePromise,
      new Promise((resolve) => setTimeout(() => resolve("blocked"), 2000)),
    ]);

    expect(result).toBe("blocked");
  }, 10000);

  test("Task 7: SKIP LOCKED for concurrent transaction processing", async () => {
    await client1.query("BEGIN");
    const res1 = await client1.query(`
      SELECT * FROM transactions
      WHERE status = 'pending'
      FOR UPDATE SKIP LOCKED
      LIMIT 5
    `);
    const lockedIds = res1.rows.map(r => r.id);

    await client2.query("BEGIN");
    const sql = readFileSync(path.join("/tasks", "task7.sql"), "utf8");
    const res2 = await client2.query(sql);
    expect(res2.rows.length).toBeGreaterThan(0);
    const pickedIds = res2.rows.map(r => r.id);

    const overlap = lockedIds.filter(id => pickedIds.includes(id));
    expect(overlap.length).toBe(0);

    await client1.query("ROLLBACK");
    await client2.query("ROLLBACK");
  }, 10000);

  test("Task 8: No deadlock occurs when locking loans in the same order", async () => {
    await client1.query("UPDATE loans SET interest_rate = 5.5 WHERE id = 1");
    await client1.query("UPDATE loans SET interest_rate = 4.5 WHERE id = 2");
    const sql = readFileSync(path.join("/tasks", "task8.sql"), "utf8");
    await client1.query(sql);

    await new Promise((resolve) => setTimeout(resolve, 1000));

    const updatePromise = client2
      .query(`
        BEGIN;
        SELECT * FROM loans WHERE id IN (1,2) ORDER BY id FOR UPDATE;
        UPDATE loans SET interest_rate = interest_rate * 1.05 WHERE id IN (1,2);
      `)
      .then(() => "success")
      .catch(e => (e.code === "40P01" ? "deadlock" : "error"));

    const result = await Promise.race([
      updatePromise,
      new Promise((resolve) => setTimeout(() => resolve("blocked"), 2000)),
    ]);

    expect(result).toBe("blocked");
  }, 10000);
});