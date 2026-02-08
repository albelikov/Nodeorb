package com.nodeorb.freight.marketplace.service

import com.nodeorb.freight.marketplace.dto.OrderFillingWarningEvent
import com.nodeorb.freight.marketplace.entity.*
import com.nodeorb.freight.marketplace.repository.MasterOrderRepository
import com.nodeorb.freight.marketplace.repository.PartialOrderRepository
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
class OrderExecutionServiceTest {

    @MockK
    private lateinit var masterOrderRepository: MasterOrderRepository

    @MockK
    private lateinit var partialOrderRepository: PartialOrderRepository

    @MockK
    private lateinit var kafkaTemplate: KafkaTemplate<String, Any>

    @InjectMockKs
    private lateinit var orderExecutionService: OrderExecutionService

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
                status = PartialOrderStatus.AWARDED
            ),
            PartialOrderEntity(
                masterOrder = masterOrder,
                weight = BigDecimal("300.00"),
                volume = BigDecimal("15.00"),
                percentage = 0.3,
                status = PartialOrderStatus.IN_PROGRESS
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
    fun `should send filling warning when remaining volume is above threshold`() {
        // Arrange
        val masterOrderId = UUID.randomUUID()
        masterOrder.id = masterOrderId
        masterOrder.requiredDeliveryDate = LocalDateTime.now().plusHours(22) // Погрузка через 22 часа
        
        every { masterOrderRepository.findByStatusInAndRequiredDeliveryDateBetween(any(), any(), any()) } returns listOf(masterOrder)
        every { partialOrderRepository.findByMasterOrderId(masterOrderId) } returns partialOrders
        every { kafkaTemplate.send(any(), any()) } returns mockk()

        // Act
        orderExecutionService.checkOrderFillingStatus()

        // Assert
        verify {
            kafkaTemplate.send("order.filling.warnings", any<OrderFillingWarningEvent>())
        }
    }

    @Test
    fun `should not send filling warning when remaining volume is below threshold`() {
        // Arrange
        val masterOrderId = UUID.randomUUID()
        masterOrder.id = masterOrderId
        masterOrder.requiredDeliveryDate = LocalDateTime.now().plusHours(22)
        
        // Создаем частичные заказы с высокой заполненностью
        val filledPartialOrders = listOf(
            PartialOrderEntity(
                masterOrder = masterOrder,
                weight = BigDecimal("800.00"),
                volume = BigDecimal("40.00"),
                percentage = 0.8,
                status = PartialOrderStatus.AWARDED
            ),
            PartialOrderEntity(
                masterOrder = masterOrder,
                weight = BigDecimal("150.00"),
                volume = BigDecimal("7.50"),
                percentage = 0.15,
                status = PartialOrderStatus.IN_PROGRESS
            )
        )

        every { masterOrderRepository.findByStatusInAndRequiredDeliveryDateBetween(any(), any(), any()) } returns listOf(masterOrder)
        every { partialOrderRepository.findByMasterOrderId(masterOrderId) } returns filledPartialOrders
        every { kafkaTemplate.send(any(), any()) } returns mockk()

        // Act
        orderExecutionService.checkOrderFillingStatus()

        // Assert
        verify(exactly = 0) {
            kafkaTemplate.send("order.filling.warnings", any<OrderFillingWarningEvent>())
        }
    }

    @Test
    fun `should update master order status to COMPLETED when all partial orders are delivered`() {
        // Arrange
        val masterOrderId = UUID.randomUUID()
        masterOrder.id = masterOrderId
        masterOrder.status = MasterOrderStatus.IN_PROGRESS
        
        val deliveredPartialOrders = listOf(
            PartialOrderEntity(
                masterOrder = masterOrder,
                weight = BigDecimal("500.00"),
                volume = BigDecimal("25.00"),
                percentage = 0.5,
                status = PartialOrderStatus.DELIVERED
            ),
            PartialOrderEntity(
                masterOrder = masterOrder,
                weight = BigDecimal("500.00"),
                volume = BigDecimal("25.00"),
                percentage = 0.5,
                status = PartialOrderStatus.DELIVERED
            )
        )

        every { masterOrderRepository.findById(masterOrderId) } returns Optional.of(masterOrder)
        every { partialOrderRepository.findByMasterOrderId(masterOrderId) } returns deliveredPartialOrders
        every { masterOrderRepository.save(any()) } answers { firstArg() }
        every { kafkaTemplate.send(any(), any()) } returns mockk()

        // Act
        val result = orderExecutionService.updateMasterOrderStatus(masterOrderId)

        // Assert
        assertEquals(MasterOrderStatus.COMPLETED, result.status)
        verify { masterOrderRepository.save(match { it.status == MasterOrderStatus.COMPLETED }) }
        verify { kafkaTemplate.send("order.status.changed", any()) }
    }

    @Test
    fun `should update master order status to IN_PROGRESS when some partial orders are in progress`() {
        // Arrange
        val masterOrderId = UUID.randomUUID()
        masterOrder.id = masterOrderId
        masterOrder.status = MasterOrderStatus.PARTIALLY_FILLED
        
        val mixedPartialOrders = listOf(
            PartialOrderEntity(
                masterOrder = masterOrder,
                weight = BigDecimal("400.00"),
                volume = BigDecimal("20.00"),
                percentage = 0.4,
                status = PartialOrderStatus.AWARDED
            ),
            PartialOrderEntity(
                masterOrder = masterOrder,
                weight = BigDecimal("300.00"),
                volume = BigDecimal("15.00"),
                percentage = 0.3,
                status = PartialOrderStatus.IN_PROGRESS
            ),
            PartialOrderEntity(
                masterOrder = masterOrder,
                weight = BigDecimal("300.00"),
                volume = BigDecimal("15.00"),
                percentage = 0.3,
                status = PartialOrderStatus.AVAILABLE
            )
        )

        every { masterOrderRepository.findById(masterOrderId) } returns Optional.of(masterOrder)
        every { partialOrderRepository.findByMasterOrderId(masterOrderId) } returns mixedPartialOrders
        every { masterOrderRepository.save(any()) } answers { firstArg() }
        every { kafkaTemplate.send(any(), any()) } returns mockk()

        // Act
        val result = orderExecutionService.updateMasterOrderStatus(masterOrderId)

        // Assert
        assertEquals(MasterOrderStatus.IN_PROGRESS, result.status)
        verify { masterOrderRepository.save(match { it.status == MasterOrderStatus.IN_PROGRESS }) }
        verify { kafkaTemplate.send("order.status.changed", any()) }
    }

    @Test
    fun `should validate partial order capacity correctly`() {
        // Arrange
        val masterOrderId = UUID.randomUUID()
        masterOrder.id = masterOrderId
        masterOrder.remainingVolume = BigDecimal("10.00") // Осталось 10 кубометров
        
        every { masterOrderRepository.findById(masterOrderId) } returns Optional.of(masterOrder)

        // Act & Assert
        // Запрос на объем больше minQuantum (minQuantum = 10% от 50 = 5)
        assertTrue(orderExecutionService.validatePartialOrderCapacity(masterOrderId, BigDecimal("6.00")))
        
        // Запрос на объем меньше minQuantum
        assertTrue(!orderExecutionService.validatePartialOrderCapacity(masterOrderId, BigDecimal("4.00")))
        
        // Запрос на объем больше оставшегося
        assertTrue(!orderExecutionService.validatePartialOrderCapacity(masterOrderId, BigDecimal("15.00")))
    }

    @Test
    fun `should calculate min quantum correctly`() {
        // Arrange
        val masterOrderId = UUID.randomUUID()
        masterOrder.id = masterOrderId
        masterOrder.totalVolume = BigDecimal("100.00") // 10% = 10
        
        every { masterOrderRepository.findById(masterOrderId) } returns Optional.of(masterOrder)

        // Act & Assert
        // Для объема 100, minQuantum должен быть 10 (10%)
        assertTrue(orderExecutionService.validatePartialOrderCapacity(masterOrderId, BigDecimal("10.00")))
        assertTrue(!orderExecutionService.validatePartialOrderCapacity(masterOrderId, BigDecimal("9.00")))
    }

    @Test
    fun `should handle minimum quantum of 1 cubic meter`() {
        // Arrange
        val masterOrderId = UUID.randomUUID()
        masterOrder.id = masterOrderId
        masterOrder.totalVolume = BigDecimal("5.00") // 10% = 0.5, но минимум 1
        
        every { masterOrderRepository.findById(masterOrderId) } returns Optional.of(masterOrder)

        // Act & Assert
        // Для маленького объема minQuantum должен быть 1
        assertTrue(orderExecutionService.validatePartialOrderCapacity(masterOrderId, BigDecimal("1.00")))
        assertTrue(!orderExecutionService.validatePartialOrderCapacity(masterOrderId, BigDecimal("0.50")))
    }

    @Test
    fun `should check order completion correctly`() {
        // Arrange
        val masterOrderId = UUID.randomUUID()
        masterOrder.id = masterOrderId
        masterOrder.status = MasterOrderStatus.IN_PROGRESS
        
        val deliveredPartialOrders = listOf(
            PartialOrderEntity(
                masterOrder = masterOrder,
                weight = BigDecimal("500.00"),
                volume = BigDecimal("25.00"),
                percentage = 0.5,
                status = PartialOrderStatus.DELIVERED
            ),
            PartialOrderEntity(
                masterOrder = masterOrder,
                weight = BigDecimal("500.00"),
                volume = BigDecimal("25.00"),
                percentage = 0.5,
                status = PartialOrderStatus.DELIVERED
            )
        )

        every { masterOrderRepository.findById(masterOrderId) } returns Optional.of(masterOrder)
        every { partialOrderRepository.findByMasterOrderId(masterOrderId) } returns deliveredPartialOrders
        every { masterOrderRepository.save(any()) } answers { firstArg() }
        every { kafkaTemplate.send(any(), any()) } returns mockk()

        // Act
        val result = orderExecutionService.checkOrderCompletion(masterOrderId)

        // Assert
        assertTrue(result)
        assertEquals(MasterOrderStatus.COMPLETED, masterOrder.status)
        verify { kafkaTemplate.send("order.completed", any()) }
    }

    @Test
    fun `should not update status if order is already completed`() {
        // Arrange
        val masterOrderId = UUID.randomUUID()
        masterOrder.id = masterOrderId
        masterOrder.status = MasterOrderStatus.COMPLETED
        
        every { masterOrderRepository.findById(masterOrderId) } returns Optional.of(masterOrder)

        // Act
        val result = orderExecutionService.checkOrderCompletion(masterOrderId)

        // Assert
        assertTrue(result)
        verify(exactly = 0) { masterOrderRepository.save(any()) }
    }

    @Test
    fun `should force complete order correctly`() {
        // Arrange
        val masterOrderId = UUID.randomUUID()
        masterOrder.id = masterOrderId
        masterOrder.status = MasterOrderStatus.IN_PROGRESS
        
        val partialOrders = listOf(
            PartialOrderEntity(
                masterOrder = masterOrder,
                weight = BigDecimal("400.00"),
                volume = BigDecimal("20.00"),
                percentage = 0.4,
                status = PartialOrderStatus.AWARDED
            ),
            PartialOrderEntity(
                masterOrder = masterOrder,
                weight = BigDecimal("300.00"),
                volume = BigDecimal("15.00"),
                percentage = 0.3,
                status = PartialOrderStatus.IN_PROGRESS
            ),
            PartialOrderEntity(
                masterOrder = masterOrder,
                weight = BigDecimal("300.00"),
                volume = BigDecimal("15.00"),
                percentage = 0.3,
                status = PartialOrderStatus.AVAILABLE
            )
        )

        every { masterOrderRepository.findById(masterOrderId) } returns Optional.of(masterOrder)
        every { partialOrderRepository.findByMasterOrderId(masterOrderId) } returns partialOrders
        every { partialOrderRepository.save(any()) } answers { firstArg() }
        every { masterOrderRepository.save(any()) } answers { firstArg() }
        every { kafkaTemplate.send(any(), any()) } returns mockk()

        // Act
        val result = orderExecutionService.forceCompleteOrder(masterOrderId, "Emergency completion")

        // Assert
        assertEquals(MasterOrderStatus.COMPLETED, result.status)
        
        // Проверяем, что незавершенные частичные заказы были отменены
        verify {
            partialOrderRepository.save(match { it.status == PartialOrderStatus.CANCELLED })
        }
        
        verify { kafkaTemplate.send("order.force.completed", any()) }
    }

    @Test
    fun `should get order execution statistics correctly`() {
        // Arrange
        every { masterOrderRepository.count() } returns 100L
        every { masterOrderRepository.countByStatus(MasterOrderStatus.COMPLETED) } returns 60L
        every { masterOrderRepository.countByStatus(MasterOrderStatus.IN_PROGRESS) } returns 20L
        every { masterOrderRepository.countByStatus(MasterOrderStatus.PARTIALLY_FILLED) } returns 10L
        every { masterOrderRepository.countByStatus(MasterOrderStatus.OPEN) } returns 5L
        every { masterOrderRepository.countByStatus(MasterOrderStatus.CANCELLED) } returns 5L

        // Act
        val statistics = orderExecutionService.getOrderExecutionStatistics()

        // Assert
        assertEquals(100L, statistics.totalOrders)
        assertEquals(60L, statistics.completedOrders)
        assertEquals(20L, statistics.inProgressOrders)
        assertEquals(10L, statistics.pendingOrders)
        assertEquals(5L, statistics.openOrders)
        assertEquals(5L, statistics.cancelledOrders)
        assertEquals(0.6, statistics.completionRate)
    }

    private fun createMockPoint(lat: Double, lon: Double): org.locationtech.jts.geom.Point {
        val geometryFactory = org.locationtech.jts.geom.GeometryFactory()
        return geometryFactory.createPoint(org.locationtech.jts.geom.Coordinate(lon, lat))
    }
}