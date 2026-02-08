package com.logistics.customs

import com.logistics.customs.entities.CustomsDeclarations
import com.logistics.customs.entities.CustomsDocuments
import com.logistics.customs.entities.CustomsPayments
import com.logistics.customs.entities.GoodsClassifications
import com.logistics.customs.services.CustomsServiceImpl
import io.grpc.ManagedChannelBuilder
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class CustomsServiceTest {
    private val channel = ManagedChannelBuilder.forAddress("localhost", 50056)
        .usePlaintext()
        .build()

    @BeforeEach
    fun setup() {
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
        transaction {
            SchemaUtils.create(
                CustomsDeclarations,
                CustomsDocuments,
                CustomsPayments,
                GoodsClassifications
            )
        }
    }

    @AfterEach
    fun teardown() {
        transaction {
            SchemaUtils.drop(
                CustomsDeclarations,
                CustomsDocuments,
                CustomsPayments,
                GoodsClassifications
            )
        }
        channel.shutdown()
    }

    @Test
    fun `test create and retrieve declaration`() {
        val service = CustomsServiceImpl()
        val request = CreateDeclarationRequest.newBuilder()
            .setDeclarationNumber("TEST-2024-001")
            .setShipperId(UUID.randomUUID().toString())
            .setConsigneeId(UUID.randomUUID().toString())
            .setOrderId(UUID.randomUUID().toString())
            .setGoods("Test goods")
            .setTotalValue("1000.00")
            .setCustomsProcedure("Export")
            .setStatus("PENDING")
            .build()

        val response = service.createDeclaration(request)
        
        assertNotNull(response)
        assertNotNull(response.id)
        assertEquals("TEST-2024-001", response.declarationNumber)
        assertEquals("Test goods", response.goods)
        assertEquals("1000.00", response.totalValue)
        assertEquals("Export", response.customsProcedure)
        assertEquals("PENDING", response.status)
        assertNotNull(response.createdAt)
        assertNotNull(response.updatedAt)
    }

    @Test
    fun `test create and retrieve document`() {
        val service = CustomsServiceImpl()
        val declarationId = UUID.randomUUID().toString()
        
        val request = CreateDocumentRequest.newBuilder()
            .setDeclarationId(declarationId)
            .setDocumentType("Invoice")
            .setDocumentNumber("DOC-2024-001")
            .setDocumentDate(LocalDateTime.now().toString())
            .setFileUrl("https://example.com/invoice.pdf")
            .setStatus("PENDING")
            .build()

        val response = service.createDocument(request)
        
        assertNotNull(response)
        assertNotNull(response.id)
        assertEquals(declarationId, response.declarationId)
        assertEquals("Invoice", response.documentType)
        assertEquals("DOC-2024-001", response.documentNumber)
        assertEquals("https://example.com/invoice.pdf", response.fileUrl)
        assertEquals("PENDING", response.status)
        assertNotNull(response.createdAt)
        assertNotNull(response.updatedAt)
    }

    @Test
    fun `test create and retrieve payment`() {
        val service = CustomsServiceImpl()
        val declarationId = UUID.randomUUID().toString()
        
        val request = CreatePaymentRequest.newBuilder()
            .setDeclarationId(declarationId)
            .setPaymentNumber("PAY-2024-001")
            .setAmount("500.00")
            .setPaymentDate(LocalDateTime.now().toString())
            .setPaymentMethod("Bank Transfer")
            .setStatus("PENDING")
            .build()

        val response = service.createPayment(request)
        
        assertNotNull(response)
        assertNotNull(response.id)
        assertEquals(declarationId, response.declarationId)
        assertEquals("PAY-2024-001", response.paymentNumber)
        assertEquals("500.00", response.amount)
        assertEquals("Bank Transfer", response.paymentMethod)
        assertEquals("PENDING", response.status)
        assertNotNull(response.createdAt)
        assertNotNull(response.updatedAt)
    }

    @Test
    fun `test create and retrieve goods classification`() {
        val service = CustomsServiceImpl()
        val declarationId = UUID.randomUUID().toString()
        
        val request = CreateGoodsClassificationRequest.newBuilder()
            .setDeclarationId(declarationId)
            .setProductCode("PROD-001")
            .setProductName("Test Product")
            .setQuantity(10)
            .setUnit("pcs")
            .setValue("100.00")
            .setTnvedCode("8471.30")
            .setCountryOfOrigin("CN")
            .setCountryOfDestination("RU")
            .setCustomsTariff("0.10")
            .setVatRate("0.20")
            .build()

        val response = service.createGoodsClassification(request)
        
        assertNotNull(response)
        assertNotNull(response.id)
        assertEquals(declarationId, response.declarationId)
        assertEquals("PROD-001", response.productCode)
        assertEquals("Test Product", response.productName)
        assertEquals(10, response.quantity)
        assertEquals("pcs", response.unit)
        assertEquals("100.00", response.value)
        assertEquals("8471.30", response.tnvedCode)
        assertEquals("CN", response.countryOfOrigin)
        assertEquals("RU", response.countryOfDestination)
        assertEquals("0.10", response.customsTariff)
        assertEquals("0.20", response.vatRate)
        assertNotNull(response.createdAt)
        assertNotNull(response.updatedAt)
    }
}