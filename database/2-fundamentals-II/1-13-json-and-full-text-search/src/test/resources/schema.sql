--------------------------------------------------------------------------------
-- Create Quotes table
--------------------------------------------------------------------------------
CREATE TABLE quotes (
    id SERIAL PRIMARY KEY,
    person TEXT,
    quote TEXT
);

--------------------------------------------------------------------------------
-- Create Posts table
--------------------------------------------------------------------------------
DROP TABLE IF EXISTS posts;

CREATE TABLE posts (
    id SERIAL PRIMARY KEY,
    title TEXT,
    body TEXT
);
--------------------------------------------------------------------------------
-- Create History Facts table
--------------------------------------------------------------------------------
CREATE TABLE history_facts (
    id SERIAL PRIMARY KEY,
    title TEXT,
    details JSONB
);
CREATE INDEX idx_history_details_gin ON history_facts USING GIN (details);