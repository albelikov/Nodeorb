package com.logistics.financelegal.integration

import com.logistics.financelegal.CustomsServiceImpl
import com.logistics.financelegal.entities.CustomsDocuments
import com.logistics.financelegal.entities.CustomsDocument
import io.grpc.ManagedChannelBuilder
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class CustomsServiceIntegrationTest {
    private val channel = ManagedChannelBuilder.forAddress("localhost", 8080)
        .usePlaintext()
        .build()

    @BeforeEach
    fun setup() {
        // Initialize in-memory database for testing
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")
        
        transaction {
            SchemaUtils.create(CustomsDocuments)
        }
    }

    @AfterEach
    fun teardown() {
        transaction {
            SchemaUtils.drop(CustomsDocuments)
        }
        channel.shutdown()
    }

    @Test
    fun `test create and retrieve customs document`() {
        val service = CustomsServiceImpl()
        
        // Create customs document
        val request = CreateCustomsDocumentRequest.newBuilder()
            .setDocumentNumber("DOC-2026-001")
            .setDocumentType("CMR")
            .setCustomsCode("1000")
            .setGoodsDescription("Electronics goods")
            .setValue("10000.00")
            .setCurrency("USD")
            .setCustomsDuty("1000.00")
            .setVatAmount("2000.00")
            .setTotalTaxes("3000.00")
            .setStatus("PENDING")
            .setIssueDate(LocalDateTime.now().toString())
            .setExpiryDate(LocalDateTime.now().plusDays(30).toString())
            .setDocumentUrl("https://example.com/documents/DOC-2026-001.pdf")
            .setCreatedBy("test_user")
            .build()

        val response = service.createCustomsDocument(request)
        
        // Verify response
        assertNotNull(response)
        assertEquals("DOC-2026-001", response.documentNumber)
        assertEquals("CMR", response.documentType)
        assertEquals("Electronics goods", response.goodsDescription)
        assertEquals("10000.00", response.value)
        assertEquals("USD", response.currency)
        assertEquals("1000.00", response.customsDuty)
        assertEquals("2000.00", response.vatAmount)
        assertEquals("3000.00", response.totalTaxes)
        assertEquals("PENDING", response.status)
        assertEquals("test_user", response.createdBy)
    }

    @Test
    fun `test update customs document status`() {
        val service = CustomsServiceImpl()
        
        // Create customs document
        val createRequest = CreateCustomsDocumentRequest.newBuilder()
            .setDocumentNumber("DOC-2026-002")
            .setDocumentType("Invoice")
            .setCustomsCode("2000")
            .setGoodsDescription("Office supplies")
            .setValue("5000.00")
            .setCurrency("EUR")
            .setCustomsDuty("500.00")
            .setVatAmount("1000.00")
            .setTotalTaxes("1500.00")
            .setStatus("PENDING")
            .setIssueDate(LocalDateTime.now().toString())
            .setExpiryDate(LocalDateTime.now().plusDays(30).toString())
            .setDocumentUrl("https://example.com/documents/DOC-2026-002.pdf")
            .setCreatedBy("test_user")
            .build()

        val createResponse = service.createCustomsDocument(createRequest)
        
        // Update status
        val updateRequest = UpdateCustomsDocumentStatusRequest.newBuilder()
            .setId(createResponse.id)
            .setStatus("APPROVED")
            .build()

        val updateResponse = service.updateCustomsDocumentStatus(updateRequest)
        
        // Verify status update
        assertEquals("APPROVED", updateResponse.status)
    }
}