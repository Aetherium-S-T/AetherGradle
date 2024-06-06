package club.aetherium.gradle.decompiler

import org.jetbrains.java.decompiler.main.DecompilerContext
import org.jetbrains.java.decompiler.main.extern.IResultSaver
import java.io.*
import java.nio.charset.StandardCharsets
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.function.Supplier
import java.util.jar.JarOutputStream
import java.util.jar.Manifest
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.math.max


class ResultSaver(private val output: Supplier<File>) :
    IResultSaver {
    var outputStreams: MutableMap<String, ZipOutputStream> = HashMap()
    var saveExecutors: MutableMap<String, ExecutorService> = HashMap()
    var lineMapWriter: PrintWriter? = null

    override fun createArchive(path: String, archiveName: String, manifest: Manifest) {
        val key = "$path/$archiveName"
        val file = output.get()

        try {
            val fos = FileOutputStream(file)
            val zos = if (manifest == null) ZipOutputStream(fos) else JarOutputStream(fos, manifest)
            outputStreams[key] = zos
            saveExecutors[key] = Executors.newSingleThreadExecutor()
        } catch (e: IOException) {
            throw RuntimeException("Unable to create archive: $file", e)
        }
    }

    override fun saveClassEntry(
        path: String,
        archiveName: String,
        qualifiedName: String,
        entryName: String,
        content: String
    ) {
        this.saveClassEntry(path, archiveName, qualifiedName, entryName, content, null)
    }

    override fun saveClassEntry(
        path: String,
        archiveName: String,
        qualifiedName: String,
        entryName: String,
        content: String,
        mapping: IntArray?
    ) {
        val key = "$path/$archiveName"
        val executor = saveExecutors[key]
        executor!!.submit {
            val zos = outputStreams[key]
            try {
                zos!!.putNextEntry(ZipEntry(entryName))

                if (content != null) {
                    zos.write(content.toByteArray(StandardCharsets.UTF_8))
                }
            } catch (e: IOException) {
                DecompilerContext.getLogger().writeMessage("Cannot write entry $entryName", e)
            }
            if (mapping != null && lineMapWriter != null) {
                var maxLine = 0
                var maxLineDest = 0
                val builder = StringBuilder()

                var i = 0
                while (i < mapping.size) {
                    maxLine = max(maxLine.toDouble(), mapping[i].toDouble()).toInt()
                    maxLineDest = max(maxLineDest.toDouble(), mapping[i + 1].toDouble()).toInt()
                    builder.append("\t").append(mapping[i]).append("\t").append(mapping[i + 1]).append("\n")
                    i += 2
                }

                lineMapWriter!!.println(qualifiedName + "\t" + maxLine + "\t" + maxLineDest)
                lineMapWriter!!.println(builder.toString())
            }
        }
    }

    override fun closeArchive(path: String, archiveName: String) {
        val key = "$path/$archiveName"
        val executor = saveExecutors[key]
        val closeFuture = executor!!.submit {
            val zos = outputStreams[key]
            try {
                zos!!.close()
            } catch (e: IOException) {
                throw RuntimeException("Unable to close zip. $key", e)
            }
        }
        executor.shutdown()

        try {
            closeFuture.get()
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        } catch (e: ExecutionException) {
            throw RuntimeException(e)
        }

        outputStreams.remove(key)
        saveExecutors.remove(key)

        if (lineMapWriter != null) {
            lineMapWriter!!.flush()
            lineMapWriter!!.close()
        }
    }

    override fun saveFolder(path: String) {
    }

    override fun copyFile(source: String, path: String, entryName: String) {
    }

    override fun saveClassFile(
        path: String,
        qualifiedName: String,
        entryName: String,
        content: String,
        mapping: IntArray
    ) {
    }

    override fun saveDirEntry(path: String, archiveName: String, entryName: String) {
    }

    override fun copyEntry(source: String, path: String, archiveName: String, entry: String) {
    }
}
