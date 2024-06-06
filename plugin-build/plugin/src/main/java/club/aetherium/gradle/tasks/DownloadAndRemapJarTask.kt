package club.aetherium.gradle.tasks

import club.aetherium.gradle.extension.MinecraftExtension
import club.aetherium.gradle.utils.Downloader
import club.aetherium.gradle.utils.Mapper
import club.aetherium.gradle.utils.manifest.MinecraftManifest
import club.aetherium.gradle.utils.manifest.MinecraftManifest.gson
import club.aetherium.gradle.utils.manifest.data.VersionData
import net.fabricmc.tinyremapper.NonClassCopyMode
import net.fabricmc.tinyremapper.OutputConsumerPath
import net.fabricmc.tinyremapper.TinyRemapper
import net.fabricmc.tinyremapper.TinyUtils
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.IOException
import java.net.URL

abstract class DownloadAndRemapJarTask : DefaultTask() {
    private val minecraftVersion: Property<String>
        get() = project.extensions.getByType(MinecraftExtension::class.java).minecraftVersion

    @OutputFile
    val remappedJar = project.projectDir.resolve("build")
        .resolve(".aether").resolve("client_${minecraftVersion.get()}_remapped.jar")


    private lateinit var manifest: VersionData
    private lateinit var downloadedJar: File

    @TaskAction
    fun fetchManifests() {
        project.logger.lifecycle("[AetherGradle] Fetching version manifests")

        minecraftVersion.orNull ?: throw GradleException("Version must be set")

        val mcManifest = MinecraftManifest.fromId(minecraftVersion.get())
            ?: throw RuntimeException("Unknown version specified (${minecraftVersion.get()})")

        manifest = gson.fromJson(
            URL(mcManifest.url).openStream().reader().readText(),
            VersionData::class.java
        ) ?: throw RuntimeException("Failed to fetch version manifest")

        downloadedJar = project.projectDir.resolve("build")
            .resolve(".aether").resolve("client_${minecraftVersion.get()}.jar")

        project.logger.lifecycle("[AetherGradle] Downloading client")

        Downloader.download(
            manifest.downloads.client.url, downloadedJar,
            manifest.downloads.client.sha1
        ) {
            val totalBoxes = 30
            val neededBoxes = (it * totalBoxes).toInt()
            val characters = "=".repeat(neededBoxes)
            val whitespaces = " ".repeat(totalBoxes - neededBoxes)

            print(
                "[AetherGradle] Client: [$characters$whitespaces] (${it})\r"
            )
        }
        val mappings = Mapper.extractTinyMappingsFromJar(
            project.configurations.getByName("mappings").resolve().toList()[0]
        )

        project.logger.lifecycle("[AetherGradle] Remapping client")

        val remapper = TinyRemapper.newRemapper()
            .withMappings(TinyUtils.createTinyMappingProvider(mappings,
                "official", "named"))
            .skipLocalVariableMapping(true)
            .ignoreConflicts(true)
            .ignoreFieldDesc(true)
            .resolveMissing(false)
            .build()

        try {
            OutputConsumerPath.Builder(remappedJar.toPath()).build().use { outputConsumer ->
                outputConsumer.addNonClassFiles(downloadedJar.toPath(),
                    NonClassCopyMode.FIX_META_INF, remapper)
                remapper.readInputs(downloadedJar.toPath())
                remapper.apply(outputConsumer)
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        } finally {
            remapper.finish()
        }
    }
}
