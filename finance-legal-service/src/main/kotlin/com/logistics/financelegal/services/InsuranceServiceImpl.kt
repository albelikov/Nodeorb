package com.logistics.financelegal

import com.logistics.financelegal.entities.*
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class InsuranceServiceImpl : InsuranceServiceGrpcKt.InsuranceServiceCoroutineImplBase() {
    override suspend fun createInsurancePolicy(request: CreateInsurancePolicyRequest): InsurancePolicyResponse {
        return transaction {
            val id = InsurancePolicies.insertAndGetId {
                it[policyNumber] = request.policyNumber
                it[policyType] = request.policyType
                it[insurer] = request.insurer
                it[insured] = request.insured
                it[startDate] = LocalDateTime.parse(request.startDate)
                it[endDate] = LocalDateTime.parse(request.endDate)
                it[coverageAmount] = java.math.BigDecimal(request.coverageAmount)
                it[premiumAmount] = java.math.BigDecimal(request.premiumAmount)
                it[currency] = request.currency.takeIf { it.isNotEmpty() } ?: "USD"
                it[coverageDetails] = request.coverageDetails.takeIf { it.isNotEmpty() }
                it[status] = request.status.takeIf { it.isNotEmpty() } ?: "ACTIVE"
                it[documentUrl] = request.documentUrl.takeIf { it.isNotEmpty() }
                it[createdBy] = request.createdBy
                it[updatedAt] = LocalDateTime.now()
            }.value
            
            InsurancePolicyResponse.newBuilder()
                .setId(id)
                .setPolicyNumber(request.policyNumber)
                .setPolicyType(request.policyType)
                .setInsurer(request.insurer)
                .setInsured(request.insured)
                .setStartDate(request.startDate)
                .setEndDate(request.endDate)
                .setCoverageAmount(request.coverageAmount)
                .setPremiumAmount(request.premiumAmount)
                .setCurrency(request.currency)
                .setCoverageDetails(request.coverageDetails)
                .setStatus(request.status.takeIf { it.isNotEmpty() } ?: "ACTIVE")
                .setDocumentUrl(request.documentUrl)
                .setCreatedBy(request.createdBy)
                .build()
        }
    }

    override suspend fun getInsurancePolicy(request: GetInsurancePolicyRequest): InsurancePolicyResponse {
        return transaction {
            InsurancePolicies.select { InsurancePolicies.id eq request.id }
                .singleOrNull()
                ?.let {
                    InsurancePolicyResponse.newBuilder()
                        .setId(it[InsurancePolicies.id].value)
                        .setPolicyNumber(it[InsurancePolicies.policyNumber])
                        .setPolicyType(it[InsurancePolicies.policyType])
                        .setInsurer(it[InsurancePolicies.insurer])
                        .setInsured(it[InsurancePolicies.insured])
                        .setStartDate(it[InsurancePolicies.startDate].toString())
                        .setEndDate(it[InsurancePolicies.endDate].toString())
                        .setCoverageAmount(it[InsurancePolicies.coverageAmount].toString())
                        .setPremiumAmount(it[InsurancePolicies.premiumAmount].toString())
                        .setCurrency(it[InsurancePolicies.currency])
                        .setCoverageDetails(it[InsurancePolicies.coverageDetails] ?: "")
                        .setStatus(it[InsurancePolicies.status])
                        .setDocumentUrl(it[InsurancePolicies.documentUrl] ?: "")
                        .setCreatedBy(it[InsurancePolicies.createdBy])
                        .build()
                } ?: InsurancePolicyResponse.getDefaultInstance()
        }
    }

    override suspend fun listInsurancePolicies(request: ListInsurancePoliciesRequest): ListInsurancePoliciesResponse {
        return transaction {
            val policies = InsurancePolicies.selectAll()
                .orderBy(InsurancePolicies.createdAt to SortOrder.DESC)
                .map {
                    InsurancePolicyResponse.newBuilder()
                        .setId(it[InsurancePolicies.id].value)
                        .setPolicyNumber(it[InsurancePolicies.policyNumber])
                        .setPolicyType(it[InsurancePolicies.policyType])
                        .setInsurer(it[InsurancePolicies.insurer])
                        .setInsured(it[InsurancePolicies.insured])
                        .setStartDate(it[InsurancePolicies.startDate].toString())
                        .setEndDate(it[InsurancePolicies.endDate].toString())
                        .setCoverageAmount(it[InsurancePolicies.coverageAmount].toString())
                        .setPremiumAmount(it[InsurancePolicies.premiumAmount].toString())
                        .setCurrency(it[InsurancePolicies.currency])
                        .setCoverageDetails(it[InsurancePolicies.coverageDetails] ?: "")
                        .setStatus(it[InsurancePolicies.status])
                        .setDocumentUrl(it[InsurancePolicies.documentUrl] ?: "")
                        .setCreatedBy(it[InsurancePolicies.createdBy])
                        .build()
                }
            
            ListInsurancePoliciesResponse.newBuilder()
                .addAllPolicies(policies)
                .build()
        }
    }

    override suspend fun updateInsurancePolicyStatus(request: UpdateInsurancePolicyStatusRequest): InsurancePolicyResponse {
        return transaction {
            InsurancePolicies.update({ InsurancePolicies.id eq request.id }) {
                it[status] = request.status
                it[updatedAt] = LocalDateTime.now()
            }
            
            getInsurancePolicy(GetInsurancePolicyRequest.newBuilder().setId(request.id).build())
        }
    }
}