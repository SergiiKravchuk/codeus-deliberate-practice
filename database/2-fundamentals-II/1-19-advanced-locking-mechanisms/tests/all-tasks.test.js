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

  test("Task 5: Only SELECT FOR UPDATE блокує конкурентний UPDATE", async () => {
    await client1.query("UPDATE accounts SET balance = 1000 WHERE id = 10");
    await client1.query("UPDATE accounts SET balance = 1000 WHERE id = 20");

    await client1.query("BEGIN;");
    const sql = readFileSync(path.join("/tasks", "task5.sql"), "utf8");

    const selectQuery = sql.split(";")[0] + ";";
    await client1.query(selectQuery);

    await new Promise(resolve => setTimeout(resolve, 500));

    const updatePromise = client2.query(`
      BEGIN;
      UPDATE accounts SET balance = 500 WHERE id = 10;
      COMMIT;
    `).then(() => "completed").catch(() => "error");

    const updateResult = await Promise.race([
      updatePromise,
      new Promise(resolve => setTimeout(() => resolve("blocked"), 1500))
    ]);

    await client1.query("ROLLBACK;");

    expect(updateResult).toBe("blocked");
  }, 10000);

  test("Task 6: Advisory lock blocks concurrent update", async () => {
      const fileContent = readFileSync(path.join("/tasks", "task6.sql"), "utf8");

      await client1.query(fileContent);

      const lockResult = await client1.query(
        `SELECT COUNT(*) AS cnt FROM pg_locks 
        WHERE locktype = 'advisory' AND classid = 0 AND objid = 12345`
      );
      expect(Number(lockResult.rows[0].cnt)).toBeGreaterThan(0);

      await new Promise((resolve) => setTimeout(resolve, 1000));

      const updatePromise = client2.query(`
        BEGIN;
        SELECT pg_advisory_lock(12345);
        UPDATE accounts SET balance = balance + 25 WHERE id = 1;
        COMMIT;
      `)
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
});