pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.fabricmc.net")
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
        maven("https://maven.fabricmc.net")
    }
}

plugins {
    `gradle-enterprise`
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
        publishAlwaysIf(System.getenv("GITHUB_ACTIONS") == "true")
        publishOnFailure()
    }
}

rootProject.name = "AetherGradle"

include(":example")
includeBuild("plugin-build")
