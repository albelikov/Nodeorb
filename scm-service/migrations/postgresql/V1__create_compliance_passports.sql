-- Create compliance passports table
CREATE TABLE compliance_passports (
    id SERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    passport_type VARCHAR(50) NOT NULL, -- ITAR, EAR, EAR99, etc.
    status VARCHAR(50) NOT NULL, -- ACTIVE, EXPIRED, SUSPENDED
    issue_date TIMESTAMP WITH TIME ZONE NOT NULL,
    expiry_date TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, passport_type)
);

-- Create manual entry validation table
CREATE TABLE manual_entry_validation (
    id SERIAL PRIMARY KEY,
    event_id VARCHAR(255) NOT NULL UNIQUE,
    user_id VARCHAR(255) NOT NULL,
    order_id VARCHAR(255) NOT NULL,
    materials_cost DECIMAL(15,2) NOT NULL,
    labor_cost DECIMAL(15,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(50) NOT NULL, -- APPROVED, AUDIT_REQUIRED, REJECTED
    deviation DECIMAL(5,4) NOT NULL,
    median_price DECIMAL(15,2) NOT NULL,
    suggested_median DECIMAL(15,2) NOT NULL,
    audit_required BOOLEAN NOT NULL,
    reason TEXT,
    validated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_order_id (order_id),
    INDEX idx_event_id (event_id),
    INDEX idx_status (status),
    INDEX idx_validated_at (validated_at)
);

-- Create appeals table
CREATE TABLE appeals (
    id SERIAL PRIMARY KEY,
    appeal_id VARCHAR(255) NOT NULL UNIQUE,
    event_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    justification TEXT NOT NULL,
    status VARCHAR(50) NOT NULL, -- SUBMITTED, APPROVED, REJECTED, REVIEWED
    submitted_at TIMESTAMP WITH TIME ZONE NOT NULL,
    reviewed_at TIMESTAMP WITH TIME ZONE,
    reviewed_by VARCHAR(255),
    review_notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (event_id) REFERENCES manual_entry_validation(event_id),
    INDEX idx_appeal_id (appeal_id),
    INDEX idx_event_id (event_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
);

-- Create evidence items table
CREATE TABLE evidence_items (
    id SERIAL PRIMARY KEY,
    appeal_id INTEGER NOT NULL,
    item_id VARCHAR(255) NOT NULL,
    item_type VARCHAR(50) NOT NULL, -- DOCUMENT, PHOTO, INVOICE, TEXT
    url TEXT NOT NULL,
    description TEXT,
    uploaded_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (appeal_id) REFERENCES appeals(id),
    INDEX idx_appeal_id (appeal_id),
    INDEX idx_item_id (item_id)
);

-- Create access checks table
CREATE TABLE access_checks (
    id SERIAL PRIMARY KEY,
    check_id VARCHAR(255) NOT NULL UNIQUE,
    user_id VARCHAR(255) NOT NULL,
    order_id VARCHAR(255) NOT NULL,
    access_granted BOOLEAN NOT NULL,
    reason TEXT,
    requires_biometrics BOOLEAN NOT NULL DEFAULT FALSE,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_check_id (check_id),
    INDEX idx_user_id (user_id),
    INDEX idx_order_id (order_id),
    INDEX idx_access_granted (access_granted),
    INDEX idx_timestamp (timestamp)
);

-- Create geofence checks table
CREATE TABLE geofence_checks (
    id SERIAL PRIMARY KEY,
    check_id VARCHAR(255) NOT NULL UNIQUE,
    user_id VARCHAR(255) NOT NULL,
    order_id VARCHAR(255) NOT NULL,
    is_inside BOOLEAN NOT NULL,
    latitude DECIMAL(10,8) NOT NULL,
    longitude DECIMAL(11,8) NOT NULL,
    violation_reason TEXT,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_check_id (check_id),
    INDEX idx_user_id (user_id),
    INDEX idx_order_id (order_id),
    INDEX idx_is_inside (is_inside),
    INDEX idx_timestamp (timestamp)
);

-- Create hash chain table for WORM storage
CREATE TABLE hash_chain (
    id SERIAL PRIMARY KEY,
    chain_id VARCHAR(255) NOT NULL,
    node_id VARCHAR(255) NOT NULL,
    node_type VARCHAR(50) NOT NULL, -- VALIDATION, APPEAL, ACCESS_CHECK, GEOFENCE_CHECK
    hash_value VARCHAR(64) NOT NULL,
    previous_hash VARCHAR(64),
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(chain_id, node_id),
    INDEX idx_chain_id (chain_id),
    INDEX idx_node_id (node_id),
    INDEX idx_hash_value (hash_value),
    INDEX idx_timestamp (timestamp)
);

-- Create trust scores table
CREATE TABLE trust_scores (
    id SERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL UNIQUE,
    trust_score DECIMAL(5,2) NOT NULL,
    trust_level VARCHAR(20) NOT NULL, -- CRITICAL, LOW, MEDIUM, HIGH
    last_updated TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_trust_score (trust_score),
    INDEX idx_trust_level (trust_level)
);

-- Create triggers for updated_at timestamps
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_compliance_passports_updated_at 
    BEFORE UPDATE ON compliance_passports 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_trust_scores_updated_at 
    BEFORE UPDATE ON trust_scores 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Create function to calculate hash chain root
CREATE OR REPLACE FUNCTION calculate_hash_chain_root(chain_id_param VARCHAR(255))
RETURNS VARCHAR(64) AS $$
DECLARE
    root_hash VARCHAR(64);
    current_hash VARCHAR(64);
    done BOOLEAN := FALSE;
    cur CURSOR FOR 
        SELECT hash_value FROM hash_chain 
        WHERE chain_id = chain_id_param 
        ORDER BY timestamp;
BEGIN
    root_hash := '';
    
    OPEN cur;
    LOOP
        FETCH cur INTO current_hash;
        EXIT WHEN NOT FOUND;
        
        root_hash := encode(digest(root_hash || current_hash, 'sha256'), 'hex');
    END LOOP;
    CLOSE cur;
    
    RETURN root_hash;
END;
$$ LANGUAGE plpgsql;

-- Create function to validate geofence compliance
CREATE OR REPLACE FUNCTION validate_geofence_compliance(
    user_lat DECIMAL(10,8),
    user_lon DECIMAL(11,8),
    cargo_lat DECIMAL(10,8),
    cargo_lon DECIMAL(11,8),
    max_distance_km DECIMAL(8,2) DEFAULT 50.0
)
RETURNS BOOLEAN AS $$
DECLARE
    distance DECIMAL(8,2);
BEGIN
    -- Calculate distance using Haversine formula
    distance := 6371 * 2 * asin(sqrt(
        power(sin(radians(user_lat - cargo_lat) / 2), 2) +
        cos(radians(cargo_lat)) * cos(radians(user_lat)) *
        power(sin(radians(user_lon - cargo_lon) / 2), 2)
    ));
    
    RETURN distance <= max_distance_km;
END;
$$ LANGUAGE plpgsql;

-- Create function to check sanctions compliance
CREATE OR REPLACE FUNCTION check_sanctions_compliance(
    entity_name VARCHAR(255),
    entity_type VARCHAR(50),
    country VARCHAR(3)
)
RETURNS BOOLEAN AS $$
BEGIN
    -- This would integrate with actual sanctions lists
    -- For now, return true (compliant)
    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;

-- Create function to calculate trust score components
CREATE OR REPLACE FUNCTION calculate_trust_score_components(user_id_param VARCHAR(255))
RETURNS TABLE (
    price_accuracy_score DECIMAL(5,2),
    appeal_success_score DECIMAL(5,2),
    biometrics_compliance_score DECIMAL(5,2),
    geographic_compliance_score DECIMAL(5,2),
    time_factor_score DECIMAL(5,2)
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        COALESCE(
            (SELECT AVG(CASE WHEN status = 'APPROVED' THEN 100.0 ELSE 0.0 END)
             FROM manual_entry_validation 
             WHERE user_id = user_id_param), 50.0
        ) AS price_accuracy_score,
        
        COALESCE(
            (SELECT AVG(CASE WHEN status = 'APPROVED' THEN 100.0 ELSE 0.0 END)
             FROM appeals 
             WHERE user_id = user_id_param), 50.0
        ) AS appeal_success_score,
        
        COALESCE(
            (SELECT AVG(CASE WHEN access_granted THEN 100.0 ELSE 0.0 END)
             FROM access_checks 
             WHERE user_id = user_id_param AND requires_biometrics = TRUE), 50.0
        ) AS biometrics_compliance_score,
        
        COALESCE(
            (SELECT AVG(CASE WHEN is_inside AND violation_reason IS NULL THEN 100.0 ELSE 0.0 END)
             FROM geofence_checks 
             WHERE user_id = user_id_param), 50.0
        ) AS geographic_compliance_score,
        
        COALESCE(
            (SELECT LEAST(50.0, (EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - MIN(validated_at))) / 86400 / 30))
             FROM manual_entry_validation 
             WHERE user_id = user_id_param), 0.0
        ) AS time_factor_score;
END;
$$ LANGUAGE plpgsql;

-- Create function to update trust score
CREATE OR REPLACE FUNCTION update_trust_score(user_id_param VARCHAR(255))
RETURNS VOID AS $$
DECLARE
    price_accuracy DECIMAL(5,2);
    appeal_success DECIMAL(5,2);
    biometrics_compliance DECIMAL(5,2);
    geographic_compliance DECIMAL(5,2);
    time_factor DECIMAL(5,2);
    final_score DECIMAL(5,2);
    trust_level VARCHAR(20);
BEGIN
    -- Calculate component scores
    SELECT 
        price_accuracy_score, appeal_success_score, biometrics_compliance_score,
        geographic_compliance_score, time_factor_score
    INTO 
        price_accuracy, appeal_success, biometrics_compliance,
        geographic_compliance, time_factor
    FROM calculate_trust_score_components(user_id_param);
    
    -- Calculate weighted average
    final_score := (
        price_accuracy * 0.30 +
        appeal_success * 0.25 +
        biometrics_compliance * 0.20 +
        geographic_compliance * 0.15 +
        time_factor * 0.10
    );
    
    -- Determine trust level
    CASE 
        WHEN final_score < 25.0 THEN trust_level := 'CRITICAL';
        WHEN final_score < 50.0 THEN trust_level := 'LOW';
        WHEN final_score < 75.0 THEN trust_level := 'MEDIUM';
        ELSE trust_level := 'HIGH';
    END CASE;
    
    -- Update or insert trust score
    INSERT INTO trust_scores (user_id, trust_score, trust_level, last_updated)
    VALUES (user_id_param, final_score, trust_level, CURRENT_TIMESTAMP)
    ON CONFLICT (user_id) 
    DO UPDATE SET 
        trust_score = EXCLUDED.trust_score,
        trust_level = EXCLUDED.trust_level,
        last_updated = EXCLUDED.last_updated;
END;
$$ LANGUAGE plpgsql;