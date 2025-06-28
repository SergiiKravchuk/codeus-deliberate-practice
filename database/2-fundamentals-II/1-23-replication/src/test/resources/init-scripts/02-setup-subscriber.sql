-- ============================================
-- PostgreSQL Logical Subscriber Setup
-- Creates minimal schema for logical replication exercises
-- ============================================

-- Users table (matching master structure)
CREATE TABLE users
(
    id         SERIAL PRIMARY KEY,
    name       VARCHAR(100)        NOT NULL,
    email      VARCHAR(100) UNIQUE NOT NULL,
    department VARCHAR(50),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Orders table (matching master structure)
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

-- Products table (matching master structure)
CREATE TABLE products
(
    id         SERIAL PRIMARY KEY,
    name       VARCHAR(100)   NOT NULL,
    price      DECIMAL(10, 2) NOT NULL,
    category   VARCHAR(50),
    in_stock   BOOLEAN   DEFAULT true,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Setup complete
DO
$$
    BEGIN
        RAISE NOTICE 'Logical subscriber initialized - ready for subscriptions';
    END
$$;