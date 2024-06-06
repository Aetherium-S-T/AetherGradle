package club.aetherium.gradle.utils

import java.io.BufferedReader
import java.io.File
import java.util.jar.JarFile

object Mapper {
    fun extractTinyMappingsFromJar(file: File): BufferedReader? {
        val jarFile = JarFile(file)
        for (entry in jarFile.entries()) {
            if(!entry.isDirectory) {
                if(entry.name.endsWith(".tiny")) {
                    return jarFile.getInputStream(entry).bufferedReader()
                }
            }
        }
        return null
    }
}
