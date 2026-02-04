package com.nodeorb.freight.marketplace.service

import com.nodeorb.freight.marketplace.dto.TrustTokenInfo
import com.nodeorb.freight.marketplace.entity.SecurityLevel
import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Instant
import java.util.*
import java.util.logging.Logger

/**
 * Сервис генерации и управления Trust Token
 * Реализует логику создания доверенных токенов для перевозчиков
 */
@Service
class TrustTokenService {

    companion object {
        private val logger = Logger.getLogger(TrustTokenService::class.java.name)
        private const val TOKEN_PREFIX = "TRUST_"
        private const val TOKEN_ALGORITHM = "SHA-256"
        private const val TOKEN_LENGTH = 64
    }

    private val secureRandom = SecureRandom()
    private val activeTokens = mutableMapOf<String, TokenData>()

    /**
     * Генерация Trust Token
     */
    fun generateTrustToken(
        carrierId: UUID,
        masterOrderId: UUID,
        bidId: UUID,
        complianceStatus: String,
        securityLevel: SecurityLevel,
        riskScore: Double,
        permissions: List<String>,
        metadata: Map<String, String> = emptyMap()
    ): TrustTokenInfo {
        logger.info("Generating trust token for carrier: $carrierId, order: $masterOrderId")

        val tokenId = generateTokenId()
        val expiresAt = calculateTokenExpiration(riskScore)
        val tokenValue = generateTokenValue(tokenId, carrierId, expiresAt)
        
        val tokenData = TokenData(
            tokenId = tokenId,
            tokenValue = tokenValue,
            carrierId = carrierId,
            masterOrderId = masterOrderId,
            bidId = bidId,
            complianceStatus = complianceStatus,
            securityLevel = securityLevel,
            riskScore = riskScore,
            permissions = permissions,
            metadata = metadata,
            expiresAt = expiresAt,
            createdAt = Instant.now()
        )

        // Сохраняем токен
        activeTokens[tokenValue] = tokenData

        logger.info("Trust token generated: $tokenId for carrier: $carrierId")

        return TrustTokenInfo(
            token = tokenValue,
            carrierId = carrierId,
            expiresAt = expiresAt,
            permissions = permissions,
            metadata = metadata
        )
    }

    /**
     * Проверка валидности Trust Token
     */
    fun validateTrustToken(token: String): TokenValidationResult {
        val tokenData = activeTokens[token] ?: return TokenValidationResult.INVALID("Token not found")

        return when {
            tokenData.expiresAt < Instant.now().toEpochMilli() -> {
                TokenValidationResult.EXPIRED("Token has expired")
            }
            tokenData.securityLevel == SecurityLevel.RESTRICTED && !hasRestrictedAccess(tokenData) -> {
                TokenValidationResult.INVALID("Insufficient security clearance")
            }
            else -> {
                TokenValidationResult.VALID(tokenData)
            }
        }
    }

    /**
     * Получение информации о токене
     */
    fun getTokenInfo(token: String): TokenInfo? {
        val tokenData = activeTokens[token] ?: return null

        return TokenInfo(
            tokenId = tokenData.tokenId,
            carrierId = tokenData.carrierId,
            masterOrderId = tokenData.masterOrderId,
            bidId = tokenData.bidId,
            complianceStatus = tokenData.complianceStatus,
            securityLevel = tokenData.securityLevel.name,
            riskScore = tokenData.riskScore,
            permissions = tokenData.permissions,
            metadata = tokenData.metadata,
            expiresAt = tokenData.expiresAt,
            createdAt = tokenData.createdAt.toEpochMilli()
        )
    }

    /**
     * Отзыв Trust Token
     */
    fun revokeTrustToken(token: String): Boolean {
        val removed = activeTokens.remove(token)
        if (removed != null) {
            logger.info("Trust token revoked: ${removed.tokenId}")
            return true
        }
        return false
    }

    /**
     * Отзыв всех токенов для перевозчика
     */
    fun revokeAllTokensForCarrier(carrierId: UUID): Int {
        val removedTokens = activeTokens.filter { (_, data) -> data.carrierId == carrierId }
        removedTokens.keys.forEach { activeTokens.remove(it) }
        
        logger.info("Revoked ${removedTokens.size} tokens for carrier: $carrierId")
        return removedTokens.size
    }

