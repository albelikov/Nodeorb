plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

springBoot {
    mainClass.set("com.nodeorb.freight.marketplace.FreightMarketplaceApplicationKt")
}

dependencies {
    // Core Dependencies
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    
    // Database
    implementation("org.postgresql:postgresql:42.6.0")
    implementation("org.flywaydb:flyway-core:9.22.3")
    implementation("org.flywaydb:flyway-database-postgresql:9.22.3")
    
    // Spatial/Geospatial dependencies
    implementation("org.hibernate.orm:hibernate-spatial:6.4.1.Final")
    implementation("net.postgis:postgis-jdbc:2023.1.0")
    implementation("org.locationtech.jts:jts-core:1.19.0")
    
    // Kafka
    implementation("org.springframework.kafka:spring-kafka:3.3.0")
    
    // Spring Cloud
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client:4.0.0")
    
    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("org.mockito:mockito-core:5.8.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.8.0")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.kafka:spring-kafka-test:3.3.0")
    testImplementation("io.mockk:mockk:1.13.8")
}

tasks.test {
    useJUnitPlatform()
}
tasks.test {
    useJUnitPlatform()
}
