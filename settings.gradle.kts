// Gradle settings: plugin/dependency repositories and the two modules (domain, app).
// (Comments in this file were written by AI - Claude.)
// Where Gradle looks for plugins. The Google repo is limited to Android/Google/AndroidX groups.
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
}
// Lets Gradle download the right JDK automatically.
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
// Every module must use these repositories (no per-module repositories allowed).
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "OnlySticks"
// Pure Kotlin game logic, no Android dependencies.
include(":domain")
// The Android app.
include(":app")
