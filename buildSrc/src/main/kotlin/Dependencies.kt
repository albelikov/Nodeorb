/**
 * Centralized dependency management for Nodeorb project
 * Kotlin 2.3.0, Gradle 9.3.0, Java 25
 */

object Kotlin {
    const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}"
    const val reflect = "org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}"
    const val coroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlin}"
    const val coroutinesReactor = "org.jetbrains.kotlinx:kotlinx-coroutines-reactor:${Versions.kotlin}"
}

object SpringBoot {
    const val starter = "org.springframework.boot:spring-boot-starter"
    const val starterWeb = "org.springframework.boot:spring-boot-starter-web"
    const val starterWebflux = "org.springframework.boot:spring-boot-starter-webflux"
    const val starterWebsocket = "org.springframework.boot:spring-boot-starter-websocket"
    const val starterDataJpa = "org.springframework.boot:spring-boot-starter-data-jpa"
    const val starterDataRedis = "org.springframework.boot:spring-boot-starter-data-redis"
    const val starterSecurity = "org.springframework.boot:spring-boot-starter-security"
    const val starterActuator = "org.springframework.boot:spring-boot-starter-actuator"
    const val starterValidation = "org.springframework.boot:spring-boot-starter-validation"
    const val starterOAuth2 = "org.springframework.boot:spring-boot-starter-oauth2-resource-server"
    const val starterTest = "org.springframework.boot:spring-boot-starter-test"
}

object Database {
    const val postgresql = "org.postgresql:postgresql:${Versions.postgresql}"
    const val flyway = "org.flywaydb:flyway-core:9.22.3"
    const val flywayPostgresql = "org.flywaydb:flyway-database-postgresql:9.22.3"
    const val h2 = "com.h2database:h2:2.2.224"
    const val testcontainers = "org.testcontainers:testcontainers:${Versions.testcontainers}"
    const val testcontainersJunit = "org.testcontainers:junit-jupiter:${Versions.testcontainers}"
    const val testcontainersPostgres = "org.testcontainers:postgresql:${Versions.testcontainers}"
}

object Redis {
    const val client = "io.lettuce:lettuce-core:6.2.13"
}

object Logging {
    const val logbackCore = "ch.qos.logback:logback-core"
    const val log4jApi = "org.apache.logging.log4j:log4j-api:2.21.1"
    const val log4jCore = "org.apache.logging.log4j:log4j-core:2.21.1"
    const val slf4j = "org.slf4j:slf4j-api:2.0.9"
    const val log4jOverSlf4j = "org.slf4j:log4j-over-slf4j:2.0.9"
    const val jclOverSlf4j = "org.slf4j:jcl-over-slf4j:2.0.9"
}

object Utils {
    const val snakeyaml = "org.yaml:snakeyaml:2.2"
    const val jacksonKotlin = "com.fasterxml.jackson.module:jackson-module-kotlin:2.17.1"
    const val jacksonDatabind = "com.fasterxml.jackson.core:jackson-databind:2.17.1"
    const val jacksonDataformat = "com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.17.1"
    
    // Для Java 25
    const val jaxbApi = "jakarta.xml.bind:jakarta.xml.bind-api:${Versions.jaxb}"
    const val jaxbRuntime = "org.glassfish.jaxb:jaxb-runtime:${Versions.jaxb}"
    const val javaxAnnotation = "javax.annotation:javax.annotation-api:${Versions.javaxAnnotation}"
}

object Testing {
    const val junit = "org.junit.jupiter:junit-jupiter:${Versions.junitJupiter}"
    const val mockitoCore = "org.mockito:mockito-core:5.8.0"
    const val mockitoJunit = "org.mockito:mockito-junit-jupiter:5.8.0"
    const val assertj = "org.assertj:assertj-core:3.24.2"
    const val mockk = "io.mockk:mockk:${Versions.mockk}"
}

object Docs {
    const val springdoc = "org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0"
}

object Kafka {
    const val clients = "org.apache.kafka:kafka-clients:${Versions.kafka}"
    const val springKafka = "org.springframework.kafka:spring-kafka"
}

object Grpc {
    const val kotlinStub = "io.grpc:grpc-kotlin-stub:${Versions.grpc}"
    const val protobuf = "io.grpc:grpc-protobuf:${Versions.grpc}"
    const val stub = "io.grpc:grpc-stub:${Versions.grpc}"
    const val nettyShaded = "io.grpc:grpc-netty-shaded:${Versions.grpc}"
}

object Protobuf {
    const val protoc = "com.google.protobuf:protoc:${Versions.protobuf}"
    const val grpcJava = "io.grpc:protoc-gen-grpc-java:${Versions.grpc}"
    const val grpcKotlin = "io.grpc:protoc-gen-grpc-kotlin:1.4.0:jdk8@jar"
}

object Compose {
    const val runtime = "org.jetbrains.compose:compose-runtime:${Versions.composeMultiplatform}"
    const val foundation = "org.jetbrains.compose:compose-foundation:${Versions.composeMultiplatform}"
    const val material3 = "org.jetbrains.compose:compose-material3:${Versions.composeMultiplatform}"
}

/**
 * Common dependencies for all services
 */
object Dependencies {
    // Kotlin
    val kotlinStdlib = listOf(Kotlin.stdlib, Kotlin.reflect)
    val kotlinCoroutines = listOf(Kotlin.coroutinesCore, Kotlin.coroutinesReactor)

    // Spring Boot
    val springBootWeb = listOf(SpringBoot.starterWeb, SpringBoot.starterWebflux)
    val springBootData = listOf(SpringBoot.starterDataJpa, SpringBoot.starterDataRedis)
    val springBootSecurity = listOf(SpringBoot.starterSecurity, SpringBoot.starterOAuth2)
    val springBootCommon = listOf(
        SpringBoot.starter,
        SpringBoot.starterActuator,
        SpringBoot.starterValidation
    )

    // Database
    val database = listOf(
        Database.postgresql,
        Database.flyway,
        Database.flywayPostgresql
    )

    // Logging
    val logging = listOf(
        Logging.logbackCore,
        Logging.log4jApi,
        Logging.log4jCore,
        Logging.slf4j,
        Logging.log4jOverSlf4j,
        Logging.jclOverSlf4j
    )

    // Utils
    val utils = listOf(
        Utils.snakeyaml,
        Utils.jacksonKotlin,
        Utils.jacksonDataformat,
        Utils.jaxbApi,
        Utils.jaxbRuntime,
        Utils.javaxAnnotation
    )

    // Testing
    val testing = listOf(
        SpringBoot.starterTest,
        Database.h2,
        Database.testcontainers,
        Database.testcontainersJunit,
        Database.testcontainersPostgres,
        Testing.junit,
        Testing.mockitoCore,
        Testing.mockitoJunit,
        Testing.assertj,
        Testing.mockk
    )
}