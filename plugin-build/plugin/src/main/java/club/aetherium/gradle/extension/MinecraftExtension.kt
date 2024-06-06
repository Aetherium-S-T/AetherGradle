package club.aetherium.gradle.extension

import org.gradle.api.Project
import org.gradle.api.provider.Property
import javax.inject.Inject

@Suppress("UnnecessaryAbstractClass")
abstract class TemplateExtension
    @Inject
    constructor(project: Project) {
        private val objects = project.objects

        val version: Property<String> = objects.property(String::class.java)
    }
