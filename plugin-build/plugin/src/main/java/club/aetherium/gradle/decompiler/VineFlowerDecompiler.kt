package club.aetherium.gradle.decompiler

import org.gradle.api.Project
import org.jetbrains.java.decompiler.main.Fernflower
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences
import java.io.File

object VineFlowerDecompiler {
    fun decompile(project: Project, jar: File, destination: File) {
        val options: MutableMap<String, Any> =
            mutableMapOf(
                IFernflowerPreferences.DECOMPILE_GENERIC_SIGNATURES to "1",
                IFernflowerPreferences.BYTECODE_SOURCE_MAPPING to "1",
                IFernflowerPreferences.REMOVE_SYNTHETIC to "1",
                IFernflowerPreferences.LOG_LEVEL to "trace",
                IFernflowerPreferences.INDENT_STRING to "\t",
            )

        val saver = ResultSaver { destination }

        val ff = Fernflower(saver, options, VineFlowerLogger(project.logger))

        ff.addSource(jar)

        try {
            ff.decompileContext()
        } finally {
            ff.clearContext()
        }
    }
}
