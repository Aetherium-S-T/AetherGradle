package club.aetherium.gradle.utils

import org.apache.commons.codec.digest.DigestUtils
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream

object Downloader {
    fun download(url: String, destination: File, hash: String? = null,
                 progressCallback: (Float) -> Unit) {
        if(hash != null && destination.exists()) {
            if(hash(destination).equals(hash, ignoreCase = true)) {
                return
            }
        }

        val connection = URL(url).openConnection()
        val inputStream = BufferedInputStream(connection.getInputStream())
        val fileOutputStream = FileOutputStream(destination)

        val buffer = ByteArray(1024)
        var bytesRead: Int

        var totalBytesRead = 0L
        val fileSize = connection.contentLength.toLong()

        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            fileOutputStream.write(buffer, 0, bytesRead)
            totalBytesRead += bytesRead

            val progress = (totalBytesRead * 100 / fileSize).toInt()
            progressCallback(progress / 100f)
        }

        fileOutputStream.close()
        inputStream.close()
    }

    private fun hash(file: File): String {
        Files.newInputStream(file.toPath()).use { `in` ->
            return DigestUtils.sha1Hex(`in`)
        }
    }
}
