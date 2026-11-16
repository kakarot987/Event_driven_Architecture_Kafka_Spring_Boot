-- Operator replay audit. Keep these tables across application restarts.
CREATE TABLE IF NOT EXISTS order_db.outbox_replay_audit (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, outbox_id BIGINT NOT NULL,
    operator_id VARCHAR(100) NOT NULL, replayed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_outbox_audit (outbox_id)
);
CREATE TABLE IF NOT EXISTS payment_db.outbox_replay_audit LIKE order_db.outbox_replay_audit;
CREATE TABLE IF NOT EXISTS inventory_db.outbox_replay_audit LIKE order_db.outbox_replay_audit;
CREATE TABLE IF NOT EXISTS shipping_db.outbox_replay_audit LIKE order_db.outbox_replay_audit;

CREATE TABLE IF NOT EXISTS order_db.dlt_replay_audit (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_topic VARCHAR(100) NOT NULL,
    source_partition INTEGER NOT NULL,
    source_offset BIGINT NOT NULL,
    event_id VARCHAR(128) NOT NULL,
    operator_id VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    recorded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP NULL,
    UNIQUE KEY uk_dlt_replay_record (source_topic, source_partition, source_offset)
);
