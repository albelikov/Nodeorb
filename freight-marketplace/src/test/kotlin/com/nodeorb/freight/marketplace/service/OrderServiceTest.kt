package com.nodeorb.freight.marketplace.service

import com.nodeorb.freight.marketplace.dto.*
import com.nodeorb.freight.marketplace.entity.*
import com.nodeorb.freight.marketplace.repository.*
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.kafka.core.KafkaTemplate
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(MockKExtension::class)
class OrderServiceTest {

    @MockK
    private lateinit var masterOrderRepository: MasterOrderRepository

    @MockK
    private lateinit var partialOrderRepository: PartialOrderRepository

    @MockK
    private lateinit var bidRepository: BidRepository

    @MockK
    private lateinit var userProfileRepository: UserProfileRepository

    @MockK
    private lateinit var scmSnapshotRepository: ScmSnapshotRepository

    @MockK
    private lateinit var kafkaTemplate: KafkaTemplate<String, Any>

    @MockK
    private lateinit var scmIntegrationService: ScmIntegrationService

    @InjectMockKs
    private lateinit var orderService: OrderService

    private lateinit var masterOrder: MasterOrderEntity
    private lateinit var partialOrders: List<PartialOrderEntity>
    private lateinit var shipperId: UUID

    @BeforeEach
    fun setUp() {
        shipperId = UUID.randomUUID()
        
        masterOrder = MasterOrderEntity(
            shipperId = shipperId,
            title = "Test Order",
            cargoType = CargoType.GENERAL,
            totalWeight = BigDecimal("1000.00"),
            totalVolume = BigDecimal("50.00"),
            remainingWeight = BigDecimal("1000.00"),
            remainingVolume = BigDecimal("50.00"),
            pickupLocation = createMockPoint(50.4501, 30.5234),
            deliveryLocation = createMockPoint(49.8397, 24.0297),
            pickupAddress = "Kyiv, Ukraine",
            deliveryAddress = "Lviv, Ukraine",
            requiredDeliveryDate = LocalDateTime.now().plusDays(7),
            maxBidAmount = BigDecimal("5000.00"),
            isLtlEnabled = true,
            minLoadPercentage = 0.8
        )

        partialOrders = listOf(
            PartialOrderEntity(
                masterOrder = masterOrder,
                weight = BigDecimal("400.00"),
                volume = BigDecimal("20.00"),
                percentage = 0.4,
                status = PartialOrderStatus.AVAILABLE
            ),
            PartialOrderEntity(
                masterOrder = masterOrder,
                weight = BigDecimal("300.00"),
                volume = BigDecimal("15.00"),
                percentage = 0.3,
                status = PartialOrderStatus.AVAILABLE
            ),
            PartialOrderEntity(
                masterOrder = masterOrder,
                weight = BigDecimal("300.00"),
                volume = BigDecimal("15.00"),
                percentage = 0.3,
                status = PartialOrderStatus.AVAILABLE
            )
        )
    }

    @Test
    fun `should create master order with partial orders`() {
        // Arrange
        every { masterOrderRepository.save(any()) } returns masterOrder
        every { partialOrderRepository.save(any()) } answers { firstArg() }
        every { kafkaTemplate.send(any(), any()) } returns mockk()

        // Act
        val result = orderService.createMasterOrder(
            shipperId = shipperId,
            title = "Test Order",
            description = "Test description",
            cargoType = CargoType.GENERAL,
            totalWeight = BigDecimal("1000.00"),
            totalVolume = BigDecimal("50.00"),
            pickupLocation = createMockPoint(50.4501, 30.5234),
            deliveryLocation = createMockPoint(49.8397, 24.0297),
            pickupAddress = "Kyiv, Ukraine",
            deliveryAddress = "Lviv, Ukraine",
            requiredDeliveryDate = LocalDateTime.now().plusDays(7),
            maxBidAmount = BigDecimal("5000.00")
        )

        // Assert
        assertEquals(shipperId, result.shipperId)
        assertEquals("Test Order", result.title)
        verify { masterOrderRepository.save(any()) }
        verify { kafkaTemplate.send("order.created", any()) }
    }

    @Test
    fun `should get order progress correctly`() {
        // Arrange
        val masterOrderId = UUID.randomUUID()
        masterOrder.id = masterOrderId
        
        every { masterOrderRepository.findById(masterOrderId) } returns Optional.of(masterOrder)
        every { partialOrderRepository.findByMasterOrderId(masterOrderId) } returns partialOrders

        // Act
        val progress = orderService.getOrderProgress(masterOrderId)

        // Assert
        assertEquals(masterOrderId, progress.masterOrderId)
        assertEquals(BigDecimal("1000.00"), progress.totalWeight)
        assertEquals(BigDecimal("50.00"), progress.totalVolume)
        assertEquals(0.0, progress.filledPercentage)
        assertEquals(ProgressStatus.OPEN, progress.progressStatus)
        assertEquals(BigDecimal("0.00"), progress.committedWeight)
        assertEquals(BigDecimal("0.00"), progress.committedVolume)
        assertEquals(BigDecimal("0.00"), progress.pendingWeight)
        assertEquals(BigDecimal("0.00"), progress.pendingVolume)
        assertEquals(BigDecimal("1000.00"), progress.openWeight)
        assertEquals(BigDecimal("50.00"), progress.openVolume)
        assertEquals(3, progress.partialOrders.size)
    }

