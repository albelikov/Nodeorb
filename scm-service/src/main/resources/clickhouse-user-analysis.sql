-- ClickHouse SQL Query for User Behavior Analysis
-- Analyzes last 20 MANUAL_COST_ENTRY events for threshold gaming detection

-- Query to get user behavior statistics for fraud detection
WITH user_events AS (
    SELECT 
        order_id,
        risk_verdict,
        input_data,
        current_hash,
        prev_hash,
        event_type,
        event_time,
        -- Extract deviation from input_data (assuming format contains deviation)
        toFloat64OrNull(input_data) as deviation
    FROM scm_audit_log
    WHERE order_id LIKE '{user_id}%' 
        AND event_type = 'VALIDATION'
        AND event_time >= now() - INTERVAL 90 DAY
    ORDER BY event_time DESC
    LIMIT 20
),
user_stats AS (
    SELECT 
        COUNT(*) as total_events,
        AVG(deviation) as mean_deviation,
        -- Count events in threshold gaming range (35% - 39.9%)
        SUM(CASE WHEN deviation >= 0.35 AND deviation <= 0.399 THEN 1 ELSE 0 END) as in_range_count,
        -- Calculate percentage in range
        (SUM(CASE WHEN deviation >= 0.35 AND deviation <= 0.399 THEN 1 ELSE 0 END) * 100.0 / COUNT(*)) as percentage_in_range,
        -- Calculate standard deviation for Z-score
        stddevPop(deviation) as std_deviation,
        avg(deviation) as mean_dev
    FROM user_events
    WHERE deviation IS NOT NULL
),
current_analysis AS (
    SELECT 
        '{user_id}' as user_id,
        total_events,
        mean_deviation,
        in_range_count,
        percentage_in_range,
        std_deviation,
        mean_dev,
        -- Determine if threshold gaming detected
        CASE 
            WHEN percentage_in_range >= 70 THEN 'SUSPICIOUS_BEHAVIOR'
            WHEN total_events >= 3 THEN 'ANALYSIS_REQUIRED'
            ELSE 'INSUFFICIENT_DATA'
        END as risk_level
    FROM user_stats
)
SELECT 
    user_id,
    total_events,
    round(mean_deviation, 4) as mean_deviation,
    in_range_count,
    round(percentage_in_range, 2) as percentage_in_range,
    round(std_deviation, 4) as std_deviation,
    risk_level,
    -- Alert conditions
    CASE 
        WHEN percentage_in_range >= 70 THEN 'THRESHOLD_GAMING_DETECTED'
        WHEN total_events >= 3 AND std_deviation > 0 THEN 'Z_SCORE_ANALYSIS_REQUIRED'
        ELSE 'NORMAL'
    END as alert_condition
FROM current_analysis;

-- Query to calculate Z-score for current deviation
-- This would be used in real-time analysis
WITH user_history AS (
    SELECT 
        deviation
    FROM scm_audit_log
    WHERE order_id LIKE '{user_id}%' 
        AND event_type = 'VALIDATION'
        AND deviation IS NOT NULL
    ORDER BY event_time DESC
    LIMIT 20
),
stats AS (
    SELECT 
        avg(deviation) as mean_dev,
        stddevPop(deviation) as std_dev,
        count(*) as sample_size
    FROM user_history
)
SELECT 
    '{user_id}' as user_id,
    '{current_deviation}' as current_deviation,
    mean_dev,
    std_dev,
    sample_size,
    -- Calculate Z-score
    CASE 
        WHEN std_dev = 0 OR sample_size < 3 THEN 0
        ELSE ({current_deviation} - mean_dev) / std_dev
    END as z_score,
    -- Determine anomaly
    CASE 
        WHEN std_dev = 0 OR sample_size < 3 THEN 'INSUFFICIENT_DATA'
        WHEN (({current_deviation} - mean_dev) / std_dev) > 2.5 THEN 'ANOMALY_DETECTED'
        ELSE 'NORMAL'
    END as anomaly_status
FROM stats;

-- Query to detect threshold gaming patterns across all users
-- For system-wide monitoring
WITH user_patterns AS (
    SELECT 
        substring(order_id, 1, position(order_id, '_') - 1) as user_id,
        COUNT(*) as total_events,
        SUM(CASE WHEN toFloat64OrNull(input_data) >= 0.35 AND toFloat64OrNull(input_data) <= 0.399 THEN 1 ELSE 0 END) as in_range_count,
        (SUM(CASE WHEN toFloat64OrNull(input_data) >= 0.35 AND toFloat64OrNull(input_data) <= 0.399 THEN 1 ELSE 0 END) * 100.0 / COUNT(*)) as percentage_in_range
    FROM scm_audit_log
    WHERE event_type = 'VALIDATION'
        AND event_time >= now() - INTERVAL 30 DAY
        AND input_data IS NOT NULL
    GROUP BY substring(order_id, 1, position(order_id, '_') - 1)
    HAVING COUNT(*) >= 5
)
SELECT 
    user_id,
    total_events,
    in_range_count,
    round(percentage_in_range, 2) as percentage_in_range,
    CASE 
        WHEN percentage_in_range >= 70 THEN 'HIGH_RISK_THRESHOLD_GAMING'
        WHEN percentage_in_range >= 50 THEN 'MEDIUM_RISK_PATTERN'
        ELSE 'LOW_RISK'
    END as risk_category
FROM user_patterns
WHERE percentage_in_range >= 30
ORDER BY percentage_in_range DESC, total_events DESC;