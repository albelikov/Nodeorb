import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.android.application")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlinx.kover")
}

kotlin {
    androidTarget {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
    }
    
    jvm("desktop")
    
    js(IR) {
        browser()
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.components.resources)
                
                // Networking
                implementation("io.ktor:ktor-client-core:2.3.11")
                implementation("io.ktor:ktor-client-content-negotiation:2.3.11")
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.11")
                implementation("io.ktor:ktor-client-logging:2.3.11")
                
                // Serialization
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
                
                // Coroutines
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
            }
        }
        
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
            }
        }
        
        val androidMain by getting {
            dependencies {
                implementation(compose.ui)
                implementation(compose.uiTooling)
                implementation(compose.preview)
                implementation("androidx.activity:activity-compose:1.8.2")
                implementation("androidx.appcompat:appcompat:1.6.1")
                implementation("androidx.core:core-ktx:1.12.0")
                
                implementation("io.ktor:ktor-client-android:2.3.11")
            }
        }
        
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation("io.ktor:ktor-client-java:2.3.11")
            }
        }
        
        val jsMain by getting {
            dependencies {
                implementation(compose.html.core)
                implementation("io.ktor:ktor-client-js:2.3.11")
            }
        }
    }
}

android {
    namespace = "com.logistics.admin.frontend"
    compileSdk = 34
    
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    
    defaultConfig {
        applicationId = "com.logistics.admin.frontend"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    
    buildFeatures {
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }
}

compose.desktop {
    application {
        mainClass = "com.logistics.admin.frontend.MainKt"
        
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "LogisticsAdminFrontend"
            packageVersion = "1.0.0"
            
            windows {
                menu = true
                upgradeUuid = "12345678-1234-1234-1234-123456789012"
            }
            
            macOS {
                bundleID = "com.logistics.admin.frontend"
            }
        }
    }
}

compose.experimental {
    web.application {}
}