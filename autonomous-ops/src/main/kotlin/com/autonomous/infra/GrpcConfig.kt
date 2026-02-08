package com.autonomous.infra

import io.grpc.Server
import io.grpc.ServerBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GrpcConfig(
    @Value("\${grpc.server.port:50051}")
    private val grpcPort: Int
) {

    @Bean
    fun grpcServer(): Server {
        return ServerBuilder.forPort(grpcPort)
            .build()
    }
}