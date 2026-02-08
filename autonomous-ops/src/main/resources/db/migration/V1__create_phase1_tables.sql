-- V1__create_phase1_tables.sql - Создание таблиц для Phase 1 (MVP)

-- Таблица для миссий
CREATE TABLE missions (
    id BIGSERIAL PRIMARY KEY,
    mission_id VARCHAR(255) NOT NULL UNIQUE,
    mission_type VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    parameters TEXT,
    waypoints TEXT,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL,
    current_waypoint VARCHAR(255),
    progress_percentage DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Таблица для подзадач
CREATE TABLE tasks (
    id BIGSERIAL PRIMARY KEY,
    task_id VARCHAR(255) NOT NULL UNIQUE,
    mission_id VARCHAR(255) NOT NULL,
    task_type VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    parameters TEXT,
    status VARCHAR(50) NOT NULL,
    priority INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (mission_id) REFERENCES missions(mission_id) ON DELETE CASCADE
);

-- Таблица для профилей роботов
CREATE TABLE node_profiles (
    id BIGSERIAL PRIMARY KEY,
    node_id VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    model VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    capabilities TEXT,
    hardware_specs TEXT,
    current_location TEXT,
    battery_level DOUBLE PRECISION NOT NULL,
    current_mission_id VARCHAR(255),
    operational_hours BIGINT NOT NULL,
    last_maintenance_date TIMESTAMP NOT NULL,
    next_maintenance_date TIMESTAMP NOT NULL,
    cost_parameters TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Таблица для планов миссий
CREATE TABLE mission_plans (
    id BIGSERIAL PRIMARY KEY,
    plan_id VARCHAR(255) NOT NULL UNIQUE,
    mission_id VARCHAR(255) NOT NULL,
    version INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL,
    route TEXT,
    total_cost DOUBLE PRECISION NOT NULL,
    cost_breakdown TEXT,
    estimated_duration BIGINT NOT NULL,
    estimated_start_time TIMESTAMP NOT NULL,
    estimated_end_time TIMESTAMP NOT NULL,
    risk_assessment TEXT,
    alternatives TEXT,
    explanation TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255) NOT NULL DEFAULT 'AI-System',
    approved_at TIMESTAMP,
    approved_by VARCHAR(255),
    FOREIGN KEY (mission_id) REFERENCES missions(mission_id) ON DELETE CASCADE
);

-- Таблица для состояния выполнения
CREATE TABLE mission_execution_status (
    id BIGSERIAL PRIMARY KEY,
    mission_id VARCHAR(255) NOT NULL,
    node_id VARCHAR(255) NOT NULL,
    current_status VARCHAR(50) NOT NULL,
    current_task_id VARCHAR(255),
    current_location TEXT,
    current_altitude DOUBLE PRECISION NOT NULL,
    current_speed DOUBLE PRECISION NOT NULL,
    current_heading DOUBLE PRECISION NOT NULL,
    progress_percentage DOUBLE PRECISION NOT NULL,
    completed_tasks INTEGER NOT NULL,
    total_tasks INTEGER NOT NULL,
    battery_level DOUBLE PRECISION NOT NULL,
    estimated_remaining_range DOUBLE PRECISION NOT NULL,
    deviation_from_plan TEXT,
    active_alerts TEXT,
    last_updated TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (mission_id) REFERENCES missions(mission_id) ON DELETE CASCADE,
    FOREIGN KEY (node_id) REFERENCES node_profiles(node_id) ON DELETE SET NULL
);

-- Таблица для стоимостных параметров
CREATE TABLE cost_parameters (
    id BIGSERIAL PRIMARY KEY,
    parameter_id VARCHAR(255) NOT NULL UNIQUE,
    region VARCHAR(255) NOT NULL,
    valid_from TIMESTAMP NOT NULL,
    valid_until TIMESTAMP,
    electricity_price_per_kwh DOUBLE PRECISION NOT NULL,
    fuel_price_per_liter DOUBLE PRECISION,
    battery_swap_cost DOUBLE PRECISION,
    operator_hourly_rate DOUBLE PRECISION NOT NULL,
    technician_hourly_rate DOUBLE PRECISION NOT NULL,
    supervision_cost_per_mission DOUBLE PRECISION NOT NULL,
    penalty_per_minute_delay DOUBLE PRECISION NOT NULL,
    priority_multiplier TEXT,
    repair_cost_estimate DOUBLE PRECISION NOT NULL,
    insurance_premium DOUBLE PRECISION NOT NULL,
    liability_cost_per_incident DOUBLE PRECISION NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    approved_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Индексы для оптимизации
CREATE INDEX idx_missions_mission_id ON missions(mission_id);
CREATE INDEX idx_missions_status ON missions(status);
CREATE INDEX idx_missions_type ON missions(mission_type);
CREATE INDEX idx_missions_assigned_node_id ON missions(mission_id);

CREATE INDEX idx_tasks_task_id ON tasks(task_id);
CREATE INDEX idx_tasks_mission_id ON tasks(mission_id);
CREATE INDEX idx_tasks_sequence ON tasks(priority);
CREATE INDEX idx_tasks_status ON tasks(status);

CREATE INDEX idx_node_profiles_node_id ON node_profiles(node_id);
CREATE INDEX idx_node_profiles_type ON node_profiles(type);
CREATE INDEX idx_node_profiles_status ON node_profiles(status);
CREATE INDEX idx_node_profiles_current_mission_id ON node_profiles(current_mission_id);

CREATE INDEX idx_mission_plans_plan_id ON mission_plans(plan_id);
CREATE INDEX idx_mission_plans_mission_id ON mission_plans(mission_id);
CREATE INDEX idx_mission_plans_status ON mission_plans(status);

CREATE INDEX idx_mission_execution_status_mission_id ON mission_execution_status(mission_id);
CREATE INDEX idx_mission_execution_status_node_id ON mission_execution_status(node_id);

CREATE INDEX idx_cost_parameters_parameter_id ON cost_parameters(parameter_id);
CREATE INDEX idx_cost_parameters_region ON cost_parameters(region);
CREATE INDEX idx_cost_parameters_valid_from ON cost_parameters(valid_from);

-- Вставка начальных данных для стоимостных параметров
INSERT INTO cost_parameters (
    parameter_id,
    region,
    valid_from,
    valid_until,
    electricity_price_per_kwh,
    fuel_price_per_liter,
    battery_swap_cost,
    operator_hourly_rate,
    technician_hourly_rate,
    supervision_cost_per_mission,
    penalty_per_minute_delay,
    priority_multiplier,
    repair_cost_estimate,
    insurance_premium,
    liability_cost_per_incident,
    created_by,
    approved_by
) VALUES (
    'default',
    'global',
    NOW(),
    NULL,
    0.15,
    1.50,
    25.00,
    25.00,
    40.00,
    15.00,
    0.25,
    '{"CRITICAL": 2.0, "HIGH": 1.5, "MEDIUM": 1.0, "LOW": 0.8}',
    500.00,
    150.00,
    10000.00,
    'system',
    'system'
);