package com.logistics.financelegal

import com.logistics.financelegal.entities.*
import io.grpc.stub.StreamObserver
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class FinanceLegalServiceImpl : FinanceLegalServiceGrpcKt.FinanceLegalServiceCoroutineImplBase() {
    override suspend fun createAccount(request: CreateAccountRequest): AccountResponse {
        return transaction {
            val id = ChartOfAccounts.insertAndGetId {
                it[accountNumber] = request.accountNumber
                it[accountName] = request.accountName
                it[accountType] = request.accountType
                it[description] = request.description
                it[parentAccount] = request.parentAccount.takeIf { it.isNotEmpty() }
                it[currency] = request.currency.takeIf { it.isNotEmpty() } ?: "USD"
                it[createdBy] = request.createdBy
                it[updatedAt] = LocalDateTime.now()
            }.value
            
            AccountResponse.newBuilder()
                .setId(id)
                .setAccountNumber(request.accountNumber)
                .setAccountName(request.accountName)
                .setAccountType(request.accountType)
                .setDescription(request.description)
                .setParentAccount(request.parentAccount)
                .setCurrency(request.currency.takeIf { it.isNotEmpty() } ?: "USD")
                .setIsActive(true)
                .build()
        }
    }

    override suspend fun getAccount(request: GetAccountRequest): AccountResponse {
        return transaction {
            ChartOfAccounts.select { ChartOfAccounts.id eq request.id }
                .singleOrNull()
                ?.let {
                    AccountResponse.newBuilder()
                        .setId(it[ChartOfAccounts.id].value)
                        .setAccountNumber(it[ChartOfAccounts.accountNumber])
                        .setAccountName(it[ChartOfAccounts.accountName])
                        .setAccountType(it[ChartOfAccounts.accountType])
                        .setDescription(it[ChartOfAccounts.description] ?: "")
                        .setParentAccount(it[ChartOfAccounts.parentAccount] ?: "")
                        .setCurrency(it[ChartOfAccounts.currency])
                        .setIsActive(it[ChartOfAccounts.isActive])
                        .build()
                } ?: AccountResponse.getDefaultInstance()
        }
    }

    override suspend fun listAccounts(request: ListAccountsRequest): ListAccountsResponse {
        return transaction {
            val accounts = ChartOfAccounts.selectAll()
                .orderBy(ChartOfAccounts.accountNumber to SortOrder.ASC)
                .map {
                    AccountResponse.newBuilder()
                        .setId(it[ChartOfAccounts.id].value)
                        .setAccountNumber(it[ChartOfAccounts.accountNumber])
                        .setAccountName(it[ChartOfAccounts.accountName])
                        .setAccountType(it[ChartOfAccounts.accountType])
                        .setDescription(it[ChartOfAccounts.description] ?: "")
                        .setParentAccount(it[ChartOfAccounts.parentAccount] ?: "")
                        .setCurrency(it[ChartOfAccounts.currency])
                        .setIsActive(it[ChartOfAccounts.isActive])
                        .build()
                }
            
            ListAccountsResponse.newBuilder()
                .addAllAccounts(accounts)
                .build()
        }
    }

    override suspend fun createEntry(request: CreateEntryRequest): EntryResponse {
        return transaction {
            val id = AccountingEntries.insertAndGetId {
                it[entryDate] = LocalDateTime.parse(request.entryDate)
                it[description] = request.description
                it[debitAccount] = request.debitAccount
                it[creditAccount] = request.creditAccount
                it[amount] = java.math.BigDecimal(request.amount)
                it[currency] = request.currency.takeIf { it.isNotEmpty() } ?: "USD"
                it[exchangeRate] = request.exchangeRate.takeIf { it.isNotEmpty() }?.let { java.math.BigDecimal(it) }
                it[referenceNumber] = request.referenceNumber.takeIf { it.isNotEmpty() }
                it[transactionType] = request.transactionType
                it[createdBy] = request.createdBy
                it[createdAt] = LocalDateTime.now()
            }.value
            
            EntryResponse.newBuilder()
                .setId(id)
                .setEntryDate(request.entryDate)
                .setDescription(request.description)
                .setDebitAccount(request.debitAccount)
                .setCreditAccount(request.creditAccount)
                .setAmount(request.amount)
                .setCurrency(request.currency)
                .setExchangeRate(request.exchangeRate)
                .setReferenceNumber(request.referenceNumber)
                .setTransactionType(request.transactionType)
                .setCreatedBy(request.createdBy)
                .build()
        }
    }

    override suspend fun getEntry(request: GetEntryRequest): EntryResponse {
        return transaction {
            AccountingEntries.select { AccountingEntries.id eq request.id }
                .singleOrNull()
                ?.let {
                    EntryResponse.newBuilder()
                        .setId(it[AccountingEntries.id].value)
                        .setEntryDate(it[AccountingEntries.entryDate].toString())
                        .setDescription(it[AccountingEntries.description])
                        .setDebitAccount(it[AccountingEntries.debitAccount])
                        .setCreditAccount(it[AccountingEntries.creditAccount])
                        .setAmount(it[AccountingEntries.amount].toString())
                        .setCurrency(it[AccountingEntries.currency])
                        .setExchangeRate(it[AccountingEntries.exchangeRate]?.toString() ?: "")
                        .setReferenceNumber(it[AccountingEntries.referenceNumber] ?: "")
                        .setTransactionType(it[AccountingEntries.transactionType])
                        .setCreatedBy(it[AccountingEntries.createdBy])
                        .build()
                } ?: EntryResponse.getDefaultInstance()
        }
    }

    override suspend fun listEntries(request: ListEntriesRequest): ListEntriesResponse {
        return transaction {
            val entries = AccountingEntries.selectAll()
                .orderBy(AccountingEntries.entryDate to SortOrder.DESC)
                .map {
                    EntryResponse.newBuilder()
                        .setId(it[AccountingEntries.id].value)
                        .setEntryDate(it[AccountingEntries.entryDate].toString())
                        .setDescription(it[AccountingEntries.description])
                        .setDebitAccount(it[AccountingEntries.debitAccount])
                        .setCreditAccount(it[AccountingEntries.creditAccount])
                        .setAmount(it[AccountingEntries.amount].toString())
                        .setCurrency(it[AccountingEntries.currency])
                        .setExchangeRate(it[AccountingEntries.exchangeRate]?.toString() ?: "")
                        .setReferenceNumber(it[AccountingEntries.referenceNumber] ?: "")
                        .setTransactionType(it[AccountingEntries.transactionType])
                        .setCreatedBy(it[AccountingEntries.createdBy])
                        .build()
                }
            
            ListEntriesResponse.newBuilder()
                .addAllEntries(entries)
                .build()
        }
    }
}