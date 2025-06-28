-- ============================================
-- PostgreSQL Master Setup for Combined Replication Practice
-- Supports both Streaming and Logical Replication
-- ============================================

-- Create replication user for streaming replication
CREATE USER replicator REPLICATION LOGIN PASSWORD 'replicator_pass';

-- Add replication permissions to pg_hba.conf
\! echo "host replication replicator 0.0.0.0/0 md5" >> /var/lib/postgresql/data/pg_hba.conf

-- Reload configuration
SELECT pg_reload_conf();

-- ============================================
-- Sample Database Schema
-- ============================================

-- Users table
CREATE TABLE users
(
    id         SERIAL PRIMARY KEY,
    name       VARCHAR(100)        NOT NULL,
    email      VARCHAR(100) UNIQUE NOT NULL,
    department VARCHAR(50),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Orders table
CREATE TABLE orders
(
    id           SERIAL PRIMARY KEY,
    user_id      INTEGER REFERENCES users (id),
    product_name VARCHAR(100)   NOT NULL,
    amount       DECIMAL(10, 2) NOT NULL,
    status       VARCHAR(20) DEFAULT 'pending',
    created_at   TIMESTAMP   DEFAULT NOW(),
    updated_at   TIMESTAMP   DEFAULT NOW()
);

-- Products table (for logical replication testing)
CREATE TABLE products
(
    id         SERIAL PRIMARY KEY,
    name       VARCHAR(100)   NOT NULL,
    price      DECIMAL(10, 2) NOT NULL,
    category   VARCHAR(50),
    in_stock   BOOLEAN   DEFAULT true,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Audit log table (streaming only - not for logical replication)
CREATE TABLE audit_log
(
    id         SERIAL PRIMARY KEY,
    table_name VARCHAR(50),
    operation  VARCHAR(10),
    changed_by VARCHAR(50),
    changed_at TIMESTAMP DEFAULT NOW()
);

-- ============================================
-- Sample Data
-- ============================================

-- Insert users
INSERT INTO users (name, email, department)
VALUES ('Alice Johnson', 'alice@company.com', 'Engineering'),
       ('Bob Smith', 'bob@company.com', 'Marketing'),
       ('Charlie Brown', 'charlie@company.com', 'Sales'),
       ('Diana Wilson', 'diana@company.com', 'Engineering'),
       ('Eve Davis', 'eve@company.com', 'HR');

-- Insert products
INSERT INTO products (name, price, category, in_stock)
VALUES ('Laptop Pro', 1299.99, 'Electronics', true),
       ('Wireless Mouse', 49.99, 'Electronics', true),
       ('Office Chair', 299.99, 'Furniture', true),
       ('Monitor 4K', 599.99, 'Electronics', true),
       ('Desk Lamp', 79.99, 'Furniture', false);

-- Insert orders
INSERT INTO orders (user_id, product_name, amount, status)
VALUES (1, 'Laptop Pro', 1299.99, 'completed'),
       (2, 'Wireless Mouse', 49.99, 'pending'),
       (3, 'Office Chair', 299.99, 'shipped'),
       (1, 'Monitor 4K', 599.99, 'processing'),
       (4, 'Desk Lamp', 79.99, 'completed');

-- ============================================
-- Logical Replication Setup
-- ============================================

-- Publication for selected tables
CREATE PUBLICATION lab_publication FOR TABLE users, orders;

-- Publication for products only (for selective replication exercise)
CREATE PUBLICATION products_publication FOR TABLE products;
