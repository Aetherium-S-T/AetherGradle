package club.aetherium.gradle.tasks.remap

import club.aetherium.gradle.extension.MinecraftExtension
import club.aetherium.gradle.mixin.MixinRemapExtension
import club.aetherium.gradle.utils.Mapper
import club.aetherium.gradle.utils.openZipFileSystem
import net.fabricmc.tinyremapper.NonClassCopyMode
import net.fabricmc.tinyremapper.OutputConsumerPath
import net.fabricmc.tinyremapper.TinyRemapper
import net.fabricmc.tinyremapper.TinyUtils
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.io.File
import java.io.IOException

@DisableCachingByDefault(because = "Not worth caching")
abstract class RemapJarTask : DefaultTask() {
    @get:InputFile
    abstract val inputJar: Property<File>

    @get:OutputFile
    abstract val outputJar: Property<File>

    @get:Input
    abstract val sourceNamespace: Property<String>

    @get:Input
    abstract val targetNamespace: Property<String>

    private val minecraftVersion: Property<String>
        get() = project.extensions.getByType(MinecraftExtension::class.java).minecraftVersion

    private val downloadedJar = project.projectDir.resolve("build")
        .resolve(".aether").resolve("client_${minecraftVersion.get()}.jar")

    @TaskAction
    fun remap() {
        val mappings = Mapper.extractTinyMappingsFromJar(
            project.configurations.getByName("mappings").resolve().toList()[0]
        )

        if(!downloadedJar.exists()) {
            throw GradleException("Cannot find obfuscated Minecraft. Please run :generateSources")
        }

        val mre = MixinRemapExtension()

        val remapperConf = TinyRemapper.newRemapper()
            .withMappings(
                TinyUtils.createTinyMappingProvider(mappings,
                sourceNamespace.get(), targetNamespace.get()))
            .skipLocalVariableMapping(true)
            .skipLocalVariableMapping(true)
            .ignoreConflicts(true)
            .threads(Runtime.getRuntime().availableProcessors())

        mre.enableBaseMixin()
        remapperConf.extension(mre)

        val remapper = remapperConf.build()
        val classpath = listOf(*project.configurations.getByName("runtimeClasspath")
            .map { it.toPath() }.toTypedArray(),
            // We need the obfuscated client jar in the classpath
            downloadedJar.toPath())

        logger.lifecycle("[AetherGradle/RemapJar] Remapping ${inputJar.get().nameWithoutExtension} from ${sourceNamespace.get()} to ${targetNamespace.get()}")

        val tag = remapper.createInputTag()

        logger.lifecycle("[AetherGradle/RemapJar] Tag: $tag")

        mre.readClassPath(remapper, *classpath.toTypedArray())

        logger.lifecycle("[AetherGradle/RemapJar] Reading....")
        mre.readInput(remapper, tag, inputJar.get().toPath())

        logger.lifecycle("[AetherGradle/RemapJar] Writing....")
        try {
            OutputConsumerPath.Builder(outputJar.get().toPath()).build().use { outputConsumer ->
                outputConsumer.addNonClassFiles(inputJar.get().toPath(),
                    NonClassCopyMode.FIX_META_INF, remapper)
                remapper.apply(outputConsumer, tag)
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        remapper.finish()

        outputJar.get().toPath()
            .openZipFileSystem(mapOf("mutable" to true)).use {
                mre.insertExtra(tag, it)
            }

        logger.lifecycle("[AetherGradle/RemapJar] Remapped successfully")
    }
}
