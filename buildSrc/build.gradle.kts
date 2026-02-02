plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.0")
    implementation("org.springframework.boot:spring-boot-gradle-plugin:4.0.2")
    implementation("io.spring.gradle:dependency-management-plugin:1.1.7")
    implementation("org.jetbrains.kotlin:kotlin-serialization:2.3.0")
    implementation("com.android.tools.build:gradle:8.2.2")
    implementation("org.jetbrains.compose:compose-gradle-plugin:1.6.11")
    implementation("org.jetbrains.kotlinx:kover-gradle-plugin:0.7.4")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.0")
    implementation("org.jetbrains.kotlin:compose-compiler-gradle-plugin:2.3.0")
}