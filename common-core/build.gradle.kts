plugins {
    kotlin("jvm")
    `java-library`
}

kotlin {
    jvmToolchain(17)
}

java {
    withSourcesJar()
}

// Отключаем bootJar для библиотеки
tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = true
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
}