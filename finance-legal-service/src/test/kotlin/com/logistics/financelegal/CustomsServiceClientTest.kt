package com.logistics.financelegal

import com.logistics.customs.CreateDeclarationRequest
import com.logistics.customs.GetDeclarationRequest
import com.logistics.customs.ListDeclarationsRequest
import com.logistics.financelegal.clients.CustomsClient
import com.logistics.financelegal.services.CustomsService
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertNotNull

class CustomsServiceClientTest {
    private val customsService = CustomsService()
    
    @Test
    fun `test create declaration via customs service`() = runBlocking {
        val declaration = customsService.createDeclaration(
            declarationNumber = "TEST-2024-001",
            shipperId = UUID.randomUUID(),
            consigneeId = UUID.randomUUID(),
            orderId = UUID.randomUUID(),
            goods = "Test goods",
            totalValue = "1000.00",
            customsProcedure = "Export",
            status = "PENDING"
        )
        
        assertNotNull(declaration)
        assertNotNull(declaration.id)
        assertNotNull(declaration.declarationNumber)
        assertNotNull(declaration.shipperId)
        assertNotNull(declaration.consigneeId)
        assertNotNull(declaration.orderId)
        assertNotNull(declaration.goods)
        assertNotNull(declaration.totalValue)
        assertNotNull(declaration.customsProcedure)
        assertNotNull(declaration.status)
        assertNotNull(declaration.createdAt)
        assertNotNull(declaration.updatedAt)
    }
    
    @Test
    fun `test list declarations via customs service`() = runBlocking {
        val response = customsService.listDeclarations(pageNumber = 1, pageSize = 10)
        
        assertNotNull(response)
        assertNotNull(response.declarationsList)
    }
}