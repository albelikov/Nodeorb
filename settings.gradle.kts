pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        // Добавляем все специфические репозитории здесь:
        maven { url = uri("https://repo.osgeo.org/repository/release/") }
        maven { url = uri("https://download.java.net/maven/2/") }
    }
}

rootProject.name = "nodeorb"

include(
    "admin-backend", "admin-frontend", "autonomous-ops", "customs-service",
    "fms-service", "freight-marketplace", "oms-service",
    "scm-service", "tms-service", "transport-platform", "wms-service"
)