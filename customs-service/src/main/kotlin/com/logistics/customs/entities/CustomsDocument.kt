package com.logistics.customs.entities

import org.jetbrains.exposed.sql.*
import java.time.LocalDateTime
import java.util.UUID

object CustomsDocuments : Table("customs_documents") {
    val id = uuid("id").clientDefault { UUID.randomUUID() }
    val declarationId = uuid("declaration_id")
    val documentType = varchar("document_type", 100)
    val documentNumber = varchar("document_number", 100)
    val documentDate = datetime("document_date").default(LocalDateTime.now())
    val fileUrl = varchar("file_url", 500).nullable()
    val status = varchar("status", 50)
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())
    
    override val primaryKey = PrimaryKey(id)
    index(declarationId)
    index(isUnique = true, documentNumber)
}

data class CustomsDocument(
    val id: UUID,
    val declarationId: UUID,
    val documentType: String,
    val documentNumber: String,
    val documentDate: LocalDateTime,
    val fileUrl: String?,
    val status: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)