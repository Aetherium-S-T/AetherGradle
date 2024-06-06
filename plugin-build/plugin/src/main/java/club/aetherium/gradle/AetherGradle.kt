package club.aetherium.gradle

import club.aetherium.gradle.extension.MinecraftExtension
import club.aetherium.gradle.tasks.DownloadAndRemapJarTask
import club.aetherium.gradle.tasks.GenerateSourcesTask
import club.aetherium.gradle.utils.NativesTask
import club.aetherium.gradle.utils.manifest.MinecraftManifest
import club.aetherium.gradle.utils.manifest.MinecraftManifest.gson
import club.aetherium.gradle.utils.manifest.data.VersionData
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.net.URL

abstract class AetherGradle : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("minecraft",
            MinecraftExtension::class.java, project)

        project.configurations.create("mappings")

        val downloadAndRemapJarTask = project.tasks.register("downloadAndRemapJar", DownloadAndRemapJarTask::class.java) {
            it.group = "AetherGradle"
        }
        val generateSourcesTask = project.tasks.register("generateSources", GenerateSourcesTask::class.java) {
            it.group = "AetherGradle"
            it.dependsOn(downloadAndRemapJarTask)
        }

        project.afterEvaluate {
            val mcManifest = MinecraftManifest.fromId(extension.minecraftVersion.get())
                ?: throw RuntimeException("Unknown version specified (${extension.minecraftVersion.get()})")

            val manifest = gson.fromJson(
                URL(mcManifest.url).openStream().reader().readText(),
                VersionData::class.java
            ) ?: throw RuntimeException("Failed to fetch version manifest")

            //  Natives
            NativesTask.downloadAndExtractNatives(project, extension)

            project.repositories.add(
                project.repositories.maven {
                    it.url = project.uri("https://libraries.minecraft.net/")
                }
            )

            //  Libraries
            manifest.libraries.forEach {
                if(!it.name.contains("platform")) {
                    project.logger.info("Registering library ${it.name}")
                    project.dependencies.add("implementation", it.name)
                }
            }
        }
    }
}
