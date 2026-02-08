package com.nodeorb.shared.organization

import com.nodeorb.shared.common.Address
import com.nodeorb.shared.common.ContactInfo
import java.io.Serializable
import java.util.*

data class Organization(
    val id: UUID = UUID.randomUUID(),
    val organizationType: OrganizationType,
    val legalName: String,
    val tradeName: String?,
    val taxId: String,
    val address: Address,
    val contactInfo: ContactInfo,
    val isActive: Boolean = true
) : Serializable

enum class OrganizationType {
    CUSTOMER,
    CARRIER,
    WAREHOUSE_OPERATOR,
    CUSTOMS_BROKER,
    PARTNER
}