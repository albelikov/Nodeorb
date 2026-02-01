plugins {
    id("logistics.service")
    alias(libs.plugins.kotlin.spring)
}

dependencies {
    // Spring Boot
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    
    // GIS & Spatial (minimal)
    implementation(libs.bundles.gis.bundle.minimal)
    implementation(libs.postgresql)
    
    // Testing
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.testcontainers.postgresql)
}

