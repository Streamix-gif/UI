pluginManagement {
    plugins {
        id("com.android.library") version "8.11.1"
        id("org.jetbrains.kotlin.android") version "2.3.0"
    }
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositoriesMode.set(org.gradle.api.initialization.resolve.RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral(); maven("https://jitpack.io") }
}
rootProject.name = "streamix-backend"
include(":social-server")
