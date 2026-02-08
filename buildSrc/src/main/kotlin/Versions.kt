object Versions {
    // Ядро проекта
    const val kotlin = "2.3.0"
    const val java = "25"
    const val gradle = "9.3.0"
    const val springBoot = "4.0.2"
    
    // Kotlin Multiplatform & Compose
    const val kotlinMultiplatform = "2.3.0"
    const val composeMultiplatform = "1.10.0"
    const val material3 = "1.2.1"
    
    // Базы данных
    const val postgresql = "42.7.1"
    const val postgis = "3.6.1"
    
    // Очереди сообщений
    const val kafka = "4.1.1"
    
    // Тестирование
    const val junitJupiter = "5.10.0"
    const val testcontainers = "1.19.0"
    const val mockk = "1.13.7"
    
    // ДОПОЛНИТЕЛЬНЫЕ ЗАВИСИМОСТИ - ПРОВЕРИТЬ СОВМЕСТИМОСТИМОСТИ
    const val springDependencyManagement = "1.1.7"
    const val protobuf = "3.25.3"
    const val grpc = "1.60.0"
    const val liquibase = "4.25.0"
    const val jaxb = "4.0.4"  // Для Java 25 (JAXB был удален из Java SE)
    const val javaxAnnotation = "1.3.2"  // Для Java 25
    
    // Проверка: совместимы ли эти версии с Java 25?
    // Java 25 удалила некоторые модули, нужны альтернативы
}