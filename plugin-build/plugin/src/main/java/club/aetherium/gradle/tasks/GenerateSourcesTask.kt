package club.aetherium.gradle.tasks

import club.aetherium.gradle.decompiler.VineFlowerDecompiler
import club.aetherium.gradle.extension.MinecraftExtension
import club.aetherium.gradle.utils.extractZipFile
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class GenerateSourcesTask : DefaultTask() {
    private val minecraftVersion: Property<String>
        get() = project.extensions.getByType(MinecraftExtension::class.java).minecraftVersion

    private val remappedJar: File = project.projectDir.resolve("build")
        .resolve(".aether").resolve("client_${minecraftVersion.get()}_remapped.jar")
    private val localMavenRepoDir = File("${System.getProperties()["user.home"]}/.m2/repository")
    private val group = "com.mojang"
    private val artifactId = "minecraft-deobf"
    private val version = minecraftVersion.get()

    private val targetDirectory = localMavenRepoDir.resolve("${group.replace(".", "/")}/$artifactId/$version")

    @OutputFile
    val targetJarFile = targetDirectory.resolve("$artifactId-$version.jar")
    @OutputFile
    val targetPomFile = targetDirectory.resolve("$artifactId-$version.pom")

    @TaskAction
    fun installToMavenLocal() {
        if(!targetDirectory.exists()) targetDirectory.mkdirs()

        remappedJar.copyTo(targetJarFile, true)

        targetPomFile.writeText("""
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
              <modelVersion>4.0.0</modelVersion>
              <groupId>${group}</groupId>
              <artifactId>${artifactId}</artifactId>
              <version>${version}</version>
            </project>
        """.trimIndent())

        project.logger.lifecycle("[AetherGradle] Installed deobfuscated Minecraft version: $version to maven local")
        project.logger.lifecycle("[AetherGradle] To add Minecraft to your classpath: ")
        project.logger.lifecycle("[AetherGradle] Add this to your repositories block: \nrepositories {\n    mavenLocal()\n}")
        project.logger.lifecycle("[AetherGradle] Add this to your dependencies block: \ndependencies {\n    implementation(\"$group:$artifactId:$version\")\n}")
        project.logger.lifecycle("\n\n[AetherGradle] Thank you for using AetherGradle <3")
        project.logger.lifecycle("        - Made with love by Refactoring")
    }
}
