plugins {
    id("org.jetbrains.kotlin.jvm") apply false
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management") apply false
}

allprojects {
    group = "com.nodeorb"
    version = "0.1.0-SNAPSHOT"
    
    repositories {
        mavenCentral()
        maven { url = uri("https://repo.osgeo.org/repository/release/") }
        maven { url = uri("https://download.java.net/maven/2/") }
    }
    
    // Configure Java toolchain for JDK 25
    plugins.withType<JavaPlugin> {
        java {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(25))
            }
        }
    }
}

tasks.register("cleanAll") {
    group = "build"
    dependsOn(subprojects.map { it.tasks.named("clean") })
}

tasks.register("buildAll") {
    group = "build"
    dependsOn(subprojects.map { it.tasks.named("build") })
}
