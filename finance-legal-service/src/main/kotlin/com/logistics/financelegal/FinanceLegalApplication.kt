package com.logistics.financelegal

import io.grpc.Server
import io.grpc.ServerBuilder
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

import com.logistics.financelegal.entities.AccountingEntries
import com.logistics.financelegal.entities.ChartOfAccounts
import com.logistics.financelegal.entities.Contracts
import com.logistics.financelegal.entities.InsurancePolicies
import com.logistics.financelegal.entities.TaxDeclarations
import com.logistics.financelegal.services.FinanceLegalServiceImpl
import com.logistics.financelegal.services.AccountingServiceImpl
import com.logistics.financelegal.services.LegalServiceImpl
import com.logistics.financelegal.services.InsuranceServiceImpl
import com.logistics.financelegal.services.TaxationServiceImpl

val logger = LoggerFactory.getLogger("finance-legal-service")

fun main() {
    logger.info("Starting Finance-Legal Service...")
    
    val port = 50055
    val server = ServerBuilder.forPort(port)
        .addService(FinanceLegalServiceImpl())
        .addService(AccountingServiceImpl())
        .addService(LegalServiceImpl())
        .addService(InsuranceServiceImpl())
        .addService(TaxationServiceImpl())
        .build()
    
    // Initialize database
    Database.connect("jdbc:postgresql://localhost:5432/nodeorb", driver = "org.postgresql.Driver", 
        user = "postgres", password = "postgres")
    
    transaction {
        SchemaUtils.create(
            AccountingEntries,
            ChartOfAccounts,
            Contracts,
            InsurancePolicies,
            TaxDeclarations
        )
    }
    
    server.start()
    logger.info("Finance-Legal Service started on port $port")
    
    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            logger.info("Shutting down Finance-Legal Service...")
            server.shutdown()
            try {
                if (!server.awaitTermination(30, TimeUnit.SECONDS)) {
                    server.shutdownNow()
                }
            } catch (e: InterruptedException) {
                server.shutdownNow()
            }
        }
    })
    
    server.awaitTermination()
}