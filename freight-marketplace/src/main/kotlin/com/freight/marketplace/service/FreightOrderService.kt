package com.freight.marketplace.service

import com.freight.marketplace.dto.BidDto
import com.freight.marketplace.dto.FreightOrderDto
import com.freight.marketplace.entity.*
import com.freight.marketplace.exception.FreightMarketplaceException
import com.freight.marketplace.repository.BidRepository
import com.freight.marketplace.repository.FreightOrderRepository
import com.freight.marketplace.matching.BidMatchingAlgorithm
import com.freight.marketplace.FreightMarketplaceProperties
import com.freight.marketplace.dto.LocationDto
import com.freight.marketplace.entity.CargoType
import com.freight.marketplace.entity.OrderStatus
import com.freight.marketplace.entity.BidStatus
import com.freight.marketplace.dto.OrderStatus as DtoOrderStatus
import com.freight.marketplace.dto.BidStatus as DtoBidStatus
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Point
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Service
@Transactional
class FreightOrderService(
    private val freightOrderRepository: FreightOrderRepository,
    private val bidRepository: BidRepository,
    private val matchingAlgorithm: BidMatchingAlgorithm,
    private val properties: FreightMarketplaceProperties,
    private val geometryFactory: GeometryFactory
) {

    fun createFreightOrder(orderDto: FreightOrderDto): FreightOrderDto {
        validateCreateOrderRequest(orderDto)
        
        val orderEntity = FreightOrderEntity(
            shipperId = orderDto.shipperId,
            title = orderDto.title,
            description = orderDto.description,
            cargoType = orderDto.cargoType,
            weight = orderDto.weight,
            volume = orderDto.volume,
            pickupLocation = createPoint(orderDto.pickupLocation),
            deliveryLocation = createPoint(orderDto.deliveryLocation),
            pickupAddress = orderDto.pickupLocation.address,
            deliveryAddress = orderDto.deliveryLocation.address,
            requiredDeliveryDate = orderDto.requiredDeliveryDate,
            maxBidAmount = orderDto.maxBidAmount,
            status = com.freight.marketplace.entity.OrderStatus.OPEN
        )
        
        val savedOrder = freightOrderRepository.save(orderEntity)
        return mapToDto(savedOrder)
    }

    fun getFreightOrderById(orderId: UUID): FreightOrderDto {
        val order = freightOrderRepository.findById(orderId)
            .orElseThrow { FreightMarketplaceException("Freight order not found with id: $orderId") }
        return mapToDto(order)
    }

    fun getOrdersByShipper(shipperId: UUID, pageable: Pageable): Page<FreightOrderDto> {
        return freightOrderRepository.findByShipperId(shipperId, pageable)
            .map { mapToDto(it) }
    }

    fun getOpenOrders(pageable: Pageable): Page<FreightOrderDto> {
        return freightOrderRepository.findByStatus(com.freight.marketplace.entity.OrderStatus.OPEN, pageable)
            .map { mapToDto(it) }
    }

    fun placeBid(bidDto: BidDto): BidDto {
        val order = freightOrderRepository.findById(bidDto.freightOrderId)
            .orElseThrow { FreightMarketplaceException("Freight order not found with id: ${bidDto.freightOrderId}") }
        
        validateBid(order, bidDto)
        
        val bidEntity = BidEntity(
            carrierId = bidDto.carrierId,
            freightOrder = order,
            amount = bidDto.amount,
            proposedDeliveryDate = bidDto.proposedDeliveryDate,
            notes = bidDto.notes,
            status = com.freight.marketplace.entity.BidStatus.PENDING
        )
        
        val savedBid = bidRepository.save(bidEntity)
        
        // Обновляем статус заказа
        if (order.status == com.freight.marketplace.entity.OrderStatus.OPEN) {
            order.status = com.freight.marketplace.entity.OrderStatus.AUCTION_ACTIVE
            freightOrderRepository.save(order)
        }
        
        return BidDto(
            id = savedBid.id,
            carrierId = savedBid.carrierId,
            freightOrderId = savedBid.freightOrder.id!!,
            amount = savedBid.amount,
            proposedDeliveryDate = savedBid.proposedDeliveryDate,
            notes = savedBid.notes,
            status = savedBid.status.toDto(),
            createdAt = savedBid.createdAt,
            updatedAt = savedBid.updatedAt
        )
    }

    fun getBidsForOrder(orderId: UUID): List<BidDto> {
        return bidRepository.findByFreightOrderId(orderId)
            .map { bid ->
                BidDto(
                    id = bid.id,
                    carrierId = bid.carrierId,
                    freightOrderId = bid.freightOrder.id!!,
                    amount = bid.amount,
                    proposedDeliveryDate = bid.proposedDeliveryDate,
                    notes = bid.notes,
                    status = bid.status.toDto(),
                    score = bid.score,
                    createdAt = bid.createdAt,
                    updatedAt = bid.updatedAt
                )
            }
    }

    fun awardOrder(orderId: UUID, bidId: UUID, shipperId: UUID): BidDto {
        val order = freightOrderRepository.findById(orderId)
            .orElseThrow { FreightMarketplaceException("Freight order not found with id: $orderId") }
        
        if (order.shipperId != shipperId) {
            throw FreightMarketplaceException("Only the order shipper can award bids")
        }
        
        val bid = bidRepository.findById(bidId)
            .orElseThrow { FreightMarketplaceException("Bid not found with id: $bidId") }
        
        if (bid.freightOrder.id != order.id) {
            throw FreightMarketplaceException("Bid does not match the specified order")
        }
        
        // Пометка выбранной заявки как принятой
        bid.status = com.freight.marketplace.entity.BidStatus.ACCEPTED
        order.status = com.freight.marketplace.entity.OrderStatus.AWARDED
        
        // Пометка остальных заявок как отклоненных
        bidRepository.findByFreightOrderId(orderId)
            .filter { it.id != bidId }
            .forEach { 
                it.status = com.freight.marketplace.entity.BidStatus.REJECTED 
                bidRepository
            }
        
        val savedBid = bidRepository.save(bid)
        freightOrderRepository.save(order)
        
        return BidDto(
            id = savedBid.id,
            carrierId = savedBid.carrierId,
            freightOrderId = savedBid.freightOrder.id!!,
            amount = savedBid.amount,
            proposedDeliveryDate = savedBid.proposedDeliveryDate,
            notes = savedBid.notes,
            status = savedBid.status.toDto(),
            createdAt = savedBid.createdAt,
            updatedAt = savedBid.updatedAt
        )
    }

    fun getBidsByCarrier(carrierId: UUID, pageable: Pageable): Page<BidDto> {
        return bidRepository.findByCarrierId(carrierId, pageable)
            .map { bid ->
                BidDto(
                    id = bid.id,
                    carrierId = bid.carrierId,
                    freightOrderId = bid.freightOrder.id!!,
                    amount = bid.amount,
                    proposedDeliveryDate = bid.proposedDeliveryDate,
                    notes = bid.notes,
                    status = bid.status.toDto(),
                    score = bid.score,
                    createdAt = bid.createdAt,
                    updatedAt = bid.updatedAt
                )
            }
    }

    private fun validateCreateOrderRequest(orderDto: FreightOrderDto) {
        if (orderDto.requiredDeliveryDate.isBefore(LocalDateTime.now())) {
            throw FreightMarketplaceException("Required delivery date cannot be in the past")
        }
        
        if (orderDto.maxBidAmount <= BigDecimal.ZERO) {
            throw FreightMarketplaceException("Maximum bid amount must be positive")
        }
    }

    private fun validateBid(order: FreightOrderEntity, bidDto: BidDto) {
        if (order.status != com.freight.marketplace.entity.OrderStatus.OPEN && order.status != com.freight.marketplace.entity.OrderStatus.AUCTION_ACTIVE) {
            throw FreightMarketplaceException("Order is not accepting bids")
        }
        
        if (bidDto.amount > order.maxBidAmount) {
            throw FreightMarketplaceException("Bid amount exceeds maximum allowed")
        }
        
        if (bidDto.proposedDeliveryDate.isAfter(order.requiredDeliveryDate)) {
            throw FreightMarketplaceException("Proposed delivery date is later than required")
        }
        
        // Проверка на повторные заявки
        val existingBid = bidRepository.findByFreightOrderIdAndCarrierId(order.id!!, bidDto.carrierId)
        if (existingBid != null) {
            throw FreightMarketplaceException("Carrier has already placed a bid on this order")
        }
        
        // Проверка лимита заявок
        val bidsCount = bidRepository.findByFreightOrderId(order.id!!).size
        if (bidsCount >= properties.auction.maxBidsPerOrder) {
            throw FreightMarketplaceException("Maximum bids reached for this order")
        }
    }

    private fun createPoint(locationDto: LocationDto): Point {
        return geometryFactory.createPoint(
            Coordinate(locationDto.longitude, locationDto.latitude)
        )
    }

    private fun mapToDto(entity: FreightOrderEntity): FreightOrderDto {
        return FreightOrderDto(
            id = entity.id,
            shipperId = entity.shipperId,
            title = entity.title,
            description = entity.description,
            cargoType = entity.cargoType,
            weight = entity.weight,
            volume = entity.volume,
            pickupLocation = LocationDto(
                latitude = entity.pickupLocation.y,
                longitude = entity.pickupLocation.x,
                address = entity.pickupAddress,
                city = "", // Извлечь из адреса
                country = "", // Извлечь из адреса
                postalCode = null
            ),
            deliveryLocation = LocationDto(
                latitude = entity.deliveryLocation.y,
                longitude = entity.deliveryLocation.x,
                address = entity.deliveryAddress,
                city = "", // Извлечь из адреса
                country = "", // Извлечь из адреса
                postalCode = null
            ),
            requiredDeliveryDate = entity.requiredDeliveryDate,
            maxBidAmount = entity.maxBidAmount,
            status = DtoOrderStatus.valueOf(entity.status.name),
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            bids = entity.bids.map { bid ->
                BidDto(
                    id = bid.id,
                    carrierId = bid.carrierId,
                    freightOrderId = bid.freightOrder.id!!,
                    amount = bid.amount,
                    proposedDeliveryDate = bid.proposedDeliveryDate,
                    notes = bid.notes,
                    status = DtoBidStatus.valueOf(bid.status.name),
                    score = bid.score,
                    createdAt = bid.createdAt,
                    updatedAt = bid.updatedAt
                )
            }
        )
    }

    // Extension functions for type conversion
    private fun com.freight.marketplace.entity.BidStatus.toDto(): com.freight.marketplace.dto.BidStatus {
        return com.freight.marketplace.dto.BidStatus.valueOf(this.name)
    }

    private fun com.freight.marketplace.entity.OrderStatus.toDto(): com.freight.marketplace.dto.OrderStatus {
        return com.freight.marketplace.dto.OrderStatus.valueOf(this.name)
    }

    private fun com.freight.marketplace.entity.CargoType.toDto(): com.freight.marketplace.dto.CargoType {
        return com.freight.marketplace.dto.CargoType.valueOf(this.name)
    }
}