    /**
     * Получение активных токенов для перевозчика
     */
    fun getActiveTokensForCarrier(carrierId: UUID): List<TokenInfo> {
        return activeTokens.values
            .filter { it.carrierId == carrierId && it.expiresAt > Instant.now().toEpochMilli() }
            .map { tokenData ->
                TokenInfo(
                    tokenId = tokenData.tokenId,
                    carrierId = tokenData.carrierId,
                    masterOrderId = tokenData.masterOrderId,
                    bidId = tokenData.bidId,
                    complianceStatus = tokenData.complianceStatus,
                    securityLevel = tokenData.securityLevel.name,
                    riskScore = tokenData.riskScore,
                    permissions = tokenData.permissions,
                    metadata = tokenData.metadata,
                    expiresAt = tokenData.expiresAt,
                    createdAt = tokenData.createdAt.toEpochMilli()
                )
            }
    }

    /**
     * Очистка просроченных токенов
     */
    fun cleanupExpiredTokens(): Int {
        val now = Instant.now().toEpochMilli()
        val expiredTokens = activeTokens.filter { (_, data) -> data.expiresAt < now }
        
        expiredTokens.keys.forEach { activeTokens.remove(it) }
        
        if (expiredTokens.isNotEmpty()) {
            logger.info("Cleaned up ${expiredTokens.size} expired tokens")
        }
        
        return expiredTokens.size
    }

    /**
     * Проверка наличия расширенного доступа
     */
    private fun hasRestrictedAccess(tokenData: TokenData): Boolean {
        // В реальной системе здесь будет проверка дополнительных факторов
        // Например: двухфакторная аутентификация, биометрия и т.д.
        return tokenData.riskScore < 0.5 // Упрощенная проверка
    }

    /**
     * Генерация идентификатора токена
     */
    private fun generateTokenId(): String {
        val randomBytes = ByteArray(16)
        secureRandom.nextBytes(randomBytes)
        return TOKEN_PREFIX + bytesToHex(randomBytes)
    }

    /**
     * Генерация значения токена
     */
    private fun generateTokenValue(tokenId: String, carrierId: UUID, expiresAt: Long): String {
        val data = "$tokenId:$carrierId:$expiresAt:${System.currentTimeMillis()}"
        val digest = MessageDigest.getInstance(TOKEN_ALGORITHM)
        val hash = digest.digest(data.toByteArray())
        return bytesToHex(hash).substring(0, TOKEN_LENGTH)
    }

    /**
     * Расчет времени истечения токена на основе рискового балла
     */
    private fun calculateTokenExpiration(riskScore: Double): Long {
        val baseExpiration = 24 * 60 * 60 * 1000L // 24 часа
        val riskMultiplier = when {
            riskScore < 0.3 -> 1.5 // Низкий риск - дольше
            riskScore < 0.7 -> 1.0 // Средний риск - стандартно
            else -> 0.5 // Высокий риск - короче
        }
        
        return Instant.now().toEpochMilli() + (baseExpiration * riskMultiplier).toLong()
    }

    /**
     * Конвертация байтов в hex-строку
     */
    private fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // Вспомогательные классы
    private data class TokenData(
        val tokenId: String,
        val tokenValue: String,
        val carrierId: UUID,
        val masterOrderId: UUID,
        val bidId: UUID,
        val complianceStatus: String,
        val securityLevel: SecurityLevel,
        val riskScore: Double,
        val permissions: List<String>,
        val metadata: Map<String, String>,
        val expiresAt: Long,
        val createdAt: Instant
    )

    data class TokenInfo(
        val tokenId: String,
        val carrierId: UUID,
        val masterOrderId: UUID,
        val bidId: UUID,
        val complianceStatus: String,
        val securityLevel: String,
        val riskScore: Double,
        val permissions: List<String>,
        val metadata: Map<String, String>,
        val expiresAt: Long,
        val createdAt: Long
    )

    sealed class TokenValidationResult {
        data class VALID(val tokenData: TokenData) : TokenValidationResult()
        data class EXPIRED(val reason: String) : TokenValidationResult()
        data class INVALID(val reason: String) : TokenValidationResult()
    }
}