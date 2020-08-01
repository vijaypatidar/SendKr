package com.vkpapps.thunder.connection

import android.net.Uri
import androidx.core.net.toFile
import com.vkpapps.thunder.App
import com.vkpapps.thunder.analitics.Logger
import com.vkpapps.thunder.interfaces.OnFileRequestReceiverListener
import com.vkpapps.thunder.model.FileRequest
import com.vkpapps.thunder.ui.activity.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

/***
 * @author VIJAY PATIDAR
 */
class FileService(private val send: Boolean,
                  private val onFileRequestReceiverListener: OnFileRequestReceiverListener,
                  private val rid: String,
                  private val uri: Uri,
                  private val clientHelper: ClientHelper,
                  private val isDirectory: Boolean) : Runnable {

    @Throws(IOException::class)
    private fun getSocket(): Socket {
        lateinit var socket: Socket
        if (MainActivity.isHost) {
            ServerSocket(PORT).use { serverSocket ->
                serverSocket.soTimeout = MAX_WAIT_TIME
                socket = serverSocket.accept()
            }
        } else {
            socket = Socket()
            socket.connect(InetSocketAddress(HOST_ADDRESS, PORT))
        }
        return socket
    }

    override fun run() {
        try {
            if (send) {
                handleActionSend()
            } else {
                handleActionReceive()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleActionReceive() {
        Logger.d("handleActionReceive $uri")
        try {
            if (MainActivity.isHost) {
                if (!clientHelper.connected) throw  Exception("client disconnected")
                clientHelper.write(FileRequest(FileRequest.UPLOAD_REQUEST_CONFIRM, rid))
                onFileRequestReceiverListener.onRequestAccepted(rid, clientHelper.user.userId, true)
            }
            val socket = getSocket()
            val init = System.currentTimeMillis()
            val file = uri.toFile()
            if (isDirectory) {
                val zipUtils = ZipUtils()
                CoroutineScope(Default).launch {
                    while (!socket.isClosed) {
                        onFileRequestReceiverListener.onProgressChange(rid, zipUtils.transferred)
                        delay(PROGRESS_UPDATE_TIME)
                    }
                    onFileRequestReceiverListener.onProgressChange(rid, zipUtils.transferred)
                }
                zipUtils.openInputOutStream(socket.getInputStream(), file)
            } else {
                val `in` = socket.getInputStream()
                val out: OutputStream = FileOutputStream(file)
                val bytes = ByteArray(3000)
                var count: Int
                var transferredByte: Long = 0
                CoroutineScope(Default).launch {
                    while (!socket.isClosed) {
                        onFileRequestReceiverListener.onProgressChange(rid, transferredByte)
                        delay(PROGRESS_UPDATE_TIME)
                    }
                    onFileRequestReceiverListener.onProgressChange(rid, transferredByte)
                }
                while (`in`.read(bytes).also { count = it } > 0) {
                    out.write(bytes, 0, count)
                    transferredByte += count
                }
                `in`.close()
                out.flush()
                out.close()
                socket.close()
            }
            val timeTaken = System.currentTimeMillis() - init
            onFileRequestReceiverListener.onRequestSuccess(rid, timeTaken, false)
        } catch (e: Exception) {
            onFileRequestReceiverListener.onRequestFailed(rid)
            e.printStackTrace()
        }
    }

    private fun handleActionSend() {
        Logger.d("handleActionSend $uri")
        try {
            if (MainActivity.isHost) {
                if (!clientHelper.connected) throw  Exception("client disconnected")
                clientHelper.write(FileRequest(FileRequest.DOWNLOAD_REQUEST_CONFIRM, rid))
                onFileRequestReceiverListener.onRequestAccepted(rid, clientHelper.user.userId, true)
            }
            val socket = getSocket()
            val init = System.currentTimeMillis()
            if (isDirectory) {
                val zipUtils = ZipUtils()
                CoroutineScope(Default).launch {
                    while (!socket.isClosed) {
                        onFileRequestReceiverListener.onProgressChange(rid, zipUtils.transferred)
                        delay(PROGRESS_UPDATE_TIME)
                    }
                    onFileRequestReceiverListener.onProgressChange(rid, zipUtils.transferred)
                }
                zipUtils.openZipOutStream(socket.getOutputStream(), uri.toFile())
            } else {
                val inputStream: InputStream = App.context.contentResolver.openInputStream(uri)!!
                val outputStream = socket.getOutputStream()
                val bytes = ByteArray(3000)
                var count: Int
                var transferredByte: Long = 0
                CoroutineScope(Default).launch {
                    while (!socket.isClosed) {
                        onFileRequestReceiverListener.onProgressChange(rid, transferredByte)
                        delay(PROGRESS_UPDATE_TIME)
                    }
                    onFileRequestReceiverListener.onProgressChange(rid, transferredByte)
                }
                while (inputStream.read(bytes).also { count = it } > 0) {
                    outputStream.write(bytes, 0, count)
                    transferredByte += count
                }
                outputStream.flush()
                outputStream.close()
                inputStream.close()
                socket.close()
            }
            val timeTaken = (System.currentTimeMillis() - init)
            onFileRequestReceiverListener.onRequestSuccess(rid, timeTaken, true)
        } catch (e: Exception) {
            onFileRequestReceiverListener.onRequestFailed(rid)
            e.printStackTrace()
        }
    }


    companion object {
        @JvmField
        var HOST_ADDRESS: String? = null
        private const val MAX_WAIT_TIME = 1500
        private const val PORT = 7511
        private const val PROGRESS_UPDATE_TIME: Long = 2000

        fun startActionSend(onFileRequestReceiverListener: OnFileRequestReceiverListener, rid: String, uri: Uri, clientHelper: ClientHelper, isDirectory: Boolean) {
            synchronized(App.taskExecutor) {
                App.taskExecutor.submit(FileService(
                        true, onFileRequestReceiverListener, rid, uri, clientHelper, isDirectory
                ))
            }
        }

        fun startActionReceive(onFileRequestReceiverListener: OnFileRequestReceiverListener, uri: Uri, rid: String, clientHelper: ClientHelper, isDirectory: Boolean) {
            synchronized(App.taskExecutor) {
                App.taskExecutor.submit(FileService(
                        false, onFileRequestReceiverListener, rid, uri, clientHelper, isDirectory
                ))
            }
        }

    }

}