pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://api.xposed.info/") {
            content {
                includeGroup("de.robv.android.xposed")
            }
        }
        maven("https://jitpack.io") {}
    }
}

plugins {
    id("de.fayard.refreshVersions") version "0.60.5"
}

rootProject.name = "aodwallpaper"

include(":app")