CREATE TABLE IF NOT EXISTS location_history (
    id BIGSERIAL PRIMARY KEY,
    shipment_id BIGINT NOT NULL REFERENCES shipments(id),
    vehicle_id BIGINT NOT NULL,

    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    altitude DECIMAL(8, 2),

    speed DECIMAL(6, 2),
    heading DECIMAL(5, 2),

    accuracy DECIMAL(6, 2),

    timestamp TIMESTAMP NOT NULL,
    source VARCHAR(50),

    -- PostGIS
    location GEOGRAPHY(POINT, 4326)
);

CREATE INDEX IF NOT EXISTS idx_location_shipment ON location_history(shipment_id, timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_location_vehicle ON location_history(vehicle_id, timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_location_geography ON location_history USING GIST(location);