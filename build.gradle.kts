plugins {
    id("org.jetbrains.kotlin.jvm") apply false
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management") apply false
}

allprojects {
    group = "com.nodeorb"
    version = "0.1.0-SNAPSHOT"
    // Репозитории отсюда УДАЛЕНЫ
}

tasks.register("cleanAll") {
    group = "build"
    dependsOn(subprojects.map { it.tasks.named("clean") })
}

tasks.register("buildAll") {
    group = "build"
    dependsOn(subprojects.map { it.tasks.named("build") })
}