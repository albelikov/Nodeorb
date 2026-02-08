package com.freight.marketplace.controller

import com.freight.marketplace.dto.BidDto
import com.freight.marketplace.dto.FreightOrderDto
import com.freight.marketplace.service.FreightOrderService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/freight-marketplace")
class FreightOrderController(
    private val freightOrderService: FreightOrderService
) {

    @PostMapping("/orders")
    fun createFreightOrder(
        @AuthenticationPrincipal jwt: Jwt,
        @Valid @RequestBody orderDto: FreightOrderDto
    ): ResponseEntity<FreightOrderDto> {
        val userId = UUID.fromString(jwt.subject)
        val orderWithShipperId = orderDto.copy(shipperId = userId)
        val createdOrder = freightOrderService.createFreightOrder(orderWithShipperId)
        return ResponseEntity.ok(createdOrder)
    }

    @GetMapping("/orders/{orderId}")
    fun getFreightOrder(@PathVariable orderId: UUID): ResponseEntity<FreightOrderDto> {
        val order = freightOrderService.getFreightOrderById(orderId)
        return ResponseEntity.ok(order)
    }

    @GetMapping("/orders")
    fun getMyFreightOrders(
        @AuthenticationPrincipal jwt: Jwt,
        pageable: Pageable
    ): ResponseEntity<Page<FreightOrderDto>> {
        val userId = UUID.fromString(jwt.subject)
        val orders = freightOrderService.getOrdersByShipper(userId, pageable)
        return ResponseEntity.ok(orders)
    }

    @GetMapping("/orders/open")
    fun getOpenFreightOrders(pageable: Pageable): ResponseEntity<Page<FreightOrderDto>> {
        val orders = freightOrderService.getOpenOrders(pageable)
        return ResponseEntity.ok(orders)
    }

    @PostMapping("/orders/{orderId}/bids")
    fun placeBid(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable orderId: UUID,
        @Valid @RequestBody bidDto: BidDto
    ): ResponseEntity<BidDto> {
        val carrierId = UUID.fromString(jwt.subject)
        val bidWithIds = bidDto.copy(
            freightOrderId = orderId,
            carrierId = carrierId
        )
        val createdBid = freightOrderService.placeBid(bidWithIds)
        return ResponseEntity.ok(createdBid)
    }

    @GetMapping("/orders/{orderId}/bids")
    fun getBidsForOrder(@PathVariable orderId: UUID): ResponseEntity<List<BidDto>> {
        val bids = freightOrderService.getBidsForOrder(orderId)
        return ResponseEntity.ok(bids)
    }

    @PostMapping("/orders/{orderId}/bids/{bidId}/award")
    fun awardOrder(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable orderId: UUID,
        @PathVariable bidId: UUID
    ): ResponseEntity<BidDto> {
        val shipperId = UUID.fromString(jwt.subject)
        val awardedBid = freightOrderService.awardOrder(orderId, bidId, shipperId)
        return ResponseEntity.ok(awardedBid)
    }

    @GetMapping("/bids/my")
    fun getMyBids(
        @AuthenticationPrincipal jwt: Jwt,
        pageable: Pageable
    ): ResponseEntity<List<BidDto>> {
        val carrierId = UUID.fromString(jwt.subject)
        val bids = freightOrderService.getBidsByCarrier(carrierId, pageable)
        return ResponseEntity.ok(bids.content)
    }
}