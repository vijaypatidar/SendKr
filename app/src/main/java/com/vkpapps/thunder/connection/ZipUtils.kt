package com.vkpapps.thunder.connection

import androidx.documentfile.provider.DocumentFile
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class ZipUtils {
    private val buffer = FileService.BUFFER
    private var bi = 0
    var transferred: Long = 0

    @Throws(IOException::class)
    fun openZipOutStream(outputStream: OutputStream, path: File) {
        bi = path.absolutePath.length + 1
        val zos = ZipOutputStream(outputStream)
        addEntry(zos, path)
        zos.close()
    }

    @Throws(IOException::class)
    private fun addEntry(zos: ZipOutputStream, path: File) {
        if (path.isDirectory) {
            for (file in DocumentFile.fromFile(path).listFiles()) {
                addEntry(zos, File(path, file.name!!))
            }
        } else {
            val zipEntry = ZipEntry(path.absolutePath.substring(bi))
            zos.putNextEntry(zipEntry)
            val `in`: InputStream = FileInputStream(path)
            var read: Int
            while (`in`.read(buffer).also { read = it } > 0) {
                zos.write(buffer, 0, read)
                transferred += read
            }
            zos.flush()
            zos.closeEntry()
            `in`.close()
        }
    }

    @Throws(IOException::class)
    fun openInputOutStream(inputStream: InputStream, path: File) {
        val zis = ZipInputStream(inputStream)
        var entry: ZipEntry? = zis.nextEntry
        while (entry != null) {
            val fileName: String = entry.name
            val file = File(path, fileName)
            file.parentFile?.mkdirs()
            val fos = FileOutputStream(file)
            var read: Int
            while (zis.read(buffer).also { read = it } > 0) {
                fos.write(buffer, 0, read)
                transferred += read
            }
            fos.flush()
            fos.close()
            zis.closeEntry()
            entry = zis.nextEntry
        }
        zis.close()
    }
}