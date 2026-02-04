-- Создание таблицы мастер-заказов
CREATE TABLE master_orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shipper_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    cargo_type VARCHAR(50) NOT NULL,
    total_weight DECIMAL(15,2) NOT NULL,
    total_volume DECIMAL(15,2) NOT NULL,
    remaining_weight DECIMAL(15,2) NOT NULL,
    remaining_volume DECIMAL(15,2) NOT NULL,
    pickup_location GEOGRAPHY(POINT, 4326) NOT NULL,
    delivery_location GEOGRAPHY(POINT, 4326) NOT NULL,
    pickup_address VARCHAR(500) NOT NULL,
    delivery_address VARCHAR(500) NOT NULL,
    required_delivery_date TIMESTAMP NOT NULL,
    max_bid_amount DECIMAL(15,2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    is_ltl_enabled BOOLEAN NOT NULL DEFAULT true,
    min_load_percentage DOUBLE PRECISION NOT NULL DEFAULT 0.8,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Создание таблицы частичных заказов
CREATE TABLE partial_orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    master_order_id UUID NOT NULL REFERENCES master_orders(id) ON DELETE CASCADE,
    weight DECIMAL(15,2) NOT NULL,
    volume DECIMAL(15,2) NOT NULL,
    percentage DOUBLE PRECISION NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE',
    assigned_carrier_id UUID,
    assigned_bid_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Создание таблицы снимков SCM
CREATE TABLE scm_snapshots (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bid_id UUID NOT NULL,
    carrier_id UUID NOT NULL,
    master_order_id UUID NOT NULL,
    snapshot_date TIMESTAMP NOT NULL,
    compliance_status VARCHAR(50) NOT NULL,
    compliance_details TEXT,
    security_clearance VARCHAR(50) NOT NULL,
    security_details TEXT,
    risk_score DOUBLE PRECISION NOT NULL,
    risk_factors TEXT,
    audit_trail TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Индексы для производительности
CREATE INDEX idx_master_orders_shipper_id ON master_orders(shipper_id);
CREATE INDEX idx_master_orders_status ON master_orders(status);
CREATE INDEX idx_master_orders_pickup_location ON master_orders USING GIST(pickup_location);
CREATE INDEX idx_master_orders_delivery_location ON master_orders USING GIST(delivery_location);
CREATE INDEX idx_master_orders_created_at ON master_orders(created_at);

CREATE INDEX idx_partial_orders_master_order_id ON partial_orders(master_order_id);
CREATE INDEX idx_partial_orders_status ON partial_orders(status);
CREATE INDEX idx_partial_orders_assigned_carrier_id ON partial_orders(assigned_carrier_id);

CREATE INDEX idx_scm_snapshots_bid_id ON scm_snapshots(bid_id);
CREATE INDEX idx_scm_snapshots_carrier_id ON scm_snapshots(carrier_id);
CREATE INDEX idx_scm_snapshots_master_order_id ON scm_snapshots(master_order_id);
CREATE INDEX idx_scm_snapshots_snapshot_date ON scm_snapshots(snapshot_date);

-- Обновление существующей таблицы bids для поддержки новых связей
ALTER TABLE bids ADD COLUMN IF NOT EXISTS master_order_id UUID;
ALTER TABLE bids ADD COLUMN IF NOT EXISTS partial_order_id UUID;
ALTER TABLE bids ADD COLUMN IF NOT EXISTS scm_snapshot_id UUID;

-- Добавление внешних ключей
ALTER TABLE bids ADD CONSTRAINT fk_bid_master_order 
    FOREIGN KEY (master_order_id) REFERENCES master_orders(id) ON DELETE CASCADE;
ALTER TABLE bids ADD CONSTRAINT fk_bid_partial_order 
    FOREIGN KEY (partial_order_id) REFERENCES partial_orders(id) ON DELETE CASCADE;
ALTER TABLE bids ADD CONSTRAINT fk_bid_scm_snapshot 
    FOREIGN KEY (scm_snapshot_id) REFERENCES scm_snapshots(id) ON DELETE SET NULL;

-- Индексы для новых колонок в bids
CREATE INDEX idx_bids_master_order_id ON bids(master_order_id);
CREATE INDEX idx_bids_partial_order_id ON bids(partial_order_id);
CREATE INDEX idx_bids_scm_snapshot_id ON bids(scm_snapshot_id);

-- Функция для обновления updated_at
CREATE OR REPLACE FUNCTION update_master_orders_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE OR REPLACE FUNCTION update_partial_orders_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Триггеры для автоматического обновления updated_at
CREATE TRIGGER update_master_orders_updated_at 
    BEFORE UPDATE ON master_orders 
    FOR EACH ROW EXECUTE FUNCTION update_master_orders_updated_at();

CREATE TRIGGER update_partial_orders_updated_at 
    BEFORE UPDATE ON partial_orders 
    FOR EACH ROW EXECUTE FUNCTION update_partial_orders_updated_at();