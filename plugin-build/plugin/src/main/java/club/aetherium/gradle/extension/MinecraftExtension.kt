package club.aetherium.gradle.extension

import org.gradle.api.Project
import org.gradle.api.provider.Property
import javax.inject.Inject

@Suppress("UnnecessaryAbstractClass")
abstract class MinecraftExtension
    @Inject
    constructor(project: Project) {
        private val objects = project.objects

        val minecraftVersion: Property<String> = objects.property(String::class.java)
    }
