plugins {
    kotlin("jvm") version "1.9.20"
    application
}

group = "com.nodeorb"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    
    // gRPC
    implementation("io.grpc:grpc-stub:1.58.0")
    implementation("io.grpc:grpc-protobuf:1.58.0")
    implementation("io.grpc:grpc-netty-shaded:1.58.0")
    
    // Resilience4j
    implementation("io.github.resilience4j:resilience4j-circuitbreaker:2.0.2")
    implementation("io.github.resilience4j:resilience4j-retry:2.0.2")
    
    // Logging
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("ch.qos.logback:logback-classic:1.4.11")
    
    // WebAuthn Support
    implementation("com.yubico:java-webauthn-server-core:1.11.5")
    implementation("com.yubico:java-webauthn-server-attestation:1.11.5")
    
    // Security & Cryptography
    implementation("org.bouncycastle:bcprov-jdk15on:1.70")
    implementation("org.bouncycastle:bcpkix-jdk15on:1.70")
    
    // JSON Processing
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")
    
    // Testing
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("com.yubico:java-webauthn-server-core:1.11.5")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("com.nodeorb.scmclient.example.BiometricMarketplaceExampleKt")
}

// Generate gRPC stubs
sourceSets {
    main {
        java {
            srcDirs("build/generated/source/proto/main/grpc")
            srcDirs("build/generated/source/proto/main/java")
        }
    }
}

tasks.register<Exec>("generateProto") {
    commandLine("protoc", 
        "--proto_path=src/main/proto",
        "--java_out=build/generated/source/proto/main/java",
        "--grpc_out=build/generated/source/proto/main/grpc",
        "--plugin=protoc-gen-grpc=protoc-gen-grpc-java",
        "src/main/proto/scm_service.proto",
        "src/main/proto/identity_service.proto"
    )
    outputs.dir("build/generated/source/proto/main")
}

tasks.named("compileKotlin") {
    dependsOn("generateProto")
}

tasks.named("compileTestKotlin") {
    dependsOn("generateProto")
}

// Configure WebAuthn dependencies
configurations.all {
    resolutionStrategy {
        force("com.fasterxml.jackson.core:jackson-databind:2.15.2")
        force("com.fasterxml.jackson.core:jackson-annotations:2.15.2")
        force("com.fasterxml.jackson.core:jackson-core:2.15.2")
    }
}

// Add source generation task for WebAuthn
tasks.register<JavaExec>("generateWebAuthnKeys") {
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("com.nodeorb.scmclient.handler.WebAuthnKeyGenerator")
    args = listOf("src/main/resources/keys")
    outputs.dir("src/main/resources/keys")
}

// Clean task to remove generated files
tasks.clean {
    delete("build/generated")
    delete("src/main/resources/keys")
}