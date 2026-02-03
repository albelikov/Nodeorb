plugins {
    kotlin("jvm") version "1.9.20"
    kotlin("plugin.serialization") version "1.9.20"
    application
}

group = "com.logistics.scm"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    
    // gRPC
    implementation("io.grpc:grpc-kotlin-stub:1.4.0")
    implementation("io.grpc:grpc-protobuf:1.58.0")
    implementation("io.grpc:grpc-stub:1.58.0")
    implementation("io.grpc:grpc-netty-shaded:1.58.0")
    
    // Exposed ORM
    implementation("org.jetbrains.exposed:exposed-core:0.47.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.47.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.47.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.47.0")
    
    // Database drivers
    implementation("org.postgresql:postgresql:42.6.0")
    
    // ClickHouse integration
    implementation("ru.yandex.clickhouse:clickhouse-jdbc:0.4.6")
    
    // Kafka integration
    implementation("org.apache.kafka:kafka-clients:3.6.1")
    
    // JSON processing
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.1")
    
    // Logging
    implementation("ch.qos.logback:logback-classic:1.4.11")
    implementation("org.slf4j:slf4j-api:2.0.9")
    
    // Testing
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.9.20")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("com.logistics.scm.ApplicationKt")
}

// Generate gRPC code from proto files
sourceSets {
    main {
        proto {
            srcDir("src/main/proto")
        }
        java {
            srcDirs("build/generated/source/proto/main/grpc")
            srcDirs("build/generated/source/proto/main/grpckt")
            srcDirs("build/generated/source/proto/main/java")
        }
    }
}

tasks.register<Exec>("generateProto") {
    commandLine("protoc", 
        "--proto_path=src/main/proto",
        "--kotlin_out=build/generated/source/proto/main/kotlin",
        "--grpc-kotlin_out=build/generated/source/proto/main/kotlin",
        "--java_out=build/generated/source/proto/main/java",
        "--grpc_out=build/generated/source/proto/main/java",
        "src/main/proto/validation.proto")
    
    outputs.dir("build/generated/source/proto")
}

tasks.register<Exec>("generateGrpcKotlin") {
    commandLine("protoc",
        "--plugin=protoc-gen-grpc-kotlin=build/install/grpc-kotlin-cli/bin/protoc-gen-grpc-kotlin",
        "--proto_path=src/main/proto",
        "--grpc-kotlin_out=build/generated/source/proto/main/kotlin",
        "src/main/proto/validation.proto")
    
    outputs.dir("build/generated/source/proto")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn("generateProto", "generateGrpcKotlin")
}

tasks.register<Jar>("fatJar") {
    archiveClassifier.set("fat")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    from(sourceSets.main.get().output)
    manifest {
        attributes["Main-Class"] = application.mainClass
    }
}