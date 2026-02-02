package com.nodeorb.freight.marketplace.service

import com.nodeorb.freight.marketplace.dto.BidDto
import com.nodeorb.freight.marketplace.dto.FreightOrderDto
import com.nodeorb.freight.marketplace.dto.LocationDto
import com.nodeorb.freight.marketplace.entity.*
import com.nodeorb.freight.marketplace.repository.FreightOrderRepository
import com.nodeorb.freight.marketplace.matching.BidMatchingAlgorithm
import com.nodeorb.freight.marketplace.dto.CargoType
import com.nodeorb.freight.marketplace.dto.OrderStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.locationtech.jts.geom.GeometryFactory
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

class FreightOrderServiceTest {

    private lateinit var freightOrderRepository: FreightOrderRepository
    private lateinit var freightOrderService: FreightOrderService
    private lateinit var geometryFactory: GeometryFactory

    @BeforeEach
    fun setUp() {
        freightOrderRepository = mockk()
        geometryFactory = GeometryFactory()
        freightOrderService = FreightOrderService(
            freightOrderRepository,
            mockk(), // bidRepository
            mockk(), // matchingAlgorithm
            mockk(), // properties
            geometryFactory
        )
    }

    @Test
    fun `should create freight order successfully`() {
        // Given
        val orderDto = createValidFreightOrderDto()
        val savedEntity = createFreightOrderEntity()
        
        every { freightOrderRepository.save(any()) } returns savedEntity

        // When
        val result = freightOrderService.createFreightOrder(orderDto)

        // Then
        assertNotNull(result)
        assertEquals(orderDto.title, result.title)
        verify(exactly = 1) { freightOrderRepository.save(any()) }
    }

    @Test
    fun `should get freight order by id`() {
        // Given
        val orderId = UUID.randomUUID()
        val entity = createFreightOrderEntity(
            id = orderId,
            title = "Контейнерный груз из Гамбурга в Берлин"
        )

        every { freightOrderRepository.findById(orderId) } returns Optional.of(entity)

        // When
        val result = freightOrderService.getFreightOrderById(orderId)

        // Then
        assertNotNull(result)
        assertEquals(orderId, result.id)
        verify(exactly = 1) { freightOrderRepository.findById(orderId) }
    }

    private fun createValidFreightOrderDto(): FreightOrderDto {
        return FreightOrderDto(
            shipperId = UUID.randomUUID(),
            title = "Контейнерный груз из Гамбурга в Берлин",
            description = "Требуется перевозка контейнера 20 футов",
            cargoType = CargoType.CONTAINER,
            weight = BigDecimal("25000.00"),
            volume = BigDecimal("33.2"),
            pickupLocation = createLocationDto(
                53.5511, 9.9937, "Гамбург, Германия", "Гамбург", "Германия", "20095"
            ),
            deliveryLocation = createLocationDto(
                52.5200, 13.4050, "Берлин, Германия", "Берлин", "Германия", "10117"
            ),
            requiredDeliveryDate = LocalDateTime.now().plusDays(3),
            maxBidAmount = BigDecimal("5000.00")
        )
    }

    private fun createFreightOrderEntity(
        id: UUID = UUID.randomUUID(),
        title: String = "Контейнерный груз из Гамбурга в Берлин"
    ): FreightOrderEntity {
        return FreightOrderEntity(
            id = id,
            shipperId = UUID.randomUUID(),
            title = title,
            description = "Требуется перевозка контейнера 20 футов",
            cargoType = com.nodeorb.freight.marketplace.entity.CargoType.CONTAINER,
            weight = BigDecimal("25000.00"),
            volume = BigDecimal("33.2"),
            pickupLocation = geometryFactory.createPoint(org.locationtech.jts.geom.Coordinate(9.9937, 53.5511)),
            deliveryLocation = geometryFactory.createPoint(org.locationtech.jts.geom.Coordinate(13.4050, 52.5200)),
            pickupAddress = "Гамбург, Германия",
            deliveryAddress = "Берлин, Германия",
            requiredDeliveryDate = LocalDateTime.now().plusDays(3),
            maxBidAmount = BigDecimal("5000.00"),
            status = com.nodeorb.freight.marketplace.entity.OrderStatus.OPEN
        )
    }

    private fun createLocationDto(
        lat: Double,
        lon: Double,
        address: String,
        city: String,
        country: String,
        postalCode: String?
    ): LocationDto {
        return LocationDto(
            latitude = lat,
            longitude = lon,
            address = address,
            city = city,
            country = country,
            postalCode = postalCode
        )
    }
}