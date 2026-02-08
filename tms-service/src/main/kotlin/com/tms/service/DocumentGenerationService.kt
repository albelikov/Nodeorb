package com.tms.service

import com.tms.dto.*
import com.tms.model.TransportDocument
import com.tms.repository.TransportDocumentRepository
import com.tms.repository.ShipmentRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.StringUtils
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.time.Year

@Service
class DocumentGenerationService(
    private val transportDocumentRepository: TransportDocumentRepository,
    private val shipmentRepository: ShipmentRepository
) {

    companion object {
        private val logger = LoggerFactory.getLogger(DocumentGenerationService::class.java)
        private const val BOL_DOCUMENT_TYPE = "BOL"
        private const val POD_DOCUMENT_TYPE = "POD"
        private const val CMR_DOCUMENT_TYPE = "CMR"
    }

    @Value("\${tms.documents.storage.path:./documents}")
    private lateinit var storagePath: String

    /**
     * Генерирует BOL (Bill of Lading) для отправки
     * @param shipmentId ID отправки
     * @param request Детали запроса
     * @return Ответ с информацией о сгенерированном документе
     */
    @Transactional
    fun generateBOL(shipmentId: Long, request: BolRequestDto): BolResponseDto {
        logger.info("Generating BOL for shipment: $shipmentId")

        // Проверяем, что отправка существует
        val shipment = shipmentRepository.findById(shipmentId)
            .orElseThrow { RuntimeException("Shipment not found: $shipmentId") }

        // Генерируем номер документа
        val documentNumber = generateDocumentNumber(BOL_DOCUMENT_TYPE)

        // Создаем запись в базе данных
        val transportDocument = TransportDocument(
            shipmentId = shipmentId,
            documentType = BOL_DOCUMENT_TYPE,
            documentNumber = documentNumber,
            filePath = null,
            fileSize = null,
            mimeType = null,
            status = "ISSUED",
            issuedAt = Instant.now(),
            issuedBy = null,
            signedAt = null,
            signedBy = null,
            signatureImage = null,
            metadata = null
        )

        // Сохраняем документ в базе данных
        val savedDocument = transportDocumentRepository.save(transportDocument)

        // Генерируем PDF (в текущей реализации это просто пример)
        val pdfFile = generateBOLPDF(savedDocument, request)

        // Обновляем запись в базе данных с информацией о файле
        savedDocument.filePath = pdfFile.absolutePath
        savedDocument.fileSize = pdfFile.length()
        savedDocument.mimeType = "application/pdf"
        transportDocumentRepository.save(savedDocument)

        // Обновляем статус отправки (добавляем флаг, что есть BOL)
        shipment.hasBillOfLading = true
        shipmentRepository.save(shipment)

        logger.info("BOL generated for shipment: $shipmentId, document number: $documentNumber")

        return BolResponseDto(
            documentId = savedDocument.id!!,
            documentNumber = documentNumber,
            pdfUrl = "http://localhost:8080/api/v1/documents/${savedDocument.id}/download"
        )
    }

    /**
     * Загружает POD (Proof of Delivery)
     * @param shipmentId ID отправки
     * @param file Файл с изображением POD
     * @param request Детали запроса
     * @return Ответ с информацией о сохраненном документе
     */
    @Transactional
    fun uploadPOD(shipmentId: Long, file: MultipartFile, request: PodRequestDto): PodResponseDto {
        logger.info("Uploading POD for shipment: $shipmentId")

        // Проверяем, что отправка существует
        val shipment = shipmentRepository.findById(shipmentId)
            .orElseThrow { RuntimeException("Shipment not found: $shipmentId") }

        // Генерируем номер документа
        val documentNumber = generateDocumentNumber(POD_DOCUMENT_TYPE)

        // Сохраняем файл на диск
        val fileName = StringUtils.cleanPath(file.originalFilename ?: "pod-${documentNumber}.jpg")
        val fileStorageLocation = File(storagePath)
        if (!fileStorageLocation.exists()) {
            fileStorageLocation.mkdirs()
        }
        val targetFile = File(fileStorageLocation, fileName)
        FileOutputStream(targetFile).use { outputStream ->
            outputStream.write(file.bytes)
        }

        // Создаем запись в базе данных
        val transportDocument = TransportDocument(
            shipmentId = shipmentId,
            documentType = POD_DOCUMENT_TYPE,
            documentNumber = documentNumber,
            filePath = targetFile.absolutePath,
            fileSize = file.size,
            mimeType = file.contentType,
            status = "SIGNED",
            issuedAt = Instant.now(),
            issuedBy = null,
            signedAt = Instant.parse(request.signedAt),
            signedBy = request.signedBy,
            signatureImage = request.signature,
            metadata = null
        )

        // Сохраняем документ в базе данных
        val savedDocument = transportDocumentRepository.save(transportDocument)

        // Обновляем статус отправки (добавляем флаг, что есть POD)
        shipment.hasProofOfDelivery = true
        shipmentRepository.save(shipment)

        logger.info("POD uploaded for shipment: $shipmentId, document number: $documentNumber")

        return PodResponseDto(
            documentId = savedDocument.id!!,
            documentNumber = documentNumber,
            fileUrl = "http://localhost:8080/api/v1/documents/${savedDocument.id}/download"
        )
    }

    /**
     * Генерирует уникальный номер документа
     * @param documentType Тип документа
     * @return Уникальный номер документа
     */
    private fun generateDocumentNumber(documentType: String): String {
        val year = Year.now().value
        val random = (Math.random() * 100000).toInt().toString().padStart(5, '0')
        return "$documentType-${year}-$random"
    }

    /**
     * Генерирует PDF для BOL (в текущей реализации это просто пример)
     * @param document Документ
     * @param request Детали запроса
     * @return Файл с PDF
     */
    private fun generateBOLPDF(document: TransportDocument, request: BolRequestDto): File {
        val fileStorageLocation = File(storagePath)
        if (!fileStorageLocation.exists()) {
            fileStorageLocation.mkdirs()
        }
        val fileName = "bol-${document.documentNumber}.pdf"
        val targetFile = File(fileStorageLocation, fileName)

        // TODO: Реализовать генерацию реального PDF с помощью iText или Thymeleaf-to-PDF
        // В текущей реализации просто создаем пустой файл
        targetFile.createNewFile()

        return targetFile
    }
}