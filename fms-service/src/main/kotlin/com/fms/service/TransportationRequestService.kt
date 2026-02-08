package com.fms.service

import com.fms.model.TransportationRequest
import com.fms.repository.TransportationRequestRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class TransportationRequestService(private val transportationRequestRepository: TransportationRequestRepository) {

    companion object {
        private val logger = LoggerFactory.getLogger(TransportationRequestService::class.java)
    }

    /**
     * Создает новую заявку на перевозку
     * @param request Данные о заявке
     * @return Созданная заявка
     */
    @Transactional
    fun createTransportationRequest(request: TransportationRequest): TransportationRequest {
        logger.info("Creating transportation request for customer: ${request.customerId}")
        request.createdAt = LocalDateTime.now()
        request.updatedAt = LocalDateTime.now()
        return transportationRequestRepository.save(request)
    }

    /**
     * Получает заявку на перевозку по ID
     * @param id ID заявки
     * @return Заявка на перевозку
     */
    fun getTransportationRequestById(id: UUID): TransportationRequest {
        return transportationRequestRepository.findById(id)
            .orElseThrow { RuntimeException("Transportation request not found: $id") }
    }

    /**
     * Получает все заявки на перевозку
     * @return Список всех заявок
     */
    fun getAllTransportationRequests(): List<TransportationRequest> {
        return transportationRequestRepository.findAll()
    }

    /**
     * Получает заявки на перевозку по ID клиента
     * @param customerId ID клиента
     * @return Список заявок
     */
    fun getTransportationRequestsByCustomerId(customerId: UUID): List<TransportationRequest> {
        return transportationRequestRepository.findByCustomerId(customerId)
    }

    /**
     * Получает заявки на перевозку по статусу
     * @param status Статус заявки
     * @return Список заявок
     */
    fun getTransportationRequestsByStatus(status: String): List<TransportationRequest> {
        return transportationRequestRepository.findByStatus(status)
    }

    /**
     * Получает заявки на перевозку по приоритету
     * @param priority Приоритет заявки
     * @return Список заявок
     */
    fun getTransportationRequestsByPriority(priority: String): List<TransportationRequest> {
        return transportationRequestRepository.findByPriority(priority)
    }

    /**
     * Обновляет заявку на перевозку
     * @param id ID заявки
     * @param request Данные для обновления
     * @return Обновленная заявка
     */
    @Transactional
    fun updateTransportationRequest(id: UUID, request: TransportationRequest): TransportationRequest {
        logger.info("Updating transportation request: $id")
        val existingRequest = getTransportationRequestById(id)
        existingRequest.customerId = request.customerId
        existingRequest.requestType = request.requestType
        existingRequest.cargoType = request.cargoType
        existingRequest.cargoWeight = request.cargoWeight
        existingRequest.cargoVolume = request.cargoVolume
        existingRequest.pickupLocation = request.pickupLocation
        existingRequest.deliveryLocation = request.deliveryLocation
        existingRequest.pickupTime = request.pickupTime
        existingRequest.deliveryTime = request.deliveryTime
        existingRequest.priority = request.priority
        existingRequest.status = request.status
        existingRequest.price = request.price
        existingRequest.updatedAt = LocalDateTime.now()
        return transportationRequestRepository.save(existingRequest)
    }

    /**
     * Обновляет статус заявки на перевозку
     * @param id ID заявки
     * @param status Новый статус
     * @return Обновленная заявка
     */
    @Transactional
    fun updateTransportationRequestStatus(id: UUID, status: String): TransportationRequest {
        logger.info("Updating transportation request status: $id, $status")
        val request = getTransportationRequestById(id)
        request.status = status
        request.updatedAt = LocalDateTime.now()
        return transportationRequestRepository.save(request)
    }

    /**
     * Удаляет заявку на перевозку
     * @param id ID заявки
     */
    @Transactional
    fun deleteTransportationRequest(id: UUID) {
        logger.info("Deleting transportation request: $id")
        val request = getTransportationRequestById(id)
        transportationRequestRepository.delete(request)
    }
}