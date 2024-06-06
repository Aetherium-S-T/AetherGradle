plugins {
    java
    id("club.aetherium.gradle")
}

minecraft {
    minecraftVersion = "1.8.9"
}

repositories {
    mavenCentral()
    maven("https://raw.githubusercontent.com/BleachDev/cursed-mappings/main/")
    maven("https://maven.legacyfabric.net")
}

dependencies {
    add("mappings", "net.legacyfabric:yarn:1.8.9+build.mcp")
}
