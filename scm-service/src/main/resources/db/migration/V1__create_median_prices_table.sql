-- Создание таблицы для хранения медианных цен
CREATE TABLE median_prices (
    id BIGSERIAL PRIMARY KEY,
    cargo_type VARCHAR(50) NOT NULL,
    route_distance_km INTEGER NOT NULL,
    region VARCHAR(100) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    materials_median DECIMAL(15,2) NOT NULL,
    labor_median DECIMAL(15,2) NOT NULL,
    total_median DECIMAL(15,2) NOT NULL,
    data_points_count INTEGER NOT NULL,
    calculated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Создание индексов для оптимизации запросов
CREATE INDEX idx_median_prices_cargo_type ON median_prices(cargo_type);
CREATE INDEX idx_median_prices_route_distance ON median_prices(route_distance_km);
CREATE INDEX idx_median_prices_region ON median_prices(region);
CREATE INDEX idx_median_prices_currency ON median_prices(currency);
CREATE INDEX idx_median_prices_calculated_at ON median_prices(calculated_at);

-- Создание составного индекса для основного запроса
CREATE INDEX idx_median_prices_lookup ON median_prices(cargo_type, route_distance_km, region, currency);

-- Создание таблицы для хранения исторических данных цен
CREATE TABLE price_history (
    id BIGSERIAL PRIMARY KEY,
    order_id VARCHAR(50) NOT NULL,
    cargo_type VARCHAR(50) NOT NULL,
    route_distance_km INTEGER NOT NULL,
    region VARCHAR(100) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    materials_cost DECIMAL(15,2) NOT NULL,
    labor_cost DECIMAL(15,2) NOT NULL,
    total_cost DECIMAL(15,2) NOT NULL,
    submitted_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Создание индексов для истории цен
CREATE INDEX idx_price_history_order_id ON price_history(order_id);
CREATE INDEX idx_price_history_cargo_type ON price_history(cargo_type);
CREATE INDEX idx_price_history_route_distance ON price_history(route_distance_km);
CREATE INDEX idx_price_history_region ON price_history(region);
CREATE INDEX idx_price_history_currency ON price_history(currency);
CREATE INDEX idx_price_history_submitted_at ON price_history(submitted_at);

-- Создание функции для расчета медианы
CREATE OR REPLACE FUNCTION calculate_median_price(
    p_cargo_type VARCHAR(50),
    p_route_distance_km INTEGER,
    p_region VARCHAR(100),
    p_currency VARCHAR(3)
) RETURNS DECIMAL(15,2) AS $$
DECLARE
    median_price DECIMAL(15,2);
BEGIN
    -- Расчет медианы с использованием PERCENTILE_CONT
    SELECT PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY total_cost)
    INTO median_price
    FROM price_history
    WHERE cargo_type = p_cargo_type
        AND route_distance_km = p_route_distance_km
        AND region = p_region
        AND currency = p_currency
        AND submitted_at >= NOW() - INTERVAL '1 year'; -- Используем данные за последний год
    
    RETURN median_price;
END;
$$ LANGUAGE plpgsql;

-- Создание функции для обновления медианных цен
CREATE OR REPLACE FUNCTION update_median_prices() RETURNS VOID AS $$
DECLARE
    rec RECORD;
    median_materials DECIMAL(15,2);
    median_labor DECIMAL(15,2);
    median_total DECIMAL(15,2);
    data_points INTEGER;
BEGIN
    -- Удаляем старые записи (оставляем только последние 100 записей для каждого типа)
    DELETE FROM median_prices mp1
    WHERE id NOT IN (
        SELECT id FROM median_prices mp2
        WHERE mp2.cargo_type = mp1.cargo_type
            AND mp2.route_distance_km = mp1.route_distance_km
            AND mp2.region = mp1.region
            AND mp2.currency = mp1.currency
        ORDER BY calculated_at DESC
        LIMIT 100
    );

    -- Вставляем новые медианные значения
    FOR rec IN
        SELECT DISTINCT cargo_type, route_distance_km, region, currency
        FROM price_history
        WHERE submitted_at >= NOW() - INTERVAL '1 year'
    LOOP
        -- Рассчитываем медианы
        SELECT PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY materials_cost),
               PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY labor_cost),
               PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY total_cost),
               COUNT(*)
        INTO median_materials, median_labor, median_total, data_points
        FROM price_history
        WHERE cargo_type = rec.cargo_type
            AND route_distance_km = rec.route_distance_km
            AND region = rec.region
            AND currency = rec.currency
            AND submitted_at >= NOW() - INTERVAL '1 year';

        -- Вставляем новую запись
        INSERT INTO median_prices (
            cargo_type, route_distance_km, region, currency,
            materials_median, labor_median, total_median, data_points_count
        ) VALUES (
            rec.cargo_type, rec.route_distance_km, rec.region, rec.currency,
            median_materials, median_labor, median_total, data_points
        );
    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- Создание представления для удобного доступа к актуальным медианным ценам
