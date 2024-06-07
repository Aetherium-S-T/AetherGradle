import club.aetherium.gradle.api.impl.MixinExtension.Companion.mixin
import club.aetherium.gradle.extension.RunMode.Companion.tweaker

plugins {
    java
    id("club.aetherium.gradle")
}

repositories {
    mavenCentral()
    maven("https://raw.githubusercontent.com/BleachDev/cursed-mappings/main/")
    maven("https://maven.legacyfabric.net")
}

dependencies {
    mappings("net.legacyfabric:yarn:1.8.9+build.mcp")
}

minecraft {
    minecraftVersion = "1.8.9"
    runMode = tweaker("club.aetherium.example.Tweaker")
    gameExtensions =
        arrayOf(
            mixin(),
        )
}

java {
    version = JavaVersion.VERSION_1_8
}

tasks.compileJava {
    version = JavaVersion.VERSION_1_8
}
