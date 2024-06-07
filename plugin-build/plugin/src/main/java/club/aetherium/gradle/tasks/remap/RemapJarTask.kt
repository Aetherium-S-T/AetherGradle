package club.aetherium.gradle.tasks.remap

import club.aetherium.gradle.utils.Mapper
import net.fabricmc.tinyremapper.NonClassCopyMode
import net.fabricmc.tinyremapper.OutputConsumerPath
import net.fabricmc.tinyremapper.TinyRemapper
import net.fabricmc.tinyremapper.TinyUtils
import net.fabricmc.tinyremapper.extension.mixin.MixinExtension
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.IOException

abstract class RemapJarTask : DefaultTask() {
    @get:InputFile
    abstract val inputJar: Property<File>

    @get:OutputFile
    abstract val outputJar: Property<File>

    @get:Input
    abstract val sourceNamespace: Property<String>

    @get:Input
    abstract val targetNamespace: Property<String>

    @TaskAction
    fun remap() {
        val mappings = Mapper.extractTinyMappingsFromJar(
            project.configurations.getByName("mappings").resolve().toList()[0]
        )

        val remapper = TinyRemapper.newRemapper()
            .withMappings(
                TinyUtils.createTinyMappingProvider(mappings,
                sourceNamespace.get(), targetNamespace.get()))
            .skipLocalVariableMapping(true)
            .ignoreConflicts(true)
            .ignoreFieldDesc(true)
            .resolveMissing(false)
            .extension(MixinExtension())
            .build()

        try {
            OutputConsumerPath.Builder(outputJar.get().toPath()).build().use { outputConsumer ->
                outputConsumer.addNonClassFiles(inputJar.get().toPath(),
                    NonClassCopyMode.FIX_META_INF, remapper)
                remapper.readClassPath(
                    *project.configurations.getByName("runtimeClasspath")
                        .map { it.toPath() }.toTypedArray()
                )
                remapper.readInputs(inputJar.get().toPath())
                remapper.apply(outputConsumer)
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        } finally {
            remapper.finish()
        }
    }
}