CREATE OR REPLACE VIEW current_median_prices AS
SELECT DISTINCT ON (cargo_type, route_distance_km, region, currency)
    cargo_type,
    route_distance_km,
    region,
    currency,
    materials_median,
    labor_median,
    total_median,
    data_points_count,
    calculated_at
FROM median_prices
ORDER BY cargo_type, route_distance_km, region, currency, calculated_at DESC;

-- Создание функции для получения медианной цены по заказу
CREATE OR REPLACE FUNCTION get_order_median_price(
    p_order_id VARCHAR(50)
) RETURNS DECIMAL(15,2) AS $$
DECLARE
    median_price DECIMAL(15,2);
    order_data RECORD;
BEGIN
    -- Получаем данные о заказе
    SELECT cargo_type, route_distance_km, region, currency
    INTO order_data
    FROM price_history
    WHERE order_id = p_order_id
    LIMIT 1;

    IF NOT FOUND THEN
        RETURN NULL;
    END IF;

    -- Получаем медианную цену
    SELECT total_median
    INTO median_price
    FROM current_median_prices
    WHERE cargo_type = order_data.cargo_type
        AND route_distance_km = order_data.route_distance_km
        AND region = order_data.region
        AND currency = order_data.currency;

    RETURN median_price;
END;
$$ LANGUAGE plpgsql;

-- Создание функции для добавления новой цены в историю и обновления медиан
CREATE OR REPLACE FUNCTION add_price_and_update_median(
    p_order_id VARCHAR(50),
    p_cargo_type VARCHAR(50),
    p_route_distance_km INTEGER,
    p_region VARCHAR(100),
    p_currency VARCHAR(3),
    p_materials_cost DECIMAL(15,2),
    p_labor_cost DECIMAL(15,2),
    p_total_cost DECIMAL(15,2)
) RETURNS VOID AS $$
BEGIN
    -- Добавляем новую запись в историю
    INSERT INTO price_history (
        order_id, cargo_type, route_distance_km, region, currency,
        materials_cost, labor_cost, total_cost
    ) VALUES (
        p_order_id, p_cargo_type, p_route_distance_km, p_region, p_currency,
        p_materials_cost, p_labor_cost, p_total_cost
    );

    -- Обновляем медианные цены (выполняем асинхронно)
    -- В реальной системе это можно делать по расписанию или через триггер
    PERFORM update_median_prices();
END;
$$ LANGUAGE plpgsql;

-- Создание функции для получения статистики по ценам
CREATE OR REPLACE FUNCTION get_price_statistics(
    p_cargo_type VARCHAR(50),
    p_route_distance_km INTEGER,
    p_region VARCHAR(100),
    p_currency VARCHAR(3)
) RETURNS TABLE(
    min_price DECIMAL(15,2),
    max_price DECIMAL(15,2),
    avg_price DECIMAL(15,2),
    median_price DECIMAL(15,2),
    std_dev DECIMAL(15,2),
    count_records INTEGER
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        MIN(total_cost),
        MAX(total_cost),
        AVG(total_cost),
        PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY total_cost),
        STDDEV(total_cost),
        COUNT(*)
    FROM price_history
    WHERE cargo_type = p_cargo_type
        AND route_distance_km = p_route_distance_km
        AND region = p_region
        AND currency = p_currency
        AND submitted_at >= NOW() - INTERVAL '1 year';
END;
$$ LANGUAGE plpgsql;

-- Создание функции для очистки устаревших данных
CREATE OR REPLACE FUNCTION cleanup_old_price_data(
    p_retention_days INTEGER DEFAULT 365
) RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    -- Удаляем старые записи из истории цен
    DELETE FROM price_history
    WHERE submitted_at < NOW() - (p_retention_days || ' days')::INTERVAL;
    
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    
    -- Очищаем устаревшие медианные цены
    DELETE FROM median_prices
    WHERE calculated_at < NOW() - (p_retention_days || ' days')::INTERVAL;
    
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

