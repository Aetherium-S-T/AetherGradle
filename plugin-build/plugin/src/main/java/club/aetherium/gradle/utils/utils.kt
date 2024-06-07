package club.aetherium.gradle.utils

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URI
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.util.Optional
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.io.path.exists
import kotlin.io.path.outputStream

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

fun <T> Optional<T>.orElseOptional(invoke: () -> Optional<T>): Optional<T> {
    return if (isPresent) {
        this
    } else {
        invoke()
    }
}

fun <T> Path.readZipInputStreamFor(path: String, throwIfMissing: Boolean = true, action: (InputStream) -> T): T {
    Files.newInputStream(this).use { fileInputStream ->
        ZipInputStream(fileInputStream).use { zipInputStream ->
            var entry = zipInputStream.nextEntry
            while (entry != null) {
                if (entry.name == path.replace("\\", "/")) {
                    return action.invoke(zipInputStream)
                }
                entry = zipInputStream.nextEntry
            }
            if (throwIfMissing) {
                throw IllegalArgumentException("Missing file $path in $this")
            }
        }
    }
    return null as T
}
fun Path.forEachInZip(action: (String, InputStream) -> Unit) {
    Files.newInputStream(this).use { fileInputStream ->
        ZipInputStream(fileInputStream).use { zipInputStream ->
            var entry: ZipEntry? = zipInputStream.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    action(entry.name, zipInputStream)
                }
                entry = zipInputStream.nextEntry
            }
        }
    }
}

fun Path.openZipFileSystem(vararg args: Pair<String, Any>): FileSystem {
    return openZipFileSystem(args.associate { it })
}

fun Path.openZipFileSystem(args: Map<String, *> = mapOf<String, Any>()): FileSystem {
    if (!exists() && args["create"] == true) {
        ZipOutputStream(outputStream()).use { stream ->
            stream.closeEntry()
        }
    }
    return FileSystems.newFileSystem(URI.create("jar:${toUri()}"), args, null)
}
