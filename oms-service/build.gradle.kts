plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

kotlin {
    jvmToolchain(25)
}

tasks.withType<JavaCompile> {
    targetCompatibility = "25"
    sourceCompatibility = "25"
}

springBoot {
    mainClass.set("com.logistics.oms.OmsApplicationKt")
}

dependencies {
    // Core Dependencies
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.3.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.3.0")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    
    // SCM Client SDK (temporarily removed)
    // implementation("com.nodeorb:scmclient:1.0.0")
    
    // Database
    implementation("org.postgresql:postgresql:42.6.0")
    implementation("org.flywaydb:flyway-core:8.5.13")
    implementation("org.hibernate.orm:hibernate-core:6.6.1.Final")
    implementation("org.hibernate:hibernate-spatial:6.6.1.Final")
    
    // Kafka
    implementation("org.springframework.kafka:spring-kafka:3.3.0")
    
    // LocationTech JTS для работы с геометрией
    implementation("org.locationtech.jts:jts-core:1.19.0")
    
    // Spring Cloud (временно отключено из-за конфликтов с Spring Boot 4.0.2)
    // implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    
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