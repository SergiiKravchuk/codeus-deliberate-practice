--------------------------------------------------------------------------------
-- Event Triggers Practice Schema - Final Version
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
-- DDL Audit Logging (Task 1) and Access Control (Task 3)
--------------------------------------------------------------------------------
CREATE TABLE ddl_audit_log
(
    id          SERIAL PRIMARY KEY,
    command_tag TEXT NOT NULL,
    object_type TEXT,
    object_name TEXT,
    user_name   TEXT NOT NULL,
    logged_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

--------------------------------------------------------------------------------
-- DDL Operations Statistics (Task 4)
--------------------------------------------------------------------------------
CREATE TABLE ddl_operations_stats
(
    id              SERIAL PRIMARY KEY,
    user_name       TEXT    NOT NULL,
    command_tag     TEXT    NOT NULL,
    operation_count INTEGER NOT NULL DEFAULT 1,
    last_executed   TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_name, command_tag)
);

--------------------------------------------------------------------------------
-- Trigger Execution Statistics (Task 5)
--------------------------------------------------------------------------------
CREATE TABLE trigger_execution_stats
(
    id             SERIAL PRIMARY KEY,
    trigger_name   TEXT NOT NULL,
    event_type     TEXT NOT NULL,
    command_tag    TEXT NOT NULL,
    execution_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_name      TEXT NOT NULL,
    success        BOOLEAN   DEFAULT TRUE
);

--------------------------------------------------------------------------------
-- Sample Application Tables for Testing
--------------------------------------------------------------------------------
CREATE TABLE products
(
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(100)   NOT NULL,
    price       DECIMAL(10, 2) NOT NULL,
    category_id INTEGER,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE categories
(
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE products
    ADD CONSTRAINT fk_products_category
        FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE SET NULL;

--------------------------------------------------------------------------------
-- Test View for DDL Operations
--------------------------------------------------------------------------------
CREATE VIEW product_summary AS
SELECT p.id,
       p.name as product_name,
       p.price,
       c.name as category_name,
       p.created_at
FROM products p
         LEFT JOIN categories c ON p.category_id = c.id;
