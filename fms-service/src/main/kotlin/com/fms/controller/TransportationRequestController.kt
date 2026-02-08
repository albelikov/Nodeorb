package com.fms.controller

import com.fms.model.TransportationRequest
import com.fms.service.TransportationRequestService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/transportation-requests")
class TransportationRequestController(private val transportationRequestService: TransportationRequestService) {

    companion object {
        private val logger = LoggerFactory.getLogger(TransportationRequestController::class.java)
    }

    /**
     * Создает новую заявку на перевозку
     * @param request Данные о заявке
     * @return Созданная заявка
     */
    @PostMapping
    fun createTransportationRequest(@RequestBody request: TransportationRequest): ResponseEntity<TransportationRequest> {
        logger.info("Received request to create transportation request for customer: ${request.customerId}")
        try {
            val createdRequest = transportationRequestService.createTransportationRequest(request)
            return ResponseEntity.ok(createdRequest)
        } catch (e: Exception) {
            logger.error("Error creating transportation request for customer: ${request.customerId}", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Получает заявку на перевозку по ID
     * @param id ID заявки
     * @return Заявка на перевозку
     */
    @GetMapping("/{id}")
    fun getTransportationRequestById(@PathVariable id: UUID): ResponseEntity<TransportationRequest> {
        logger.info("Received request to get transportation request by ID: $id")
        try {
            val request = transportationRequestService.getTransportationRequestById(id)
            return ResponseEntity.ok(request)
        } catch (e: Exception) {
            logger.error("Error getting transportation request by ID: $id", e)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)
        }
    }

    /**
     * Получает все заявки на перевозку
     * @return Список заявок
     */
    @GetMapping
    fun getAllTransportationRequests(): ResponseEntity<List<TransportationRequest>> {
        logger.info("Received request to get all transportation requests")
        try {
            val requests = transportationRequestService.getAllTransportationRequests()
            return ResponseEntity.ok(requests)
        } catch (e: Exception) {
            logger.error("Error getting all transportation requests", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Получает заявки на перевозку по ID клиента
     * @param customerId ID клиента
     * @return Список заявок
     */
    @GetMapping("/by-customer/{customerId}")
    fun getTransportationRequestsByCustomerId(@PathVariable customerId: UUID): ResponseEntity<List<TransportationRequest>> {
        logger.info("Received request to get transportation requests by customer ID: $customerId")
        try {
            val requests = transportationRequestService.getTransportationRequestsByCustomerId(customerId)
            return ResponseEntity.ok(requests)
        } catch (e: Exception) {
            logger.error("Error getting transportation requests by customer ID: $customerId", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Получает заявки на перевозку по статусу
     * @param status Статус заявки
     * @return Список заявок
     */
    @GetMapping("/by-status/{status}")
    fun getTransportationRequestsByStatus(@PathVariable status: String): ResponseEntity<List<TransportationRequest>> {
        logger.info("Received request to get transportation requests by status: $status")
        try {
            val requests = transportationRequestService.getTransportationRequestsByStatus(status)
            return ResponseEntity.ok(requests)
        } catch (e: Exception) {
            logger.error("Error getting transportation requests by status: $status", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Получает заявки на перевозку по приоритету
     * @param priority Приоритет заявки
     * @return Список заявок
     */
    @GetMapping("/by-priority/{priority}")
    fun getTransportationRequestsByPriority(@PathVariable priority: String): ResponseEntity<List<TransportationRequest>> {
        logger.info("Received request to get transportation requests by priority: $priority")
        try {
            val requests = transportationRequestService.getTransportationRequestsByPriority(priority)
            return ResponseEntity.ok(requests)
        } catch (e: Exception) {
            logger.error("Error getting transportation requests by priority: $priority", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Обновляет заявку на перевозку
     * @param id ID заявки
     * @param request Данные для обновления
     * @return Обновленная заявка
     */
    @PutMapping("/{id}")
    fun updateTransportationRequest(@PathVariable id: UUID, @RequestBody request: TransportationRequest): ResponseEntity<TransportationRequest> {
        logger.info("Received request to update transportation request: $id")
        try {
            val updatedRequest = transportationRequestService.updateTransportationRequest(id, request)
            return ResponseEntity.ok(updatedRequest)
        } catch (e: Exception) {
            logger.error("Error updating transportation request: $id", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Обновляет статус заявки на перевозку
     * @param id ID заявки
     * @param status Новый статус
     * @return Обновленная заявка
     */
    @PutMapping("/{id}/status")
    fun updateTransportationRequestStatus(@PathVariable id: UUID, @RequestBody status: String): ResponseEntity<TransportationRequest> {
        logger.info("Received request to update transportation request status: $id, status: $status")
        try {
            val updatedRequest = transportationRequestService.updateTransportationRequestStatus(id, status)
            return ResponseEntity.ok(updatedRequest)
        } catch (e: Exception) {
            logger.error("Error updating transportation request status: $id, status: $status", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Удаляет заявку на перевозку
     * @param id ID заявки
     * @return Ответ о результате операции
     */
    @DeleteMapping("/{id}")
    fun deleteTransportationRequest(@PathVariable id: UUID): ResponseEntity<Unit> {
        logger.info("Received request to delete transportation request: $id")
        try {
            transportationRequestService.deleteTransportationRequest(id)
            return ResponseEntity.ok().build()
        } catch (e: Exception) {
            logger.error("Error deleting transportation request: $id", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
}