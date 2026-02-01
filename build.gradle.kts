import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.3.0" apply false
    kotlin("plugin.spring") version "2.3.0" apply false
    kotlin("plugin.jpa") version "2.3.0" apply false
    id("org.springframework.boot") version "4.0.2" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

group = "com.nodeorb"
version = "0.1.0-SNAPSHOT"

allprojects {
    repositories {
        mavenCentral()
        google()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")

    group = rootProject.group
    version = rootProject.version

    dependencies {
        // Kotlin stdlib
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        
        // Jackson for Kotlin
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
        
        // Logging
        implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
        
        // Testing
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "21"
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

// Task для сборки всех сервисов
tasks.register("buildAll") {
    dependsOn(subprojects.map { it.tasks.named("build") })
    group = "build"
    description = "Build all microservices"
}

// Task для очистки всех сервисов
tasks.register("cleanAll") {
    dependsOn(subprojects.map { it.tasks.named("clean") })
    group = "build"
    description = "Clean all microservices"
}
