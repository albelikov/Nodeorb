plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(25)) }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25)
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

dependencies {
    implementation(Kotlin.stdlib)
    implementation(Kotlin.reflect)
    implementation(Utils.jacksonKotlin)
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
}