-- Ensure database encoding
SET client_encoding = 'UTF8';

-- Create USERS table
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100),
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);

-- Create POINT_MODEL table
CREATE TABLE IF NOT EXISTS point_model (
    id SERIAL PRIMARY KEY,
    x DOUBLE PRECISION NOT NULL,
    y DOUBLE PRECISION NOT NULL,
    r DOUBLE PRECISION NOT NULL,
    hit BOOLEAN NOT NULL,
    check_time TIMESTAMP,
    user_id BIGINT,
    CONSTRAINT fk_point_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes for performance optimization
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_point_model_user_id ON point_model(user_id);