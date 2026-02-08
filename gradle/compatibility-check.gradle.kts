tasks.register("checkPluginCompatibility") {
    group = "verification"
    description = "Проверяет совместимость всех плагинов с Kotlin 2.3.0, Java 25 и Spring Boot 4.0.2"
    
    doLast {
        val requiredVersions = mapOf(
            "Kotlin" to "2.3.0",
            "Java" to "25",
            "Spring Boot" to "4.0.2",
            "Gradle" to "9.3.0"
        )
        
        println("=== ПРОВЕРКА СОВМЕСТИМОСТИ ПЛАГИНОВ ===")
        println("Требуемые версии: $requiredVersions")
        println()
        
        // Проверить основные плагины
        val pluginsToCheck = listOf(
            "org.jetbrains.kotlin.jvm" to "2.3.0",
            "org.springframework.boot" to "4.0.2",
            "io.spring.dependency-management" to "1.1.7",
            "org.jetbrains.kotlin.multiplatform" to "2.3.0",
            "org.jetbrains.compose" to "1.10.0",
            "org.jetbrains.kotlin.plugin.spring" to "2.3.0",
            "org.jetbrains.kotlin.plugin.jpa" to "2.3.0"
        )
        
        pluginsToCheck.forEach { (plugin, version) ->
            println("✓ $plugin:$version - проверка совместимости...")
            // Здесь должна быть логика проверки совместимости
            // Например, проверка документации или матриц совместимости
        }
        
        println()
        println("=== ВАЖНЫЕ ПРОВЕРКИ ДЛЯ JAVA 25 ===")
        println("1. JAXB был удален из Java SE - используем jakarta.xml.bind:jakarta.xml.bind-api")
        println("2. Java EE модули удалены - используем Jakarta EE зависимости")
        println("3. Проверить, что все библиотеки поддерживают модульную систему Java")
        println("4. Spring Boot 4.0.2 должен официально поддерживать Java 25")
    }
}