package club.aetherium.gradle.utils

import club.aetherium.gradle.extension.MinecraftExtension
import club.aetherium.gradle.utils.manifest.MinecraftManifest
import club.aetherium.gradle.utils.manifest.data.VersionData
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.internal.os.OperatingSystem
import java.io.File
import java.net.URL

object NativesTask {
    fun downloadAndExtractNatives(project: Project, extension: MinecraftExtension) {
        val nativesDirectory = project.projectDir
            .resolve("build")
            .resolve(".aether")
            .resolve("caches")
            .resolve("natives")

        if(!nativesDirectory.exists()) nativesDirectory.mkdirs()

        extension.minecraftVersion.orNull ?: throw GradleException("Version must be set")

        val mcManifest = MinecraftManifest.fromId(extension.minecraftVersion.get())
            ?: throw RuntimeException("Unknown version specified (${extension.minecraftVersion.get()})")

        val manifest = MinecraftManifest.gson.fromJson(
            URL(mcManifest.url).openStream().reader().readText(),
            VersionData::class.java
        )

        manifest.libraries.forEach {
            val classifiers = it.downloads.classifiers

            if (classifiers != null) {
                val native = when (OperatingSystem.current()) {
                    OperatingSystem.WINDOWS -> classifiers.nativesWindows
                    OperatingSystem.MAC_OS -> classifiers.nativesOsx
                    OperatingSystem.LINUX -> classifiers.nativesLinux
                    else -> null
                }
                if (native != null) {
                    val file = File(nativesDirectory, native.path)
                    if (!file.exists() || file.length() != native.size) {
                        file.parentFile.mkdirs()
                        file.createNewFile()
                        Downloader.download(native.url, file) {
                            val totalBoxes = 30
                            val neededBoxes = (it * totalBoxes).toInt()
                            val characters = "=".repeat(neededBoxes)
                            val whitespaces = " ".repeat(totalBoxes - neededBoxes)

                            project.logger.lifecycle(
                                "[AetherGradle] Natives: [$characters$whitespaces] (${it})\r"
                            )
                        }
                        var excludeList = emptyList<String>()
                        if (it.extract != null) {
                            excludeList = it.extract.exclude
                        }
                        extractZipFile(
                            file.absolutePath, nativesDirectory.absolutePath,
                            excludeList)
                    }
                }
            }
        }
    }
}
