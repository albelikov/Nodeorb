CREATE TABLE IF NOT EXISTS shipments (
    id BIGSERIAL PRIMARY KEY,
    shipment_number VARCHAR(50) UNIQUE NOT NULL,

    -- Order reference
    order_id BIGINT,

    -- Route
    route_id BIGINT REFERENCES routes(id),

    -- Carrier & Vehicle
    carrier_id BIGINT,
    vehicle_id BIGINT,
    driver_id BIGINT,

    -- Pickup
    pickup_address VARCHAR(500) NOT NULL,
    pickup_latitude DECIMAL(10, 8) NOT NULL,
    pickup_longitude DECIMAL(11, 8) NOT NULL,
    pickup_date_time_start TIMESTAMP NOT NULL,
    pickup_date_time_end TIMESTAMP NOT NULL,
    actual_pickup_date_time TIMESTAMP,

    -- Delivery
    delivery_address VARCHAR(500) NOT NULL,
    delivery_latitude DECIMAL(10, 8) NOT NULL,
    delivery_longitude DECIMAL(11, 8) NOT NULL,
    delivery_date_time_start TIMESTAMP NOT NULL,
    delivery_date_time_end TIMESTAMP NOT NULL,
    actual_delivery_date_time TIMESTAMP,

    -- Cargo details
    weight NUMERIC(10, 2) NOT NULL,
    volume NUMERIC(10, 2) NOT NULL,
    package_count INT NOT NULL,
    cargo_type VARCHAR(50) NOT NULL,
    description TEXT,
    special_handling TEXT,
    temperature_min NUMERIC(5, 2),
    temperature_max NUMERIC(5, 2),
    hazmat BOOLEAN DEFAULT FALSE,
    hazmat_class VARCHAR(50),

    -- Costs
    base_rate NUMERIC(10, 2),
    fuel_surcharge NUMERIC(10, 2),
    accessorial_charges NUMERIC(10, 2),
    total_cost NUMERIC(10, 2),

    -- Status
    status VARCHAR(50) NOT NULL,

    -- Tracking
    current_latitude DECIMAL(10, 8),
    current_longitude DECIMAL(11, 8),
    last_location_update TIMESTAMP,
    estimated_arrival TIMESTAMP,

    -- Documents
    has_proof_of_delivery BOOLEAN DEFAULT FALSE,
    has_bill_of_lading BOOLEAN DEFAULT FALSE,
    has_cmr BOOLEAN DEFAULT FALSE,

    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_shipments_order ON shipments(order_id);
CREATE INDEX IF NOT EXISTS idx_shipments_route ON shipments(route_id);
CREATE INDEX IF NOT EXISTS idx_shipments_status ON shipments(status);
CREATE INDEX IF NOT EXISTS idx_shipments_carrier ON shipments(carrier_id);
CREATE INDEX IF NOT EXISTS idx_shipments_vehicle ON shipments(vehicle_id);