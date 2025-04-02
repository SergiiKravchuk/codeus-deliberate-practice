--Currency ledger for isolation level demos
CREATE TABLE accounts
(
    account_id  SERIAL PRIMARY KEY,
    customer_id INT            NOT NULL,
    balance     DECIMAL(10, 2) NOT NULL,
    currency    VARCHAR(3)     NOT NULL,
    version     INT            NOT NULL DEFAULT 1,
    created_at  TIMESTAMP               DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE isolation_level_test
(
    test_id         SERIAL PRIMARY KEY,
    isolation_level VARCHAR(50)    NOT NULL,
    account_id      INTEGER,
    first_read      DECIMAL(10, 2) NOT NULL,
    second_read     DECIMAL(10, 2) NOT NULL,
    test_timestamp  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE serialization_test_results
(
    result_id       SERIAL PRIMARY KEY,
    isolation_level VARCHAR(50)    NOT NULL,
    transaction_id  INT            NOT NULL,
    initial_total   DECIMAL(10, 2) NOT NULL,
    final_balance   DECIMAL(10, 2) NOT NULL,
    status          VARCHAR(20)    NOT NULL,
    timestamp       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE lock_test_results
(
    result_id         SERIAL PRIMARY KEY,
    transaction_id    INT,
    lock_type         VARCHAR(50),
    lock_mode         VARCHAR(50),
    operation         VARCHAR(50),
    account_id        INT,
    resource_id       INT,
    initial_balance   DECIMAL(10, 2),
    final_balance     DECIMAL(10, 2),
    success           BOOLEAN,
    execution_time_ms DECIMAL(10, 2),
    blocking_time_ms  DECIMAL(10, 2),
    version_before    INT,
    version_after     INT,
    retry_count       INT,
    timestamp         TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
