package com.autonomous.data.entities

import java.time.Instant
import javax.persistence.*

@Entity
@Table(name = "weather_conditions")
data class WeatherCondition(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val location: GeoPoint,
    val timestamp: Instant,

    val temperature: Double,
    val windSpeed: Double,
    val windDirection: Double,
    val visibility: Double,
    val precipitation: Double,
    val humidity: Double,
    val pressure: Double,
    val cloudCover: Double,

    val conditions: WeatherConditionType,
    val severity: WeatherSeverity,

    val source: String,
    val forecastHours: Int = 0
)

enum class WeatherConditionType {
    CLEAR,
    PARTLY_CLOUDY,
    CLOUDY,
    RAIN,
    SNOW,
    STORM,
    FOG,
    HAIL,
    WIND
}

enum class WeatherSeverity {
    OPTIMAL,
    GOOD,
    MODERATE,
    POOR,
    CRITICAL
}