package com.logistics.customs

import io.grpc.Server
import io.grpc.ServerBuilder
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

val logger = LoggerFactory.getLogger("customs-service")

fun main() {
    logger.info("Starting Customs Service...")
    
    val port = 50056
    val server = ServerBuilder.forPort(port)
        .addService(CustomsServiceImpl())
        .build()
    
    // Initialize database
    Database.connect("jdbc:postgresql://localhost:5432/nodeorb", driver = "org.postgresql.Driver", 
        user = "postgres", password = "postgres")
    
    transaction {
        SchemaUtils.create(
            CustomsDeclarations,
            CustomsDocuments,
            CustomsPayments,
            GoodsClassifications
        )
    }
    
    server.start()
    logger.info("Customs Service started on port $port")
    
    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            logger.info("Shutting down Customs Service...")
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