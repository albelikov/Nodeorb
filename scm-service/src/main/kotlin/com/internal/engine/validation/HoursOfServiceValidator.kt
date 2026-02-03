package com.internal.engine.validation

import com.internal.integrations.SecurityEventBus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Валидатор часов работы (ELD - Electronic Logging Device)
 * Контроль режима труда и отдыха водителей
 */
@Service
class HoursOfServiceValidator(
    private val securityEventBus: SecurityEventBus
) {

    companion object {
        // Регламент США (FMCSA)
        private const val MAX_DRIVING_HOURS_DAILY = 11.0
        private const val MAX_ON_DEMAND_HOURS_DAILY = 14.0
        private const val REQUIRED_BREAK_HOURS = 3.0
        private const val MAX_DRIVING_HOURS_WEEKLY = 70.0
        private const val REQUIRED_WEEKLY_REST_HOURS = 34.0
        
        // Регламент ЕС
        private const val EU_MAX_DRIVING_HOURS_DAILY = 9.0
        private const val EU_MAX_DRIVING_HOURS_WEEKLY = 56.0
        private const val EU_REQUIRED_DAILY_REST_HOURS = 9.0
        private const val EU_REQUIRED_WEEKLY_REST_HOURS = 45.0
    }

    /**
     * Проверка соответствия Hours of Service
     */
    @Transactional
    fun validateHoursOfService(
        driverId: String,
        vehicleId: String,
        currentTime: Instant,
        region: String = "US"
    ): HoursOfServiceResult {
        val drivingHours = getDrivingHours(driverId, currentTime, region)
        val onDemandHours = getOnDemandHours(driverId, currentTime, region)
        val lastBreak = getLastBreak(driverId, currentTime, region)
        val weeklyHours = getWeeklyHours(driverId, currentTime, region)

        val violations = mutableListOf<String>()
        var complianceStatus = "COMPLIANT"

        // Проверка ежедневного лимита вождения
        val maxDailyHours = if (region == "EU") EU_MAX_DRIVING_HOURS_DAILY else MAX_DRIVING_HOURS_DAILY
        if (drivingHours >= maxDailyHours) {
            violations.add("Daily driving limit exceeded")
            complianceStatus = "VIOLATION"
        }

        // Проверка общего времени в наряде
        val maxOnDemandHours = if (region == "EU") maxDailyHours + 2 else MAX_ON_DEMAND_HOURS_DAILY
        if (onDemandHours >= maxOnDemandHours) {
            violations.add("Daily on-demand limit exceeded")
            complianceStatus = "VIOLATION"
        }

        // Проверка перерыва
        val requiredBreakHours = if (region == "EU") 4.5 else REQUIRED_BREAK_HOURS
        if (lastBreak < requiredBreakHours) {
            violations.add("Required break not taken")
            complianceStatus = "VIOLATION"
        }

        // Проверка недельного лимита
        val maxWeeklyHours = if (region == "EU") EU_MAX_DRIVING_HOURS_WEEKLY else MAX_DRIVING_HOURS_WEEKLY
        if (weeklyHours >= maxWeeklyHours) {
            violations.add("Weekly driving limit exceeded")
            complianceStatus = "VIOLATION"
        }

        // Проверка недельного отдыха
        val requiredWeeklyRest = if (region == "EU") EU_REQUIRED_WEEKLY_REST_HOURS else REQUIRED_WEEKLY_REST_HOURS
        val lastWeeklyRest = getLastWeeklyRest(driverId, currentTime, region)
        if (lastWeeklyRest < requiredWeeklyRest) {
            violations.add("Required weekly rest not taken")
            complianceStatus = "VIOLATION"
        }

        val result = HoursOfServiceResult(
            driverId = driverId,
            vehicleId = vehicleId,
            complianceStatus = complianceStatus,
            violations = violations,
            remainingDrivingHours = maxDailyHours - drivingHours,
            remainingOnDemandHours = maxOnDemandHours - onDemandHours,
            timeToNextBreak = calculateTimeToNextBreak(lastBreak, requiredBreakHours),
            weeklyHours = weeklyHours,
            timestamp = currentTime
        )

        // Отправляем событие при нарушении
        if (complianceStatus == "VIOLATION") {
            securityEventBus.triggerHoursOfServiceViolation(
                driverId = driverId,
                vehicleId = vehicleId,
                remainingHours = result.remainingDrivingHours.toInt(),
                violationReason = violations.joinToString(", ")
            )
        }

        return result
    }

    /**
     * Проверка возможности начала новой смены
     */
    fun canStartShift(
        driverId: String,
        currentTime: Instant,
        region: String = "US"
    ): ShiftEligibilityResult {
        val lastShiftEnd = getLastShiftEnd(driverId, currentTime)
        val timeSinceLastShift = ChronoUnit.HOURS.between(lastShiftEnd, currentTime)

        val requiredRest = if (region == "EU") EU_REQUIRED_DAILY_REST_HOURS else 10.0
        val canStart = timeSinceLastShift >= requiredRest

        return ShiftEligibilityResult(
            canStart = canStart,
            timeUntilEligible = if (canStart) 0 else (requiredRest - timeSinceLastShift).toInt(),
            lastShiftEnd = lastShiftEnd,
            requiredRestHours = requiredRest
        )
    }

    /**
     * Проверка на усталость водителя
     */
    fun detectDriverFatigue(
        driverId: String,
        currentSpeed: Double,
        accelerationData: List<Double>,
        timeOfDay: Int
    ): FatigueDetectionResult {
        var fatigueScore = 0.0
        val factors = mutableListOf<String>()

        // Проверка времени суток (ночная усталость)
        if (timeOfDay in 22..6) {
            fatigueScore += 0.3
            factors.add("Night driving")
        }

        // Проверка по ускорению (характер вождения)
        val avgAcceleration = accelerationData.average()
        if (avgAcceleration < 0.1) { // Слишком плавное вождение
            fatigueScore += 0.4
            factors.add("Reduced acceleration")
        }

        // Проверка по скорости (слишком низкая или нестабильная)
        if (currentSpeed < 40.0) {
            fatigueScore += 0.2
            factors.add("Low speed")
        }

        val isFatigued = fatigueScore > 0.5

        return FatigueDetectionResult(
            isFatigued = isFatigued,
            fatigueScore = fatigueScore,
            contributingFactors = factors,
            recommendedAction = if (isFatigued) "STOP_AND_REST" else "CONTINUE_MONITORING"
        )
    }

    /**
     * Получение часов вождения за текущие сутки
     */
    private fun getDrivingHours(driverId: String, currentTime: Instant, region: String): Double {
        // В реальной системе здесь будет запрос к базе данных ELD
        // Пока заглушка
        return 6.5
    }

    /**
     * Получение общего времени в наряде
     */
    private fun getOnDemandHours(driverId: String, currentTime: Instant, region: String): Double {
        // В реальной системе здесь будет запрос к базе данных
        // Пока заглушка
        return 9.0
    }

    /**
     * Получение времени последнего перерыва
     */
    private fun getLastBreak(driverId: String, currentTime: Instant, region: String): Double {
        // В реальной системе здесь будет запрос к базе данных
        // Пока заглушка
        return 2.5
    }

    /**
     * Получение часов вождения за неделю
     */
    private fun getWeeklyHours(driverId: String, currentTime: Instant, region: String): Double {
        // В реальной системе здесь будет запрос к базе данных
        // Пока заглушка
        return 45.0
    }

    /**
     * Получение времени последнего недельного отдыха
     */
    private fun getLastWeeklyRest(driverId: String, currentTime: Instant, region: String): Double {
        // В реальной системе здесь будет запрос к базе данных
        // Пока заглушка
        return 40.0
    }

    /**
     * Получение времени окончания последней смены
     */
    private fun getLastShiftEnd(driverId: String, currentTime: Instant): Instant {
        // В реальной системе здесь будет запрос к базе данных
        // Пока заглушка
        return currentTime.minus(12, ChronoUnit.HOURS)
    }

    /**
     * Расчет времени до следующего перерыва
     */
    private fun calculateTimeToNextBreak(lastBreak: Double, requiredBreak: Double): Int {
        return if (lastBreak >= requiredBreak) 0 else (requiredBreak - lastBreak).toInt() * 60
    }
}

/**
 * Результат проверки Hours of Service
 */
data class HoursOfServiceResult(
    val driverId: String,
    val vehicleId: String,
    val complianceStatus: String,
    val violations: List<String>,
    val remainingDrivingHours: Double,
    val remainingOnDemandHours: Double,
    val timeToNextBreak: Int,
    val weeklyHours: Double,
    val timestamp: Instant
)

/**
 * Результат проверки возможности начала смены
 */
data class ShiftEligibilityResult(
    val canStart: Boolean,
    val timeUntilEligible: Int,
    val lastShiftEnd: Instant,
    val requiredRestHours: Double
)

/**
 * Результат детектирования усталости
 */
data class FatigueDetectionResult(
    val isFatigued: Boolean,
    val fatigueScore: Double,
    val contributingFactors: List<String>,
    val recommendedAction: String
)