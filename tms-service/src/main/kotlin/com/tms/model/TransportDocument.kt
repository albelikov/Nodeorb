package com.tms.model

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "transport_documents")
data class TransportDocument(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var shipmentId: Long,

    @Column(nullable = false)
    var documentType: String,

    @Column(unique = true)
    var documentNumber: String,

    var filePath: String?,

    var fileSize: Long?,

    var mimeType: String?,

    var status: String,

    var issuedAt: Instant?,

    var issuedBy: Long?,

    var signedAt: Instant?,

    var signedBy: String?,

    var signatureImage: String?,

    @Column(columnDefinition = "jsonb")
    var metadata: String?,

    var createdAt: Instant = Instant.now()
)