package club.aetherium.gradle.tasks

import club.aetherium.gradle.decompiler.VineFlowerDecompiler
import club.aetherium.gradle.extension.MinecraftExtension
import club.aetherium.gradle.utils.extractZipFile
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class GenerateSourcesTask : DefaultTask() {
    private val minecraftVersion: Property<String>
        get() = project.extensions.getByType(MinecraftExtension::class.java).minecraftVersion

    private val remappedJar: File = project.projectDir.resolve("build")
        .resolve(".aether").resolve("client_${minecraftVersion.get()}_remapped.jar")

    @TaskAction
    fun installToMavenLocal() {
        val localMavenRepoDir = File("${System.getProperties()["user.home"]}/.m2/repository")
        val group = "com.mojang"
        val artifactId = "minecraft-deobf"
        val version = minecraftVersion.get()

        val targetDirectory = localMavenRepoDir.resolve("$group/$artifactId/$version")
        val targetJarFile = targetDirectory.resolve("$artifactId-$version.jar")

        if(!targetDirectory.exists()) targetDirectory.mkdirs()

        remappedJar.copyTo(targetJarFile, true)

        project.logger.lifecycle("[AetherGradle] Installed deobfuscated Minecraft version: $version to maven local")
        project.logger.lifecycle("[AetherGradle] To use the dependency: ")
        project.logger.lifecycle("[AetherGradle] Add this to your repositories block: \nrepositories {\nmavenLocal()\n}")
        project.logger.lifecycle("[AetherGradle] Add this to your dependencies block: \ndependencies {\nimplementation(\"$group:$artifactId:$version\")\n}")
        project.logger.lifecycle("\n\n[AetherGradle] Thank you for using AetherGradle <3")
        project.logger.lifecycle("        - Made with love by Refactoring")
    }
}
