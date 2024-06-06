package club.aetherium.gradle.utils

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

fun extractZipFile(zipFile: String, outputDirectory: String, exclusions: List<String> = emptyList()) {
    val buffer = ByteArray(1024)
    val folder = File(outputDirectory)

    if (!folder.exists()) {
        folder.mkdir()
    }

    ZipInputStream(FileInputStream(zipFile)).use { zis ->
        var zipEntry = zis.nextEntry

        while (zipEntry != null) {
            val newFile = File(outputDirectory, zipEntry.name)

            if (!exclusions.any { zipEntry?.name!!.contains(it) }) {
                if(zipEntry.isDirectory) {
                    newFile.mkdirs()
                } else {
                    File(newFile.parent).mkdirs()
                    FileOutputStream(newFile).use { fos ->
                        var len: Int
                        while (zis.read(buffer).also { len = it } > 0) {
                            fos.write(buffer, 0, len)
                        }
                    }
                }
            } else {
                println("Excluding ${zipEntry.name}")
            }

            zipEntry = zis.nextEntry
        }
        zis.closeEntry()
    }
}