    @Test
    fun `should assign carrier to partial order successfully`() {
        // Arrange
        val partialOrder = partialOrders.first()
        val carrierId = UUID.randomUUID()
        val bidId = UUID.randomUUID()
        
        every { partialOrderRepository.findById(partialOrder.id!!) } returns Optional.of(partialOrder)
        every { partialOrderRepository.save(any()) } answers { firstArg() }
        every { masterOrderRepository.save(any()) } answers { firstArg() }
        every { kafkaTemplate.send(any(), any()) } returns mockk()

        // Act
        val result = orderService.assignCarrierToPartialOrder(partialOrder.id!!, carrierId, bidId)

        // Assert
        assertEquals(carrierId, result.assignedCarrierId)
        assertEquals(bidId, result.assignedBidId)
        assertEquals(PartialOrderStatus.AWARDED, result.status)
        verify { partialOrderRepository.save(any()) }
        verify { masterOrderRepository.save(any()) }
        verify { kafkaTemplate.send("carrier.assigned", any()) }
    }

    @Test
    fun `should calculate progress status correctly`() {
        // Arrange
        val masterOrderId = UUID.randomUUID()
        masterOrder.id = masterOrderId
        
        // Test OPEN status
        every { masterOrderRepository.findById(masterOrderId) } returns Optional.of(masterOrder)
        every { partialOrderRepository.findByMasterOrderId(masterOrderId) } returns emptyList()
        
        var progress = orderService.getOrderProgress(masterOrderId)
        assertEquals(ProgressStatus.OPEN, progress.progressStatus)

        // Test PENDING status
        val partialOrderPending = partialOrders.first().apply { status = PartialOrderStatus.AWARDED }
        every { partialOrderRepository.findByMasterOrderId(masterOrderId) } returns listOf(partialOrderPending)
        
        progress = orderService.getOrderProgress(masterOrderId)
        assertEquals(ProgressStatus.PENDING, progress.progressStatus)

        // Test COMMITTED status
        val partialOrdersCommitted = partialOrders.map { it.apply { status = PartialOrderStatus.AWARDED } }
        every { partialOrderRepository.findByMasterOrderId(masterOrderId) } returns partialOrdersCommitted
        
        progress = orderService.getOrderProgress(masterOrderId)
        assertEquals(ProgressStatus.COMMITTED, progress.progressStatus)
    }

    @Test
    fun `should perform auto check correctly`() {
        // Arrange
        val masterOrderId = UUID.randomUUID()
        masterOrder.id = masterOrderId
        masterOrder.requiredDeliveryDate = LocalDateTime.now().plusHours(22) // Погрузка через 22 часа
        
        every { masterOrderRepository.findById(masterOrderId) } returns Optional.of(masterOrder)
        every { partialOrderRepository.findByMasterOrderId(masterOrderId) } returns partialOrders
        every { masterOrderRepository.save(any()) } answers { firstArg() }
        every { partialOrderRepository.save(any()) } answers { firstArg() }
        every { kafkaTemplate.send(any(), any()) } returns mockk()

        // Act
        val result = orderService.performAutoCheck(masterOrderId)

        // Assert
        assertEquals(masterOrderId, result.masterOrderId)
        assertEquals(AutoCheckAction.NONE, result.action)
        assertTrue(result.reason.contains("acceptable parameters"))
    }

    @Test
    fun `should cancel order due to low fill rate`() {
        // Arrange
        val masterOrderId = UUID.randomUUID()
        masterOrder.id = masterOrderId
        masterOrder.requiredDeliveryDate = LocalDateTime.now().minusHours(3) // Погрузка была 3 часа назад
        
        val partialOrderAvailable = partialOrders.first().apply { status = PartialOrderStatus.AVAILABLE }
        every { masterOrderRepository.findById(masterOrderId) } returns Optional.of(masterOrder)
        every { partialOrderRepository.findByMasterOrderId(masterOrderId) } returns listOf(partialOrderAvailable)
        every { masterOrderRepository.save(any()) } answers { firstArg() }
        every { partialOrderRepository.save(any()) } answers { firstArg() }
        every { kafkaTemplate.send(any(), any()) } returns mockk()

        // Act
        val result = orderService.performAutoCheck(masterOrderId)

        // Assert
        assertEquals(AutoCheckAction.CANCEL_MASTER_ORDER, result.action)
        assertTrue(result.reason.contains("low fill rate"))
        verify { masterOrderRepository.save(match { it.status == MasterOrderStatus.CANCELLED }) }
    }

