CREATE TABLE IF NOT EXISTS routes (
    id BIGSERIAL PRIMARY KEY,
    route_number VARCHAR(50) UNIQUE NOT NULL,

    -- Origin & Destination
    origin_address VARCHAR(500) NOT NULL,
    origin_latitude DECIMAL(10, 8) NOT NULL,
    origin_longitude DECIMAL(11, 8) NOT NULL,

    destination_address VARCHAR(500) NOT NULL,
    destination_latitude DECIMAL(10, 8) NOT NULL,
    destination_longitude DECIMAL(11, 8) NOT NULL,

    -- Route details
    total_distance DECIMAL(10, 2),
    total_duration INT,
    estimated_cost DECIMAL(10, 2),
    fuel_consumption DECIMAL(10, 2),
    co2_emissions DECIMAL(10, 2),

    -- Geometry (PostGIS)
    geometry GEOMETRY(LineString, 4326),

    -- Metadata
    vehicle_type VARCHAR(50),
    optimization_type VARCHAR(50),
    status VARCHAR(50),

    calculated_at TIMESTAMP DEFAULT NOW(),
    created_by BIGINT
);

CREATE INDEX IF NOT EXISTS idx_routes_origin ON routes 
    USING GIST (ST_MakePoint(origin_longitude, origin_latitude));
CREATE INDEX IF NOT EXISTS idx_routes_destination ON routes 
    USING GIST (ST_MakePoint(destination_longitude, destination_latitude));