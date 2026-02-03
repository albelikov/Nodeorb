-- V1__initial_schema.sql
CREATE SCHEMA IF NOT EXISTS scm;

SET search_path TO scm;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS postgis_raster;
CREATE EXTENSION IF NOT EXISTS postgis_topology;

-- Таблица Compliance Passports с PostGIS
CREATE TABLE compliance_passports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    keycloak_user_id UUID NOT NULL UNIQUE,
    entity_type VARCHAR(20) NOT NULL,
    trust_score DECIMAL(5,2) DEFAULT 50.00 CHECK (trust_score >= 0 AND trust_score <= 100),
    compliance_status VARCHAR(20) DEFAULT 'PENDING',
    is_biometrics_enabled BOOLEAN DEFAULT FALSE,
    verification_data JSONB DEFAULT '{}'::jsonb,
    geo_fencing_zone GEOMETRY(Geometry, 4326),
    expires_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    version BIGINT DEFAULT 0,
    
    CONSTRAINT valid_compliance_status 
        CHECK (compliance_status IN (
            'PENDING', 'VERIFIED', 'SUSPENDED', 
            'BLACKLISTED', 'EXPIRED', 'UNDER_REVIEW', 'RESTRICTED'
        )),
    CONSTRAINT valid_entity_type
        CHECK (entity_type IN (
            'CARRIER', 'SHIPPER', 'WAREHOUSE', 'DRIVER',
            'DISPATCHER', 'CUSTOMS_AGENT', 'INSURANCE_PROVIDER', 'AUDITOR'
        ))
);

-- Индексы для производительности
CREATE INDEX idx_passports_keycloak_user_id ON compliance_passports(keycloak_user_id);
CREATE INDEX idx_passports_compliance_status ON compliance_passports(compliance_status);
CREATE INDEX idx_passports_trust_score ON compliance_passports(trust_score);
CREATE INDEX idx_passports_expires_at ON compliance_passports(expires_at);
CREATE INDEX idx_passports_geo_fencing_zone ON compliance_passports USING GIST(geo_fencing_zone);
CREATE INDEX idx_passports_verification_data_gin ON compliance_passports USING GIN(verification_data jsonb_path_ops);

-- Таблица ручной валидации
CREATE TABLE manual_entry_validation (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL,
    user_id UUID NOT NULL REFERENCES compliance_passports(id),
    service_source VARCHAR(50),
    materials_cost DECIMAL(15,2) NOT NULL CHECK (materials_cost >= 0),
    labor_cost DECIMAL(15,2) NOT NULL CHECK (labor_cost >= 0),
    currency VARCHAR(3) DEFAULT 'USD',
    total_cost DECIMAL(15,2) GENERATED ALWAYS AS (materials_cost + labor_cost) STORED,
    risk_verdict VARCHAR(20) NOT NULL,
    ai_confidence_score DECIMAL(5,2) CHECK (ai_confidence_score >= 0 AND ai_confidence_score <= 1),
    deviation_percentage DECIMAL(10,4),
    requires_appeal BOOLEAN DEFAULT FALSE,
    appeal_status VARCHAR(20) DEFAULT 'NONE',
    market_median_price DECIMAL(15,2),
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    
    CONSTRAINT valid_risk_verdict 
        CHECK (risk_verdict IN ('GREEN', 'YELLOW', 'RED')),
    CONSTRAINT valid_appeal_status
        CHECK (appeal_status IN ('NONE', 'PENDING', 'APPROVED', 'REJECTED', 'ESCALATED'))
);

-- Partitioning по датам для больших объемов данных
CREATE TABLE manual_entry_validation_y2025 (
    LIKE manual_entry_validation INCLUDING ALL,
    CHECK (created_at >= '2025-01-01' AND created_at < '2026-01-01')
) PARTITION BY RANGE (created_at);

-- Индексы
CREATE INDEX idx_validation_user_id ON manual_entry_validation(user_id);
CREATE INDEX idx_validation_order_id ON manual_entry_validation(order_id);
CREATE INDEX idx_validation_created_at ON manual_entry_validation(created_at);
CREATE INDEX idx_validation_risk_verdict ON manual_entry_validation(risk_verdict);

-- WORM хранилище событий (шаблон для ClickHouse)
COMMENT ON TABLE manual_entry_validation IS 
    'WORM: Write-Once-Read-Many. Changes require new audit entry.';

-- Триггер для предотвращения обновлений
CREATE OR REPLACE FUNCTION prevent_validation_updates()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.created_at IS DISTINCT FROM NEW.created_at THEN
        RAISE EXCEPTION 'WORM: Cannot modify timestamp in manual_entry_validation';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tr_prevent_validation_updates
    BEFORE UPDATE ON manual_entry_validation
    FOR EACH ROW
    EXECUTE FUNCTION prevent_validation_updates();