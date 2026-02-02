plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

// Версия Spring Cloud для Spring Boot 3.2.x
extra["springCloudVersion"] = "2023.0.0"

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

springBoot {
    mainClass.set("com.logi.autonomousops.AutonomousOpsApplicationKt")
}

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    
    // Database
    implementation("org.postgresql:postgresql")
    
    // Spring Cloud - ДОБАВЛЕНО!
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    
    // IoT & Device Management
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")
    
    // Kafka for robot command/control
    implementation("org.springframework.kafka:spring-kafka")
    
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    
    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:postgresql:1.19.3")
    testImplementation("org.springframework.kafka:spring-kafka-test")
}