pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        // Kotlin 플러그인 버전 (최신 안정화 버전 확인 후 사용 권장, 현재 1.9.22 예시)
        id("org.jetbrains.kotlin.android") version "1.9.22" apply false

    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "JJB2.0"
include(":app")
 