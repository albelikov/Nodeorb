package com.logistics.financelegal

import com.logistics.financelegal.entities.*
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class LegalModuleServiceImpl : LegalModuleServiceGrpcKt.LegalModuleServiceCoroutineImplBase() {
    override suspend fun createContract(request: CreateContractRequest): ContractResponse {
        return transaction {
            val id = Contracts.insertAndGetId {
                it[contractNumber] = request.contractNumber
                it[contractName] = request.contractName
                it[counterparty] = request.counterparty
                it[counterpartyId] = request.counterpartyId.takeIf { it > 0 }
                it[startDate] = LocalDateTime.parse(request.startDate)
                it[endDate] = request.endDate.takeIf { it.isNotEmpty() }?.let { LocalDateTime.parse(it) }
                it[contractType] = request.contractType
                it[status] = request.status.takeIf { it.isNotEmpty() } ?: "DRAFT"
                it[totalAmount] = request.totalAmount.takeIf { it.isNotEmpty() }?.let { java.math.BigDecimal(it) }
                it[currency] = request.currency.takeIf { it.isNotEmpty() } ?: "USD"
                it[terms] = request.terms.takeIf { it.isNotEmpty() }
                it[signedBy] = request.signedBy.takeIf { it.isNotEmpty() }
                it[documentUrl] = request.documentUrl.takeIf { it.isNotEmpty() }
                it[createdBy] = request.createdBy
                it[updatedAt] = LocalDateTime.now()
            }.value
            
            ContractResponse.newBuilder()
                .setId(id)
                .setContractNumber(request.contractNumber)
                .setContractName(request.contractName)
                .setCounterparty(request.counterparty)
                .setCounterpartyId(request.counterpartyId)
                .setStartDate(request.startDate)
                .setEndDate(request.endDate)
                .setContractType(request.contractType)
                .setStatus(request.status.takeIf { it.isNotEmpty() } ?: "DRAFT")
                .setTotalAmount(request.totalAmount)
                .setCurrency(request.currency)
                .setTerms(request.terms)
                .setSignedBy(request.signedBy)
                .setDocumentUrl(request.documentUrl)
                .setCreatedBy(request.createdBy)
                .build()
        }
    }

    override suspend fun getContract(request: GetContractRequest): ContractResponse {
        return transaction {
            Contracts.select { Contracts.id eq request.id }
                .singleOrNull()
                ?.let {
                    ContractResponse.newBuilder()
                        .setId(it[Contracts.id].value)
                        .setContractNumber(it[Contracts.contractNumber])
                        .setContractName(it[Contracts.contractName])
                        .setCounterparty(it[Contracts.counterparty])
                        .setCounterpartyId(it[Contracts.counterpartyId] ?: 0L)
                        .setStartDate(it[Contracts.startDate].toString())
                        .setEndDate(it[Contracts.endDate]?.toString() ?: "")
                        .setContractType(it[Contracts.contractType])
                        .setStatus(it[Contracts.status])
                        .setTotalAmount(it[Contracts.totalAmount]?.toString() ?: "")
                        .setCurrency(it[Contracts.currency])
                        .setTerms(it[Contracts.terms] ?: "")
                        .setSignedBy(it[Contracts.signedBy] ?: "")
                        .setDocumentUrl(it[Contracts.documentUrl] ?: "")
                        .setCreatedBy(it[Contracts.createdBy])
                        .build()
                } ?: ContractResponse.getDefaultInstance()
        }
    }

    override suspend fun listContracts(request: ListContractsRequest): ListContractsResponse {
        return transaction {
            val contracts = Contracts.selectAll()
                .orderBy(Contracts.createdAt to SortOrder.DESC)
                .map {
                    ContractResponse.newBuilder()
                        .setId(it[Contracts.id].value)
                        .setContractNumber(it[Contracts.contractNumber])
                        .setContractName(it[Contracts.contractName])
                        .setCounterparty(it[Contracts.counterparty])
                        .setCounterpartyId(it[Contracts.counterpartyId] ?: 0L)
                        .setStartDate(it[Contracts.startDate].toString())
                        .setEndDate(it[Contracts.endDate]?.toString() ?: "")
                        .setContractType(it[Contracts.contractType])
                        .setStatus(it[Contracts.status])
                        .setTotalAmount(it[Contracts.totalAmount]?.toString() ?: "")
                        .setCurrency(it[Contracts.currency])
                        .setTerms(it[Contracts.terms] ?: "")
                        .setSignedBy(it[Contracts.signedBy] ?: "")
                        .setDocumentUrl(it[Contracts.documentUrl] ?: "")
                        .setCreatedBy(it[Contracts.createdBy])
                        .build()
                }
            
            ListContractsResponse.newBuilder()
                .addAllContracts(contracts)
                .build()
        }
    }

    override suspend fun updateContractStatus(request: UpdateContractStatusRequest): ContractResponse {
        return transaction {
            Contracts.update({ Contracts.id eq request.id }) {
                it[status] = request.status
                it[updatedAt] = LocalDateTime.now()
            }
            
            getContract(GetContractRequest.newBuilder().setId(request.id).build())
        }
    }
}