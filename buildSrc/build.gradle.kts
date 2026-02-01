plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.21")
    implementation("org.springframework.boot:spring-boot-gradle-plugin:3.4.1")
    implementation("io.spring.gradle:dependency-management-plugin:1.1.6")
}

// Настройка обработки дубликатов для processResources
tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

// buildSrc использует версию Java, совместимую с Gradle
// Java 25 настраивается только в плагине logistics.service для проектов

