# PostgreSQL Replication Training Practice - Setup

## Overview

This practice provides hands-on experience with **PostgreSQL Replication** using a multi-container Docker environment. You will work with both **Streaming Replication** (physical) and **Logical Replication** to understand their differences, use cases, and practical implementation.

## Total Time: ~70 minutes

## Architecture

The practice consists of 3 PostgreSQL 17 containers:

```
┌─────────────────┐    streaming     ┌─────────────────┐
│   Master        │ ────────────────→│ Streaming Slave │
│   (Port 5435)   │                  │   (Port 5436)   │
│                 │                  │                 │
│ ✅ Read/Write   │                  │ ✅ Read Only    │
│ ✅ Publications │                  │ ✅ Hot Standby  │
└─────────────────┘                  └─────────────────┘
         │
         │ logical
         ▼
┌─────────────────┐
│ Logical Sub.    │
│ (Port 5437)     │
│                 │
│ ✅ Read/Write   │
│ ✅ Subscriptions│
└─────────────────┘
```

## Prerequisites

- **Docker Desktop** installed and running
- **IntelliJ IDEA** with Database plugin
- Basic **PostgreSQL knowledge** (SELECT, INSERT, etc.)
- Basic **SQL understanding**

## Quick Start

### Step 1: Clone and Start Environment

```bash
# Clone the repository
git clone https://github.com/SergiiKravchuk/codeus-deliberate-practice.git
git checkout 1-23-replication
cd 1-23-replication

# Start all containers
docker compose up -d

# Verify all containers are running
docker compose ps
```

**Expected Output:**
```
NAME                    STATUS    PORTS
pg-master               Up        0.0.0.0:5435->5432/tcp
pg-streaming-slave      Up        0.0.0.0:5436->5432/tcp  
pg-logical-subscriber   Up        0.0.0.0:5437->5432/tcp
```

### Step 2: Configure Database Connections in IntelliJ IDEA

Open **Database Tool Window** (`View → Tool Windows → Database`)

#### Connection 1: Master Database
- **Name:** `master`
- **Host:** `localhost`
- **Port:** `5435`
- **Database:** `replication_lab`
- **User:** `postgres`
- **Password:** `password`
- **Test Connection** ✅

#### Connection 2: Streaming Slave
- **Name:** `streaming_slave`
- **Host:** `localhost`
- **Port:** `5436`
- **Database:** `replication_lab`
- **User:** `postgres`
- **Password:** `password`
- **Test Connection** ✅

#### Connection 3: Logical Subscriber
- **Name:** `logical_subscriber`
- **Host:** `localhost`
- **Port:** `5437`
- **Database:** `replication_lab`
- **User:** `postgres`
- **Password:** `password`
- **Test Connection** ✅

### Step 3: Initial Environment Verification

Run this query on **all three databases** to verify setup:

```sql
SELECT 
    inet_server_port() as port,
    current_database() as database,
    current_user as user,
    version() as postgresql_version;
```

**Expected Results:**
- Master: `port = 5432` (internal Docker port)
- Streaming Slave: `port = 5432`
- Logical Subscriber: `port = 5432`

## Database Schema

Each database contains the following tables:

### `users` Table
```sql
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    department VARCHAR(50),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);
```

### `orders` Table
```sql
CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    product_name VARCHAR(100) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);
```

### `products` Table
```sql
CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    category VARCHAR(50),
    created_at TIMESTAMP DEFAULT NOW()
);
```

## Initial Data

The **Master** database contains sample data:
- **5 users** (Alice, Bob, Charlie, Diana, Eve)
- **5 products** (Laptop, Mouse, Chair, Monitor, Lamp)
- **5 orders** (orders with various statuses)

The **Streaming Slave** automatically receives this data.
The **Logical Subscriber** starts empty (configured in exercises).

## Practice Structure

Complete the exercises in order:

1. **[Exercise 01: Environment Discovery](exercises/exercise-01-discovery.md)** 
    - Identify database roles and status
    - Explore sample data

2. **[Exercise 02: Streaming Replication](exercises/exercise-02-streaming.md)** 
    - Test physical replication behavior
    - Monitor replication status and lag

3. **[Exercise 03: Logical Replication](exercises/exercise-03-logical.md)** 
    - Configure subscriptions
    - Test selective replication

4. **[Exercise 04: Comparison Analysis](exercises/exercise-04-comparison.md)** 
    - Compare both replication types
    - Analyze use cases


## Troubleshooting

### Container Issues
```bash
# Check container status
docker compose ps

# View container logs
docker compose logs postgres-master
docker compose logs postgres-streaming-slave
docker compose logs postgres-logical-subscriber

# Restart specific container
docker restart pg-master

# Reset entire environment
docker compose down -v
docker compose up -d
```

### Connection Issues
- Verify Docker Desktop is running
- Check port availability (5435, 5436, 5437)
- Ensure containers are in "Up" status
- Test connection from terminal:
  ```bash
  psql -h localhost -p 5435 -U postgres -d replication_lab
  ```

### Permission Issues
```bash
# If you get permission errors, try:
docker compose down
docker system prune
docker compose up -d
```

## Cleanup

When finished with the practice:

```bash
# Stop and remove containers, networks, volumes
docker compose down -v

# Optional: Remove images
docker rmi postgres:17-alpine
```

## Next Steps

After completing the setup, proceed to **[Exercise 01: Environment Discovery](exercises/exercise-01-discovery.md)** to begin the hands-on learning experience.