    @Test
    fun `should cancel expired partial orders`() {
        // Arrange
        val masterOrderId = UUID.randomUUID()
        masterOrder.id = masterOrderId
        
        val expiredPartialOrder = partialOrders.first().apply {
            status = PartialOrderStatus.AVAILABLE
            createdAt = LocalDateTime.now().minusHours(3) // Просрочен 3 часа назад
        }
        
        every { masterOrderRepository.findById(masterOrderId) } returns Optional.of(masterOrder)
        every { partialOrderRepository.findByMasterOrderId(masterOrderId) } returns listOf(expiredPartialOrder)
        every { partialOrderRepository.save(any()) } answers { firstArg() }
        every { kafkaTemplate.send(any(), any()) } returns mockk()

        // Act
        val result = orderService.performAutoCheck(masterOrderId)

        // Assert
        assertEquals(AutoCheckAction.CANCEL_PARTIAL_ORDER, result.action)
        assertTrue(result.reason.contains("expired partial orders"))
        verify { partialOrderRepository.save(match { it.status == PartialOrderStatus.CANCELLED }) }
    }

    @Test
    fun `should send reminder when approaching cancellation time`() {
        // Arrange
        val masterOrderId = UUID.randomUUID()
        masterOrder.id = masterOrderId
        masterOrder.requiredDeliveryDate = LocalDateTime.now().plusHours(23) // Погрузка через 23 часа
        
        every { masterOrderRepository.findById(masterOrderId) } returns Optional.of(masterOrder)
        every { partialOrderRepository.findByMasterOrderId(masterOrderId) } returns partialOrders
        every { kafkaTemplate.send(any(), any()) } returns mockk()

        // Act
        val result = orderService.performAutoCheck(masterOrderId)

        // Assert
        assertEquals(AutoCheckAction.SEND_REMINDER, result.action)
        assertTrue(result.reason.contains("Reminder sent"))
        verify { kafkaTemplate.send("order.reminders", any()) }
    }

    @Test
    fun `should calculate filled percentage correctly`() {
        // Arrange
        val masterOrderId = UUID.randomUUID()
        masterOrder.id = masterOrderId
        
        val partialOrder1 = partialOrders[0].apply { status = PartialOrderStatus.AWARDED }
        val partialOrder2 = partialOrders[1].apply { status = PartialOrderStatus.AVAILABLE }
        val partialOrder3 = partialOrders[2].apply { status = PartialOrderStatus.AVAILABLE }
        
        every { masterOrderRepository.findById(masterOrderId) } returns Optional.of(masterOrder)
        every { partialOrderRepository.findByMasterOrderId(masterOrderId) } returns listOf(partialOrder1, partialOrder2, partialOrder3)

        // Act
        val progress = orderService.getOrderProgress(masterOrderId)

        // Assert
        assertEquals(0.4, progress.filledPercentage) // Только первый частичный заказ назначен
    }

    @Test
    fun `should calculate weight and volume distribution correctly`() {
        // Arrange
        val masterOrderId = UUID.randomUUID()
        masterOrder.id = masterOrderId
        
        val partialOrder1 = partialOrders[0].apply { status = PartialOrderStatus.AWARDED }
        val partialOrder2 = partialOrders[1].apply { status = PartialOrderStatus.BIDDING }
        val partialOrder3 = partialOrders[2].apply { status = PartialOrderStatus.AVAILABLE }
        
        every { masterOrderRepository.findById(masterOrderId) } returns Optional.of(masterOrder)
        every { partialOrderRepository.findByMasterOrderId(masterOrderId) } returns listOf(partialOrder1, partialOrder2, partialOrder3)

        // Act
        val progress = orderService.getOrderProgress(masterOrderId)

        // Assert
        assertEquals(BigDecimal("400.00"), progress.committedWeight) // Назначенный
        assertEquals(BigDecimal("20.00"), progress.committedVolume)
        assertEquals(BigDecimal("300.00"), progress.pendingWeight) // На аукционе
        assertEquals(BigDecimal("15.00"), progress.pendingVolume)
        assertEquals(BigDecimal("300.00"), progress.openWeight) // Доступный
        assertEquals(BigDecimal("15.00"), progress.openVolume)
    }

    private fun createMockPoint(lat: Double, lon: Double): org.locationtech.jts.geom.Point {
        val geometryFactory = org.locationtech.jts.geom.GeometryFactory()
        return geometryFactory.createPoint(org.locationtech.jts.geom.Coordinate(lon, lat))
    }
}