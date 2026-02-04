package com.nodeorb.freight.marketplace.controller

import com.nodeorb.freight.marketplace.dto.*
import com.nodeorb.freight.marketplace.entity.*
import com.nodeorb.freight.marketplace.service.OrderService
import com.nodeorb.freight.marketplace.service.ScmIntegrationService
import com.nodeorb.freight.marketplace.service.TrustTokenService
import com.nodeorb.freight.marketplace.service.ComplianceService
import com.nodeorb.freight.marketplace.repository.MasterOrderRepository
import com.nodeorb.freight.marketplace.repository.PartialOrderRepository
import com.nodeorb.freight.marketplace.repository.UserProfileRepository
import com.nodeorb.freight.marketplace.repository.BidRepository
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

/**
 * Контроллер для Carrier UI
 * Обеспечивает функциональность подачи ставок с возможностью выбора объема через слайдер
 */
@RestController
@RequestMapping("/api/v1/carrier")
class CarrierController(
    private val orderService: OrderService,
    private val scmIntegrationService: ScmIntegrationService,
    private val trustTokenService: TrustTokenService,
    private val complianceService: ComplianceService,
    private val masterOrderRepository: MasterOrderRepository,
    private val partialOrderRepository: PartialOrderRepository,
    private val userProfileRepository: UserProfileRepository,
    private val bidRepository: BidRepository
) {

    /**
     * Получение списка доступных для ставок заказов
     */
    @GetMapping("/orders")
    fun getAvailableOrders(
        @AuthenticationPrincipal jwt: Jwt,
        pageable: Pageable
    ): ResponseEntity<Page<MasterOrderEntity>> {
        val carrierId = UUID.fromString(jwt.subject)
        
        // Проверяем профиль перевозчика
        val userProfile = userProfileRepository.findById(carrierId)
            .orElseThrow { RuntimeException("Carrier profile not found") }

        // Получаем активные заказы
        val orders = masterOrderRepository.findByStatusIn(
            listOf(MasterOrderStatus.OPEN, MasterOrderStatus.PARTIALLY_FILLED),
            pageable
        )

        return ResponseEntity.ok(orders)
    }

    /**
     * Получение прогресса заказа
     */
    @GetMapping("/orders/{orderId}/progress")
    fun getOrderProgress(
        @PathVariable orderId: UUID
    ): ResponseEntity<OrderProgressDto> {
        val progress = orderService.getOrderProgress(orderId)
        return ResponseEntity.ok(progress)
    }

    /**
     * Подача ставки с выбором объема (LTL)
     */
    @PostMapping("/orders/{orderId}/bids")
    fun placeBid(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable orderId: UUID,
        @Valid @RequestBody bidRequest: BidRequestDto
    ): ResponseEntity<BidResponseDto> {
        val carrierId = UUID.fromString(jwt.subject)
        
        // Проверяем доступность заказа
        val masterOrder = masterOrderRepository.findById(orderId)
            .orElseThrow { RuntimeException("Order not found") }

        if (masterOrder.status !in listOf(MasterOrderStatus.OPEN, MasterOrderStatus.PARTIALLY_FILLED)) {
            throw RuntimeException("Order is not available for bidding")
        }

        // Проверяем соответствие объема
        if (bidRequest.weight > masterOrder.remainingWeight || bidRequest.volume > masterOrder.remainingVolume) {
            throw RuntimeException("Requested volume exceeds available capacity")
        }

        // Проверяем минимальный процент загрузки
        val requestedPercentage = calculatePercentage(bidRequest.weight, masterOrder.totalWeight)
        if (requestedPercentage < masterOrder.minLoadPercentage) {
            throw RuntimeException("Requested volume is below minimum load percentage")
        }

        // Проверяем соответствие перевозчика
        val complianceResult = complianceService.checkCompliancePassport(
            carrierId = carrierId,
            masterOrderId = orderId,
            cargoType = masterOrder.cargoType.name,
            route = RouteInfo(
                pickupLocation = LocationInfo(
                    latitude = masterOrder.pickupLocation.y,
                    longitude = masterOrder.pickupLocation.x,
                    country = extractCountryFromAddress(masterOrder.pickupAddress),
                    city = extractCityFromAddress(masterOrder.pickupAddress)
                ),
                deliveryLocation = LocationInfo(
                    latitude = masterOrder.deliveryLocation.y,
                    longitude = masterOrder.deliveryLocation.x,
                    country = extractCountryFromAddress(masterOrder.deliveryAddress),
                    city = extractCityFromAddress(masterOrder.deliveryAddress)
                )
            )
        )

        if (complianceResult.complianceStatus != "COMPLIANT") {
            throw RuntimeException("Carrier does not meet compliance requirements")
        }

        // Создаем частичный заказ
        val partialOrder = createPartialOrder(masterOrder, bidRequest, carrierId)
        
        // Создаем заявку
        val bid = createBid(carrierId, partialOrder, bidRequest)
        
        // Генерируем Trust Token
        val trustToken = trustTokenService.generateTrustToken(
            carrierId = carrierId,
            masterOrderId = orderId,
            bidId = bid.id!!,
            complianceStatus = complianceResult.complianceStatus,
            securityLevel = SecurityLevel.valueOf(complianceResult.securityClearance),
            riskScore = complianceResult.riskScore,
            permissions = listOf("BID_SUBMISSION", "ORDER_ACCESS")
        )

        // Отправляем событие в SCM
        val bidEvent = BidPlacementEvent(
            bidId = bid.id!!,
            carrierId = carrierId,
            masterOrderId = orderId,
            amount = bidRequest.amount.toDouble(),
            proposedDeliveryDate = bidRequest.proposedDeliveryDate.toString(),
            notes = bidRequest.notes,
            route = RouteInfo(
                pickupLocation = LocationInfo(
                    latitude = masterOrder.pickupLocation.y,
                    longitude = masterOrder.pickupLocation.x,
                    country = extractCountryFromAddress(masterOrder.pickupAddress)
                ),
                deliveryLocation = LocationInfo(
                    latitude = masterOrder.deliveryLocation.y,
                    longitude = masterOrder.deliveryLocation.x,
                    country = extractCountryFromAddress(masterOrder.deliveryAddress)
                )
            ),
            cargoDetails = CargoDetails(
                cargoType = masterOrder.cargoType.name,
                weight = bidRequest.weight.toDouble(),
                volume = bidRequest.volume.toDouble(),
                hazardous = bidRequest.hazardous,
                temperatureControlled = bidRequest.temperatureControlled
            )
        )

        // Отправляем событие в Kafka для SCM проверки
        // (в реальной системе здесь будет вызов KafkaTemplate)

        return ResponseEntity.ok(
            BidResponseDto(
                bidId = bid.id!!,
                partialOrderId = partialOrder.id!!,
                trustToken = trustToken.token,
                complianceStatus = complianceResult.complianceStatus,
                riskScore = complianceResult.riskScore,
                message = "Bid placed successfully"
            )
        )
    }

    /**
     * Получение ставок перевозчика
     */
    @GetMapping("/bids")
    fun getMyBids(
        @AuthenticationPrincipal jwt: Jwt,
        pageable: Pageable
    ): ResponseEntity<Page<BidEntity>> {
        val carrierId = UUID.fromString(jwt.subject)
        
        val bids = bidRepository.findByCarrierId(carrierId, pageable)
        return ResponseEntity.ok(bids)
    }

    /**
     * Получение Trust Token
     */
    @GetMapping("/trust-token")
    fun getTrustToken(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestParam orderId: UUID
    ): ResponseEntity<TrustTokenInfo> {
        val carrierId = UUID.fromString(jwt.subject)
        
        // Проверяем наличие активных токенов
        val activeTokens = trustTokenService.getActiveTokensForCarrier(carrierId)
        val token = activeTokens.find { it.masterOrderId == orderId }
        
        return if (token != null) {
            ResponseEntity.ok(
                TrustTokenInfo(
                    token = token.token,
                    carrierId = token.carrierId,
                    expiresAt = token.expiresAt,
                    permissions = token.permissions,
                    metadata = token.metadata
                )
            )
        } else {
            ResponseEntity.notFound().build()
        }
    }

    /**
     * Проверка валидности Trust Token
     */
    @PostMapping("/validate-token")
    fun validateToken(@RequestBody tokenRequest: TokenValidationRequest): ResponseEntity<TokenValidationResult> {
        val result = trustTokenService.validateTrustToken(tokenRequest.token)
        
        return when (result) {
            is TrustTokenService.TokenValidationResult.VALID -> {
                ResponseEntity.ok(
                    TokenValidationResult(
                        isValid = true,
                        tokenInfo = TokenInfo(
                            tokenId = result.tokenData.tokenId,
                            carrierId = result.tokenData.carrierId,
                            masterOrderId = result.tokenData.masterOrderId,
                            bidId = result.tokenData.bidId,
                            complianceStatus = result.tokenData.complianceStatus,
                            securityLevel = result.tokenData.securityLevel.name,
                            riskScore = result.tokenData.riskScore,
                            permissions = result.tokenData.permissions,
                            metadata = result.tokenData.metadata,
                            expiresAt = result.tokenData.expiresAt,
                            createdAt = result.tokenData.createdAt.toEpochMilli()
                        )
                    )
                )
            }
            is TrustTokenService.TokenValidationResult.EXPIRED -> {
                ResponseEntity.ok(
                    TokenValidationResult(
                        isValid = false,
                        errorMessage = result.reason
                    )
                )
            }
            is TrustTokenService.TokenValidationResult.INVALID -> {
                ResponseEntity.ok(
                    TokenValidationResult(
                        isValid = false,
                        errorMessage = result.reason
                    )
                )
            }
        }
    }

    /**
     * Отзыв Trust Token
     */
    @PostMapping("/revoke-token")
    fun revokeToken(@RequestBody tokenRequest: TokenRevocationRequest): ResponseEntity<String> {
        val success = trustTokenService.revokeTrustToken(tokenRequest.token)
        
        return if (success) {
            ResponseEntity.ok("Token revoked successfully")
        } else {
            ResponseEntity.badRequest().body("Failed to revoke token")
        }
    }

    // Вспомогательные методы

    private fun calculatePercentage(requestedWeight: BigDecimal, totalWeight: BigDecimal): Double {
        return (requestedWeight.toDouble() / totalWeight.toDouble())
    }

    private fun createPartialOrder(
        masterOrder: MasterOrderEntity,
        bidRequest: BidRequestDto,
        carrierId: UUID
    ): PartialOrderEntity {
        val partialOrder = PartialOrderEntity(
            masterOrder = masterOrder,
            weight = bidRequest.weight,
            volume = bidRequest.volume,
            percentage = calculatePercentage(bidRequest.weight, masterOrder.totalWeight),
            status = PartialOrderStatus.AVAILABLE
        )

        return partialOrderRepository.save(partialOrder)
    }

    private fun createBid(
        carrierId: UUID,
        partialOrder: PartialOrderEntity,
        bidRequest: BidRequestDto
    ): BidEntity {
        val bid = BidEntity(
            carrierId = carrierId,
            amount = bidRequest.amount,
            proposedDeliveryDate = bidRequest.proposedDeliveryDate,
            notes = bidRequest.notes,
            status = BidStatus.PENDING
        )

        return bidRepository.save(bid)
    }

    private fun extractCountryFromAddress(address: String): String {
        // Упрощенная логика извлечения страны из адреса
        return "UA" // Для примера
    }

    private fun extractCityFromAddress(address: String): String {
        // Упрощенная логика извлечения города из адреса
        return address.split(",")[0] // Для примера
    }
}

/**
 * DTO для запроса на подачу ставки
 */
data class BidRequestDto(
    val weight: BigDecimal,
    val volume: BigDecimal,
    val amount: BigDecimal,
    val proposedDeliveryDate: LocalDateTime,
    val notes: String? = null,
    val hazardous: Boolean = false,
    val temperatureControlled: Boolean = false
)

/**
 * DTO для ответа на подачу ставки
 */
data class BidResponseDto(
    val bidId: UUID,
    val partialOrderId: UUID,
    val trustToken: String,
    val complianceStatus: String,
    val riskScore: Double,
    val message: String
)

/**
 * DTO для проверки токена
 */
data class TokenValidationRequest(
    val token: String
)

/**
 * DTO для результата проверки токена
 */
data class TokenValidationResult(
    val isValid: Boolean,
    val tokenInfo: TokenInfo? = null,
    val errorMessage: String? = null
)

/**
 * DTO для отзыва токена
 */
data class TokenRevocationRequest(
    val token: String
)