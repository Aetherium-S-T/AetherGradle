package club.aetherium.gradle.tasks.run

import club.aetherium.gradle.extension.MinecraftExtension
import club.aetherium.gradle.utils.Downloader
import club.aetherium.gradle.utils.manifest.MinecraftManifest
import club.aetherium.gradle.utils.manifest.MinecraftManifest.gson
import club.aetherium.gradle.utils.manifest.data.VersionData
import com.google.gson.JsonObject
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.URL
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.CompletableFuture

private const val RESOURCES_URL = "https://resources.download.minecraft.net/"

abstract class DownloadAssetsTask : DefaultTask() {
    @OutputDirectory
    val assetsDirectory = project.projectDir.resolve("build")
        .resolve(".aether").resolve("assets")
    private val minecraftVersion: Property<String>
        get() = project.extensions.getByType(MinecraftExtension::class.java).minecraftVersion

    private val aetherCache = project.projectDir.resolve("build").resolve(".aether")

    @TaskAction
    fun downloadAssets() {
        minecraftVersion.orNull ?: throw GradleException("Version must be set")

        val mcManifest = MinecraftManifest.fromId(minecraftVersion.get())
            ?: throw RuntimeException("Unknown version specified (${minecraftVersion.get()})")

        val manifest = gson.fromJson(
            URL(mcManifest.url).openStream().reader().readText(),
            VersionData::class.java
        ) ?: throw RuntimeException("Failed to fetch version manifest")

        val get = URL(manifest.assetIndex.url).readText()

        val assetIndex = aetherCache.resolve("indexes/${manifest.assetIndex.id}.json")

        if (!assetIndex.exists()) {
            assetIndex.parentFile.mkdirs()
            assetIndex.createNewFile()
            assetIndex.writeText(get)
        }

        val json = gson.fromJson(get, JsonObject::class.java)
        val objects = json["objects"].asJsonObject

        var downloaded = AtomicInteger(0)

        fun assetDownloaded() {
            val prog = downloaded.get().toFloat() / objects.size().toFloat()

            val totalBoxes = 30
            val neededBoxes = (prog * totalBoxes).toInt()
            val characters = "=".repeat(neededBoxes)
            val whitespaces = " ".repeat(totalBoxes - neededBoxes)

            logger.lifecycle(
                "\r[AetherGradle] Assets: [$characters$whitespaces] (${prog * 100})\r"
            )
        }

        objects.asMap().forEach { (_, value) ->
            val hash = value.asJsonObject["hash"]!!.asString
            val folder = assetsDirectory.resolve("objects/" + hash.substring(0, 2))
            val file = File(folder, hash)
            val size = value.asJsonObject["size"]!!.asLong

            logger.lifecycle(
                "${file.exists()} | ${file.length()} / $size"
            )

            logger.lifecycle(
                "${file.exists()} | ${Downloader.hash(file)} / $hash"
            )

            if ((!file.exists() || file.length() != size) &&
                !Downloader.hash(file).equals(hash, ignoreCase = true)) {
                val url = "$RESOURCES_URL${hash.substring(0, 2)}/$hash"
                val response = URL(url).readText()

                file.parentFile.mkdirs()
                file.writeText(response)
                downloaded.incrementAndGet()
                assetDownloaded()
            }
        }
    }
}
