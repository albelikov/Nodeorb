plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.9.21"
    application
    id("com.google.protobuf")
}

group = "com.logistics.financelegal"
version = "1.0.0"

dependencies {
    implementation(Kotlin.stdlib)
    implementation(Kotlin.reflect)
    
    // gRPC
    implementation(Grpc.kotlinStub)
    implementation(Grpc.protobuf)
    implementation(Grpc.stub)
    implementation(Grpc.nettyShaded)
    
    // Exposed ORM
    implementation("org.jetbrains.exposed:exposed-core:0.47.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.47.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.47.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.47.0")
    
    // Database drivers
    implementation(Database.postgresql)
    
    // ClickHouse integration
    implementation("ru.yandex.clickhouse:clickhouse-jdbc:0.4.6")
    
    // Kafka integration
    implementation(Kafka.clients)
    
    // JSON processing
    implementation(Utils.jacksonDatabind)
    implementation(Utils.jacksonKotlin)
    
    // Statistical analysis
    implementation("org.apache.commons:commons-math3:3.6.1")
    
    // Logging
    implementation("ch.qos.logback:logback-classic:1.4.11")
    implementation(Logging.slf4j)
    
    // Testing
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:${Versions.kotlin}")
    testImplementation(Testing.junit)
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${Versions.junitJupiter}")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(25)
}

application {
    mainClass.set("com.logistics.financelegal.ApplicationKt")
}

// Generate gRPC code from proto files
protobuf {
    protoc {
        artifact = Protobuf.protoc
    }
    plugins {
        create("grpc") {
            artifact = Protobuf.grpcJava
        }
        create("grpckt") {
            artifact = Protobuf.grpcKotlin
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                create("grpc")
                create("grpckt")
            }
        }
    }
}

sourceSets {
    main {
        proto {
            srcDir("api/grpc")
        }
        java {
            srcDirs("build/generated/source/proto/main/grpc")
            srcDirs("build/generated/source/proto/main/grpckt")
            srcDirs("build/generated/source/proto/main/java")
        }
    }
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