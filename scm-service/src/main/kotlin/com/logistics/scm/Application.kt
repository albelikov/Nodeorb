package com.logistics.scm

import com.logistics.scm.grpc.ValidationServiceImpl
import com.logistics.scm.grpc.AppealServiceImpl
import com.logistics.scm.validation.ValidationServiceGrpc
import com.logistics.scm.validation.AppealServiceGrpc
import io.grpc.ServerBuilder
import io.grpc.protobuf.services.ProtoReflectionService
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

fun main() {
    // Initialize database connection
    val dbUrl = System.getenv("DATABASE_URL") ?: "jdbc:postgresql://localhost:5432/scm_service"
    val dbUser = System.getenv("DATABASE_USER") ?: "postgres"
    val dbPassword = System.getenv("DATABASE_PASSWORD") ?: "password"
    
    Database.connect(dbUrl, driver = "org.postgresql.Driver", user = dbUser, password = dbPassword)
    
    // Create tables if they don't exist
    createTables()
    
    // Start gRPC server
    val server = ServerBuilder
        .forPort(8080)
        .addService(ValidationServiceImpl())
        .addService(AppealServiceImpl())
        .addService(ProtoReflectionService.newInstance())
        .build()
    
    server.start()
    println("SCM Service started on port 8080")
    
    // Graceful shutdown
    Runtime.getRuntime().addShutdownHook(Thread {
        println("Shutting down gRPC server...")
        server.shutdown()
        println("Server shut down")
    })
    
    server.awaitTermination()
}

private fun createTables() {
    transaction {
        // Create tables using Exposed DSL
        exec("""
            CREATE TABLE IF NOT EXISTS market_price_medians (
                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                category VARCHAR(50) NOT NULL,
                region_id VARCHAR(100) NOT NULL,
                median_value DECIMAL(15,2) NOT NULL,
                updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                UNIQUE(category, region_id)
            )
        """.trimIndent())
        
        exec("""
            CREATE TABLE IF NOT EXISTS manual_entry_validation (
                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                order_id VARCHAR(255) NOT NULL,
                input_value DECIMAL(15,2) NOT NULL,
                deviation DOUBLE PRECISION NOT NULL,
                verdict VARCHAR(20) NOT NULL,
                previous_hash VARCHAR(64),
                current_hash VARCHAR(64) NOT NULL,
                created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                INDEX idx_order_id (order_id),
                INDEX idx_verdict (verdict),
                INDEX idx_created_at (created_at)
            )
        """.trimIndent())
    }
}