-- Создание комментариев к таблицам и столбцам
COMMENT ON TABLE median_prices IS 'Таблица для хранения рассчитанных медианных цен';
COMMENT ON TABLE price_history IS 'Таблица для хранения исторических данных о ценах';

COMMENT ON COLUMN median_prices.cargo_type IS 'Тип груза (ADR, REF, GENERAL и т.д.)';
COMMENT ON COLUMN median_prices.route_distance_km IS 'Расстояние маршрута в километрах';
COMMENT ON COLUMN median_prices.region IS 'Географический регион';
COMMENT ON COLUMN median_prices.currency IS 'Валюта цен';
COMMENT ON COLUMN median_prices.materials_median IS 'Медианная цена материалов';
COMMENT ON COLUMN median_prices.labor_median IS 'Медианная цена работ';
COMMENT ON COLUMN median_prices.total_median IS 'Общая медианная цена';
COMMENT ON COLUMN median_prices.data_points_count IS 'Количество точек данных для расчета';
COMMENT ON COLUMN median_prices.calculated_at IS 'Дата и время расчета медианы';

COMMENT ON COLUMN price_history.order_id IS 'Идентификатор заказа';
COMMENT ON COLUMN price_history.materials_cost IS 'Стоимость материалов';
COMMENT ON COLUMN price_history.labor_cost IS 'Стоимость работ';
COMMENT ON COLUMN price_history.total_cost IS 'Общая стоимость';
COMMENT ON COLUMN price_history.submitted_at IS 'Дата и время ввода цены';

-- Создание функции для получения медианы с кэшированием
CREATE OR REPLACE FUNCTION get_cached_median_price(
    p_cargo_type VARCHAR(50),
    p_route_distance_km INTEGER,
    p_region VARCHAR(100),
    p_currency VARCHAR(3)
) RETURNS DECIMAL(15,2) AS $$
DECLARE
    median_price DECIMAL(15,2);
    cache_age INTERVAL;
BEGIN
    -- Проверяем, есть ли актуальная медиана в кэше
    SELECT total_median, NOW() - calculated_at
    INTO median_price, cache_age
    FROM current_median_prices
    WHERE cargo_type = p_cargo_type
        AND route_distance_km = p_route_distance_km
        AND region = p_region
        AND currency = p_currency;

    -- Если кэш старше 1 часа, пересчитываем
    IF median_price IS NULL OR cache_age > INTERVAL '1 hour' THEN
        -- Пересчитываем медиану
        SELECT PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY total_cost)
        INTO median_price
        FROM price_history
        WHERE cargo_type = p_cargo_type
            AND route_distance_km = p_route_distance_km
            AND region = p_region
            AND currency = p_currency
            AND submitted_at >= NOW() - INTERVAL '1 year';

        -- Обновляем кэш
        INSERT INTO median_prices (
            cargo_type, route_distance_km, region, currency,
            materials_median, labor_median, total_median, data_points_count
        ) VALUES (
            p_cargo_type, p_route_distance_km, p_region, p_currency,
            NULL, NULL, median_price, 0
        ) ON CONFLICT DO NOTHING;
    END IF;

    RETURN median_price;
END;
$$ LANGUAGE plpgsql; <environment_details>
# Visual Studio Code - Insiders Visible Files
scm-service/src/main/kotlin/com/internal/services/IdempotencyService.kt

# Visual Studio Code - Insiders Open Tabs
scm-service/src/main/kotlin/com/internal/services/ManualCostValidationService.kt
scm-service/src/main/kotlin/com/internal/services/DynamicGeofencingService.kt
scm-service/src/main/kotlin/com/internal/services/ComplianceOraclesService.kt
scm-service/src/main/kotlin/com/internal/services/EvidencePackageGenerator.kt
scm-service/src/main/kotlin/com/internal/services/TrustScoreService.kt
scm-service/src/main/kotlin/com/internal/services/IdempotencyService.kt

# Current Time
2/3/2026, 11:32:20 PM (Europe/Kiev, UTC+2:00)

# Context Window Usage
125,121 / 256K tokens used (49%)

# Current Mode
ACT MODE
</environment_details>