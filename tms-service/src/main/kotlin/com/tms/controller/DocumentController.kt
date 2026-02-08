package com.tms.controller

import com.tms.dto.*
import com.tms.service.DocumentGenerationService
import org.slf4j.LoggerFactory
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/shipments/{shipmentId}/documents")
class DocumentController(
    private val documentGenerationService: DocumentGenerationService
) {

    companion object {
        private val logger = LoggerFactory.getLogger(DocumentController::class.java)
    }

    /**
     * Генерирует BOL (Bill of Lading) для отправки
     * @param shipmentId ID отправки
     * @param request Детали запроса
     * @return Ответ с информацией о сгенерированном документе
     */
    @PostMapping("/bol", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun generateBOL(
        @PathVariable shipmentId: Long,
        @RequestBody request: BolRequestDto
    ): ResponseEntity<BolResponseDto> {
        logger.info("Received request to generate BOL for shipment: $shipmentId")
        try {
            val response = documentGenerationService.generateBOL(shipmentId, request)
            return ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Error generating BOL for shipment: $shipmentId", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Загружает POD (Proof of Delivery)
     * @param shipmentId ID отправки
     * @param file Файл с изображением POD
     * @param request Детали запроса
     * @return Ответ с информацией о сохраненном документе
     */
    @PostMapping("/pod", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun uploadPOD(
        @PathVariable shipmentId: Long,
        @RequestParam("file") file: MultipartFile,
        @RequestParam("signature") signature: String?,
        @RequestParam("signedBy") signedBy: String,
        @RequestParam("signedAt") signedAt: String,
        @RequestParam("notes") notes: String?
    ): ResponseEntity<PodResponseDto> {
        logger.info("Received request to upload POD for shipment: $shipmentId")
        try {
            val request = PodRequestDto(signature, signedBy, signedAt, notes)
            val response = documentGenerationService.uploadPOD(shipmentId, file, request)
            return ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Error uploading POD for shipment: $shipmentId", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Получает все документы отправки
     * @param shipmentId ID отправки
     * @return Список документов
     */
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getShipmentDocuments(@PathVariable shipmentId: Long): ResponseEntity<Any> {
        logger.info("Received request to get documents for shipment: $shipmentId")
        try {
            // TODO: Implement get shipment documents
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null)
        } catch (e: Exception) {
            logger.error("Error getting documents for shipment: $shipmentId", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Скачивает документ
     * @param documentId ID документа
     * @return Файл
     */
    @GetMapping("/{documentId}/download", produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    fun downloadDocument(@PathVariable documentId: Long): ResponseEntity<FileSystemResource> {
        logger.info("Received request to download document: $documentId")
        try {
            // TODO: Implement download document
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null)
        } catch (e: Exception) {
            logger.error("Error downloading document: $documentId", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }
}