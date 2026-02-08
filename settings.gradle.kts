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
    "admin-backend", "autonomous-ops", "customs-service",
    "fms-service", "freight-marketplace", "oms-service",
    "scm-service", "tms-service", "wms-service", "finance-legal-service",
    "shared-domain", "pkg:scmclient"
)
project(":pkg:scmclient").name = "scmclient"
