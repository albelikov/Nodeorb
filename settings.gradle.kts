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
include("admin-panel", "autonomous-ops", "common-core", "customs-service", "cyber-resilience",
        "driver-app", "driver-service", "fms-service", "freight-marketplace",
        "geography-service", "integration-service", "notification-service", "oms-service",
        "order-service", "payload-service", "rating-service", "reverse-logistics",
        "route-service", "scm-audit", "scm-data-protection", "scm-iam",
        "tms-service", "tracking-service", "transport-platform", "wms-service", "yms-service")