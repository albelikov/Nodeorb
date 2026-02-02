/**
 * Centralized dependency management for Logi project
 * Kotlin 2.3.0, Gradle 9.3.0, Java 25
 */

object Versions {
    // Core
    const val kotlin = "2.3.0"
    const val springBoot = "4.0.2"
    const val springCloud = "2023.0.0"

    // Database
    const val postgresql = "42.6.0"
    const val flyway = "9.22.3"
    const val h2 = "2.2.224"

    // Redis
    const val redis = "6.2.13"

    // Testing
    const val testcontainers = "1.19.3"
    const val mockito = "5.8.0"
    const val junit = "5.10.1"

    // Utils
    const val snakeyaml = "2.2"
    const val jackson = "2.16.0"
    const val log4j = "2.21.1"
    const val slf4j = "2.0.9"

    // Documentation
    const val springdoc = "2.3.0"

    // Message Queues
    const val kafka = "3.6.1"
    const val springKafka = "3.3.0"

    // Geographic Information Systems
    const val hibernateSpatial = "6.4.1.Final"
    const val postgis = "2023.1.0"
    const val jts = "1.19.0"
    const val mockk = "1.13.8"

    // Spring Cloud
    const val springCloudEureka = "4.0.0"
    const val springWebsocket = "3.2.0"
}

object Kotlin {
    const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib"
    const val reflect = "org.jetbrains.kotlin:kotlin-reflect"
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
    const val flyway = "org.flywaydb:flyway-core:${Versions.flyway}"
    const val flywayPostgresql = "org.flywaydb:flyway-database-postgresql:${Versions.flyway}"
    const val h2 = "com.h2database:h2:${Versions.h2}"
    const val testcontainers = "org.testcontainers:testcontainers:${Versions.testcontainers}"
    const val testcontainersJunit = "org.testcontainers:junit-jupiter:${Versions.testcontainers}"
    const val testcontainersPostgres = "org.testcontainers:postgresql:${Versions.testcontainers}"
}

object Redis {
    const val client = "io.lettuce:lettuce-core:${Versions.redis}"
}

object Logging {
    const val logbackCore = "ch.qos.logback:logback-core"
    const val log4jApi = "org.apache.logging.log4j:log4j-api:${Versions.log4j}"
    const val log4jCore = "org.apache.logging.log4j:log4j-core:${Versions.log4j}"
    const val slf4j = "org.slf4j:slf4j-api:${Versions.slf4j}"
    const val log4jOverSlf4j = "org.slf4j:log4j-over-slf4j:${Versions.slf4j}"
    const val jclOverSlf4j = "org.slf4j:jcl-over-slf4j:${Versions.slf4j}"
}

object Utils {
    const val snakeyaml = "org.yaml:snakeyaml:${Versions.snakeyaml}"
    const val jacksonKotlin = "com.fasterxml.jackson.module:jackson-module-kotlin:${Versions.jackson}"
    const val jacksonDatabind = "com.fasterxml.jackson.core:jackson-databind:${Versions.jackson}"
    const val jacksonDataformat = "com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:${Versions.jackson}"
}

object Testing {
    const val junit = "org.junit.jupiter:junit-jupiter:${Versions.junit}"
    const val mockitoCore = "org.mockito:mockito-core:${Versions.mockito}"
    const val mockitoJunit = "org.mockito:mockito-junit-jupiter:${Versions.mockito}"
    const val assertj = "org.assertj:assertj-core:3.24.2"
}

object Docs {
    const val springdoc = "org.springdoc:springdoc-openapi-starter-webmvc-ui:${Versions.springdoc}"
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
        Utils.jacksonDataformat
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
        Testing.assertj
    )
}
