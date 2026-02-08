CREATE TABLE IF NOT EXISTS route_waypoints (
    id BIGSERIAL PRIMARY KEY,
    route_id BIGINT NOT NULL REFERENCES routes(id) ON DELETE CASCADE,
    sequence_order INT NOT NULL,

    address VARCHAR(500),
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,

    arrival_time TIMESTAMP,
    departure_time TIMESTAMP,
    stop_duration INT,

    UNIQUE(route_id, sequence_order)
);

CREATE INDEX IF NOT EXISTS idx_route_waypoints_route ON route_waypoints(route_id);