package club.aetherium.gradle

import club.aetherium.gradle.extension.MinecraftExtension
import club.aetherium.gradle.tasks.DownloadAndRemapJarTask
import club.aetherium.gradle.tasks.GenerateSourcesTask
import club.aetherium.gradle.tasks.run.DownloadAssetsTask
import club.aetherium.gradle.tasks.run.RunClientTask
import club.aetherium.gradle.utils.NativesTask
import club.aetherium.gradle.utils.manifest.MinecraftManifest
import club.aetherium.gradle.utils.manifest.MinecraftManifest.gson
import club.aetherium.gradle.utils.manifest.data.VersionData
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.TaskInternal
import org.gradle.api.tasks.compile.AbstractCompile
import java.net.URI
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

        val downloadAssetsTask = project.tasks.register("downloadAssets", DownloadAssetsTask::class.java) {
            it.group = "AetherGradle"
        }

        val runClientTask = project.tasks.register("runClient", RunClientTask::class.java) {
            it.group = "AetherGradle"
            it.dependsOn(downloadAssetsTask)
            it.dependsOn(project.tasks.withType(AbstractCompile::class.java).matching { that ->
                !that.name.lowercase().contains("test")
            })
            it.mainClass.set(extension.runMode.get().mainClass)
        }

        project.afterEvaluate {
            val mcManifest = MinecraftManifest.fromId(extension.minecraftVersion.get())
                ?: throw RuntimeException("Unknown version specified (${extension.minecraftVersion.get()})")

            val manifest = gson.fromJson(
                URL(mcManifest.url).openStream().reader().readText(),
                VersionData::class.java
            ) ?: throw RuntimeException("Failed to fetch version manifest")

            // Natives
            NativesTask.downloadAndExtractNatives(project, extension)

            // Deps
            project.repositories.add(
                project.repositories.maven {
                    it.url = project.uri("https://libraries.minecraft.net/")
                }
            )

            project.repositories.add(
                project.repositories.mavenLocal()
            )

            //  Libraries
            manifest.libraries.forEach {
                if(!it.name.contains("platform")) {
                    project.logger.info("Registering library ${it.name}")
                    project.dependencies.add("implementation", it.name)
                    project.dependencies.add("implementation",
                        "com.mojang:minecraft-deobf:${extension.minecraftVersion.get()}")
                }
            }

            // RunMode
            val mode = extension.runMode.get()

            mode.additionalRepositories.forEach { dep ->
                project.repositories.add(project.repositories.maven {
                    it.url = project.uri(dep)
                })
            }

            mode.additionalDependencies.forEach { dep ->
                project.dependencies.add("implementation", dep)
            }
        }
    }
}
