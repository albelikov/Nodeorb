-- ClickHouse Schema for SCM Audit Log
-- WORM-compliant schema with MergeTree engine

-- Create database if it doesn't exist
CREATE DATABASE IF NOT EXISTS scm_audit;

-- Use the database
USE scm_audit;

-- Create audit log table with WORM compliance
CREATE TABLE IF NOT EXISTS scm_audit_log (
    event_time DateTime,
    order_id String,
    risk_verdict String,
    input_data String,
    current_hash String,
    prev_hash Nullable(String),
    event_type String
) ENGINE = MergeTree()
ORDER BY (event_time, order_id, current_hash)
SETTINGS 
    index_granularity = 8192,
    allow_experimental_lightweight_delete = 0,
    allow_experimental_alter_update = 0,
    allow_experimental_alter_delete = 0;

-- Create evidence log table
CREATE TABLE IF NOT EXISTS scm_evidence_log (
    order_id String,
    evidence_package String,
    created_at DateTime
) ENGINE = MergeTree()
ORDER BY (order_id, created_at)
SETTINGS 
    index_granularity = 8192,
    allow_experimental_lightweight_delete = 0,
    allow_experimental_alter_update = 0,
    allow_experimental_alter_delete = 0;

-- Create materialized view for aggregated audit statistics
CREATE MATERIALIZED VIEW IF NOT EXISTS audit_stats_mv
TO scm_audit_log
AS SELECT
    event_time,
    order_id,
    risk_verdict,
    input_data,
    current_hash,
    prev_hash,
    event_type
FROM scm_audit_log;

-- Create projection for faster queries by order_id
ALTER TABLE scm_audit_log ADD PROJECTION order_id_projection (
    SELECT
        order_id,
        event_time,
        risk_verdict,
        current_hash
    ORDER BY (order_id, event_time)
);

-- Optimize table for better performance
OPTIMIZE TABLE scm_audit_log FINAL;

-- Verify table creation
SELECT 
    name as table_name,
    engine,
    create_table_query
FROM system.tables 
WHERE database = 'scm_audit' AND name IN ('scm_audit_log', 'scm_evidence_log');