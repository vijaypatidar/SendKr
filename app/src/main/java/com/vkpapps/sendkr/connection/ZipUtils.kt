package com.vkpapps.sendkr.connection

import androidx.documentfile.provider.DocumentFile
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import com.vkpapps.sendkr.model.RequestInfo
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class ZipUtils(private val requestInfo: RequestInfo) {
    private var bi = 0

    @Throws(IOException::class)
    fun openZipOutStream(outputStream: OutputStream, path: File) {
        bi = path.absolutePath.length + 1
        val zos = ZipOutputStream(outputStream)
        addEntry(zos, path)
        zos.flush()
        zos.close()
    }

    @Throws(IOException::class)
    private fun addEntry(zos: ZipOutputStream, path: File) {
        if (path.isDirectory) {
            for (file in DocumentFile.fromFile(path).listFiles()) {
                addEntry(zos, File(path, file.name!!))
            }
        } else if (path.length() > 0) {
            val zipEntry = ZipEntry(path.absolutePath.substring(bi))
            zos.putNextEntry(zipEntry)
            val `in`: InputStream = FileInputStream(path)
            var read: Int
            while (`in`.read(FileService.BUFFER).also { read = it } > 0) {
                zos.write(FileService.BUFFER, 0, read)
                requestInfo.transferred += read
            }
            zos.closeEntry()
            `in`.close()
        }
    }

    @Throws(IOException::class)
    fun openInputStream(inputStream: InputStream, path: File) {
        val zis = ZipInputStream(inputStream)
        var entry: ZipEntry? = zis.nextEntry
        while (entry != null) {
            val fileName: String = entry.name
            val file = File(path, fileName)
            file.parentFile?.mkdirs()
            val fos = FileOutputStream(file)
            var read: Int
            while (zis.read(FileService.BUFFER).also { read = it } > 0) {
                fos.write(FileService.BUFFER, 0, read)
                requestInfo.transferred += read
            }
            zis.closeEntry()
            entry = zis.nextEntry
        }
        zis.close()
    }
}