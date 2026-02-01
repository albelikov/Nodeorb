pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "org.springframework.boot") {
                useModule("org.springframework.boot:spring-boot-gradle-plugin:${requested.version}")
            }
        }
    }
}

rootProject.name = "nodeorb"
include("admin-backend", "admin-frontend", "autonomous-ops", "customs-service",
        "fms-service", "freight-marketplace", "oms-service",
        "scm-service", "tms-service", "transport-platform", "wms-service")
