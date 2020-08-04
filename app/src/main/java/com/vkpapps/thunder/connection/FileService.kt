package com.vkpapps.thunder.connection

import android.net.Uri
import androidx.core.net.toFile
import com.vkpapps.thunder.App
import com.vkpapps.thunder.analitics.Logger
import com.vkpapps.thunder.interfaces.OnFileRequestReceiverListener
import com.vkpapps.thunder.model.FileRequest
import com.vkpapps.thunder.model.RequestInfo
import com.vkpapps.thunder.ui.activity.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
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
                  private val requestInfo: RequestInfo,
                  private val uri: Uri,
                  private val clientHelper: ClientHelper,
                  private val isDirectory: Boolean,
                  private val skip: Long) : Runnable {

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
        Logger.d("handleActionReceive $uri isDirectory = $isDirectory")
        try {
            if (MainActivity.isHost) {
                if (!clientHelper.connected) throw  Exception("client disconnected")
                clientHelper.write(FileRequest(FileRequest.UPLOAD_REQUEST_CONFIRM, requestInfo.rid))
                onFileRequestReceiverListener.onRequestAccepted(requestInfo)
            }
            val socket = getSocket()
            val init = System.currentTimeMillis()
            val file = uri.toFile()
            if (isDirectory) {
                val zipUtils = ZipUtils()
                CoroutineScope(IO).launch {
                    while (!socket.isClosed) {
                        onFileRequestReceiverListener.onProgressChange(requestInfo, zipUtils.transferred)
                        delay(PROGRESS_UPDATE_TIME)
                    }
                    onFileRequestReceiverListener.onProgressChange(requestInfo, zipUtils.transferred)
                }
                zipUtils.openInputOutStream(socket.getInputStream(), file)
            } else {
                val `in` = socket.getInputStream()
                val out: OutputStream = FileOutputStream(file, true)
                val bytes = ByteArray(BUFFER_SIZE)
                var count: Int
                var transferredByte: Long = 0
                CoroutineScope(IO).launch {
                    while (!socket.isClosed) {
                        onFileRequestReceiverListener.onProgressChange(requestInfo, transferredByte)
                        delay(PROGRESS_UPDATE_TIME)
                    }
                    onFileRequestReceiverListener.onProgressChange(requestInfo, transferredByte)
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
            onFileRequestReceiverListener.onRequestSuccess(requestInfo, false)
        } catch (e: Exception) {
            onFileRequestReceiverListener.onRequestFailed(requestInfo)
            e.printStackTrace()
        }
    }

    private fun handleActionSend() {
        Logger.d("handleActionSend $uri  isDirectory = $isDirectory")
        try {
            if (MainActivity.isHost) {
                if (!clientHelper.connected) throw  Exception("client disconnected")
                clientHelper.write(FileRequest(FileRequest.DOWNLOAD_REQUEST_CONFIRM, requestInfo.rid))
                onFileRequestReceiverListener.onRequestAccepted(requestInfo)
            }
            val socket = getSocket()
            val init = System.currentTimeMillis()
            if (isDirectory) {
                val zipUtils = ZipUtils()
                CoroutineScope(IO).launch {
                    while (!socket.isClosed) {
                        onFileRequestReceiverListener.onProgressChange(requestInfo, zipUtils.transferred)
                        delay(PROGRESS_UPDATE_TIME)
                    }
                    onFileRequestReceiverListener.onProgressChange(requestInfo, zipUtils.transferred)
                }
                zipUtils.openZipOutStream(socket.getOutputStream(), uri.toFile())
            } else {
                val inputStream: InputStream = App.context.contentResolver.openInputStream(uri)!!
                inputStream.skip(skip)
                val outputStream = socket.getOutputStream()
                val bytes = ByteArray(BUFFER_SIZE)
                var count: Int = 0
                var transferredByte: Long = 0
                CoroutineScope(IO).launch {
                    while (!socket.isClosed) {
                        requestInfo.transferred = transferredByte
                        onFileRequestReceiverListener.onProgressChange(requestInfo, transferredByte)
                        delay(PROGRESS_UPDATE_TIME)
                    }
                    requestInfo.transferred = transferredByte
                    onFileRequestReceiverListener.onProgressChange(requestInfo, transferredByte)
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
            onFileRequestReceiverListener.onRequestSuccess(requestInfo, true)
        } catch (e: Exception) {
            onFileRequestReceiverListener.onRequestFailed(requestInfo)
            e.printStackTrace()
        }
    }


    companion object {
        @JvmField
        var HOST_ADDRESS: String? = null
        private const val MAX_WAIT_TIME = 1500
        private const val PORT = 7511
        const val BUFFER_SIZE = 4096
        private const val PROGRESS_UPDATE_TIME: Long = 1000

        fun startActionSend(onFileRequestReceiverListener: OnFileRequestReceiverListener, requestInfo: RequestInfo, uri: Uri, clientHelper: ClientHelper, isDirectory: Boolean) {
            synchronized(App.taskExecutor) {
                App.taskExecutor.submit(FileService(
                        true, onFileRequestReceiverListener, requestInfo, uri, clientHelper, isDirectory, 0
                ))
            }
        }

        fun startActionReceive(onFileRequestReceiverListener: OnFileRequestReceiverListener, uri: Uri, requestInfo: RequestInfo, clientHelper: ClientHelper, isDirectory: Boolean) {
            synchronized(App.taskExecutor) {
                App.taskExecutor.submit(FileService(
                        false, onFileRequestReceiverListener, requestInfo, uri, clientHelper, isDirectory, 0
                ))
            }
        }

    }

}