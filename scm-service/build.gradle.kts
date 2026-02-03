plugins {
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.spring") version "2.3.0"
    kotlin("plugin.jpa") version "2.3.0"
    kotlin("plugin.allopen") version "2.3.0"
    id("org.springframework.boot") version "4.0.2"
    id("io.spring.dependency-management") version "1.1.6"
    id("com.google.protobuf") version "0.9.4"
    id("org.flywaydb.flyway") version "10.0.0"
    id("com.diffplug.spotless") version "6.25.0"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
}

group = "com.nodeorb"
version = "4.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xjsr305=strict",
            "-Xjvm-default=all",
            "-Xstring-concat=indy-with-constants",
            "-Xcontext-receivers",
            "-opt-in=kotlin.RequiresOptIn"
        )
        jvmTarget = JvmTarget.JVM_25
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.spring.io/milestone ")
        name = "Spring Milestone"
    }
}

val coroutinesVersion = "1.8.0"
val protobufVersion = "4.28.2"
val grpcVersion = "1.64.0"
val testContainersVersion = "1.19.0"
val opaClientVersion = "1.0.0"

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-graphql")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.kafka:spring-kafka")
    
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutinesVersion")
    
    // Database
    implementation("org.postgresql:postgresql")
    implementation("org.postgis:postgis-jdbc:3.6.0")
    implementation("com.clickhouse:clickhouse-jdbc:0.6.2")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    
    // Security & OAuth
    implementation("org.springframework.security:spring-security-oauth2-jose")
    implementation("org.keycloak:keycloak-spring-boot-starter:24.0.0")
    implementation("com.nimbusds:nimbus-jose-jwt:9.37.3")
    
    // gRPC & Protobuf
    implementation("io.grpc:grpc-kotlin-stub:2.0.0")
    implementation("io.grpc:grpc-protobuf:$grpcVersion")
    implementation("io.grpc:grpc-netty:$grpcVersion")
    implementation("com.google.protobuf:protobuf-kotlin:$protobufVersion")
    implementation("com.google.protobuf:protobuf-java-util:$protobufVersion")
    
    // Open Policy Agent
    implementation("org.openpolicyagent:opa-java-client:$opaClientVersion")
    implementation("org.openpolicyagent:opa-java-sdk:$opaClientVersion")
    
    // Monitoring & Observability
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("io.micrometer:micrometer-tracing-bridge-brave")
    implementation("io.zipkin.reporter2:zipkin-reporter-brave")
    implementation("io.micrometer:micrometer-observation")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    
    // Geospatial
    implementation("org.locationtech.jts:jts-core:1.19.0")
    implementation("org.geolatte:geolatte-geom:1.9.0")
    
    // Utils
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("org.apache.commons:commons-lang3:3.14.0")
    implementation("commons-codec:commons-codec:1.16.0")
    implementation("org.bouncycastle:bcpkix-jdk18on:1.78")
    implementation("io.github.microutils:kotlin-logging-jvm:4.0.0")
    
    // Caching
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
    
    // Development tools
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    
    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.mockito", module = "mockito-core")
    }
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("org.testcontainers:testcontainers:$testContainersVersion")
    testImplementation("org.testcontainers:postgresql:$testContainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testContainersVersion")
    testImplementation("org.testcontainers:kafka:$testContainersVersion")
    testImplementation("io.mockk:mockk:1.13.9")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
    testImplementation("com.tngtech.archunit:archunit-junit5:1.2.0")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:$protobufVersion"
    }
    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
        }
        create("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:2.0.0:jdk8@jar"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                create("grpc")
                create("grpckt")
            }
            it.builtins {
                create("kotlin")
            }
        }
    }
}

spotless {
    kotlin {
        target("src/**/*.kt")
        ktlint("0.52.0").editorConfigOverride(
            mapOf(
                "indent_size" to "4",
                "continuation_indent_size" to "4",
                "max_line_length" to "120",
                "ktlint_standard_no-wildcard-imports" to "disabled"
            )
        )
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    systemProperty("java.util.logging.config.file", "src/test/resources/logging-test.properties")
    jvmArgs("--enable-preview")
    maxParallelForks = Runtime.getRuntime().availableProcessors() / 2
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("--enable-preview")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xjvm-default=all",
            "-Xjsr305=strict",
            "-Xstring-concat=indy-with-constants",
            "-Xcontext-receivers",
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=kotlin.ExperimentalStdlibApi"
        )
    }
}

tasks.bootJar {
    archiveFileName.set("scm-service.jar")
    manifest {
        attributes(
            "Implementation-Version" to version,
            "Built-By" to "Nodeorb SCM Team",
            "Build-Timestamp" to java.time.Instant.now().toString()
        )
    }
}

tasks.register<JavaExec>("runOpa") {
    group = "application"
    description = "Run OPA for local development"
    classpath = configurations.runtimeClasspath
    mainClass.set("openpolicyagent/opa.cli.Main")
    args = listOf("run", "--server", "--addr=localhost:8181")
}