package club.aetherium.gradle

import club.aetherium.gradle.extension.TemplateExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

abstract class TemplatePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("minecraft", TemplateExtension::class.java, project)

    }
}
