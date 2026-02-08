plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    // ОСНОВНЫЕ ПЛАГИНЫ (из стека Nodeorb)
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.0")
    implementation("org.springframework.boot:spring-boot-gradle-plugin:4.0.2")
    
    // ПРОВЕРИТЬ СОВМЕСТИМОСТЬ ЭТИХ ПЛАГИНОВ:
    // 1. Kotlin Multiplatform plugin
    implementation("org.jetbrains.kotlin.multiplatform:org.jetbrains.kotlin.multiplatform.gradle.plugin:2.3.0")
    
    // 2. Compose Multiplatform plugin
    implementation("org.jetbrains.compose:compose-gradle-plugin:1.10.0") {
        // Проверить совместимость с Kotlin 2.3.0
        because("Compose 1.10.0 должен быть совместим с Kotlin 2.3.0")
    }
    
    // 3. Spring Dependency Management
    implementation("io.spring.gradle:dependency-management-plugin:1.1.7") {
        // Проверить совместимость с Spring Boot 4.0.2
        because("Spring Boot 4.0.2 требует определённую версию dependency-management")
    }
    
    // 4. ДОПОЛНИТЕЛЬНЫЕ ПЛАГИНЫ - ПРОВЕРИТЬ СОВМЕСТИМОСТЬ:
    // Если в проекте используются, проверить совместимость с Kotlin 2.3.0 и Java 25
    
    // 4.1. Плагины для тестирования
    implementation("org.jetbrains.kotlin:kotlin-allopen:2.3.0") // Для Spring
    implementation("org.jetbrains.kotlin:kotlin-noarg:2.3.0")   // Для JPA
    
    // 4.2. Плагины для кодогенерации
    implementation("com.google.protobuf:protobuf-gradle-plugin:0.9.4") {
        // Проверить совместимость с Java 25 и Gradle 9.3
        because("Protobuf plugin должен поддерживать Java 25")
    }
    
    // 4.3. Плагины для Docker
    implementation("com.bmuschko:gradle-docker-plugin:9.4.0") {
        // Проверить совместимость с Gradle 9.3
        because("Docker plugin должен работать с Gradle 9.3.0")
    }
    
    // 4.4. Плагины для миграций БД
    implementation("org.liquibase:liquibase-gradle-plugin:2.2.1") {
        // Проверить совместимость с PostgreSQL 18
        because("Liquibase должен поддерживать PostgreSQL 18")
    }
    
    // 4.5. Плагины для анализа кода
    implementation("org.jlleitschuh.gradle:ktlint-gradle:11.6.1") {
        // Проверить совместимость с Kotlin 2.3.0
        because("Ktlint должен поддерживать Kotlin 2.3.0")
    }
    
    // 4.6. Плагины для покрытия кода
    implementation("org.jetbrains.kotlinx:kover-gradle-plugin:0.7.4") {
        // Проверить совместимость с Kotlin 2.3.0
        because("Kover должен поддерживать Kotlin 2.3.0")
    }
    
    constraints {
        // ОБЯЗАТЕЛЬНО: Проверить, что ВСЕ плагины совместимы между собой
        // и не конфликтуют по версиям зависимостей
    }
}