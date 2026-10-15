-- mysql-init/01-create-users.sql
CREATE DATABASE IF NOT EXISTS cloudarchitecturedb;
CREATE DATABASE IF NOT EXISTS order_db;
CREATE DATABASE IF NOT EXISTS payment_db;
CREATE DATABASE IF NOT EXISTS inventory_db;
CREATE DATABASE IF NOT EXISTS shipping_db;
CREATE DATABASE IF NOT EXISTS notification_db;

CREATE USER IF NOT EXISTS 'auth_user'@'%' IDENTIFIED BY 'auth_pass_local';
CREATE USER IF NOT EXISTS 'order_user'@'%' IDENTIFIED BY 'order_pass_local';
CREATE USER IF NOT EXISTS 'payment_user'@'%' IDENTIFIED BY 'payment_pass_local';
CREATE USER IF NOT EXISTS 'inventory_user'@'%' IDENTIFIED BY 'inventory_pass_local';
CREATE USER IF NOT EXISTS 'shipping_user'@'%' IDENTIFIED BY 'shipping_pass_local';
CREATE USER IF NOT EXISTS 'notification_user'@'%' IDENTIFIED BY 'notification_pass_local';

GRANT ALL PRIVILEGES ON cloudarchitecturedb.* TO 'auth_user'@'%';
GRANT ALL PRIVILEGES ON order_db.* TO 'order_user'@'%';
GRANT ALL PRIVILEGES ON payment_db.* TO 'payment_user'@'%';
GRANT ALL PRIVILEGES ON inventory_db.* TO 'inventory_user'@'%';
GRANT ALL PRIVILEGES ON shipping_db.* TO 'shipping_user'@'%';
GRANT ALL PRIVILEGES ON notification_db.* TO 'notification_user'@'%';

FLUSH PRIVILEGES;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) UNIQUE NOT NULL,
  email VARCHAR(100) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS roles (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS user_roles (
  user_id BIGINT,
  role_id BIGINT,
  PRIMARY KEY (user_id, role_id),
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (role_id) REFERENCES roles(id)
);

INSERT INTO roles(name) VALUES ('ROLE_USER');
INSERT INTO roles(name) VALUES ('ROLE_ADMIN');

-- Order Service Database
CREATE DATABASE IF NOT EXISTS order_db;
USE order_db;

-- Orders table
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id VARCHAR(255) UNIQUE NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    total_amount DECIMAL(19, 2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    order_items LONGTEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_order_id (order_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
);

-- Outbox Events table (Dual-write pattern)
CREATE TABLE IF NOT EXISTS outbox_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    aggregate_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload LONGTEXT NOT NULL,
    topic_name VARCHAR(100) NOT NULL,
    publish_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP NULL,
    INDEX idx_aggregate_id (aggregate_id),
    INDEX idx_publish_status (publish_status),
    INDEX idx_created_at (created_at)
);

-- Payment Service Database
CREATE DATABASE IF NOT EXISTS payment_db;
USE payment_db;

-- Payments table
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_id VARCHAR(255) UNIQUE NOT NULL,
    order_id VARCHAR(255) UNIQUE NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    transaction_id VARCHAR(255),
    failure_reason VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_order_id (order_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
);

-- Processed Events table (Idempotency/Deduplication)
CREATE TABLE IF NOT EXISTS processed_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id VARCHAR(255) UNIQUE NOT NULL,
    aggregate_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    topic_name VARCHAR(100) NOT NULL,
    partition_id INT NOT NULL,
    offset BIGINT NOT NULL,
    processed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_event_offset (topic_name, partition_id, offset),
    INDEX idx_event_id (event_id),
    INDEX idx_aggregate_id (aggregate_id)
);

-- Inventory Service Database
CREATE DATABASE IF NOT EXISTS inventory_db;
USE inventory_db;

CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id VARCHAR(255) UNIQUE NOT NULL,
    available_quantity INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_product_id (product_id)
);

CREATE TABLE IF NOT EXISTS inventory_reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reservation_id VARCHAR(255) NOT NULL,
    order_id VARCHAR(255) NOT NULL,
    product_id VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    reserved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_id)
);

CREATE TABLE IF NOT EXISTS processed_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id VARCHAR(255) UNIQUE NOT NULL,
    aggregate_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    topic_name VARCHAR(100) NOT NULL,
    partition_id INT NOT NULL,
    offset BIGINT NOT NULL,
    processed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_event_id (event_id)
);

-- Shipping Service Database
CREATE DATABASE IF NOT EXISTS shipping_db;
USE shipping_db;

CREATE TABLE IF NOT EXISTS shipments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shipment_id VARCHAR(255) UNIQUE NOT NULL,
    order_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    tracking_number VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_order_id (order_id)
);