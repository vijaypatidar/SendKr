package com.vkpapps.thunder.connection

import android.net.Uri
import androidx.core.net.toFile
import com.vkpapps.thunder.App
import com.vkpapps.thunder.analitics.Logger
import com.vkpapps.thunder.interfaces.OnFileRequestReceiverListener
import com.vkpapps.thunder.model.FileRequest
import com.vkpapps.thunder.model.RequestInfo
import com.vkpapps.thunder.model.constant.FileType
import com.vkpapps.thunder.model.constant.StatusType
import com.vkpapps.thunder.ui.activity.ConnectionActivity
import com.vkpapps.thunder.ui.activity.MainActivity
import java.io.*
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

/***
 * @author VIJAY PATIDAR
 */
class FileService(private val send: Boolean,
                  private val onFileRequestReceiverListener: OnFileRequestReceiverListener,
                  private val requestInfo: RequestInfo,
                  private val clientHelper: ClientHelper) : Runnable {

    @Throws(IOException::class)
    private fun getSocket(): Socket {
        lateinit var socket: Socket
        if (MainActivity.isHost) {
            ServerSocket(PORT).use { serverSocket ->
                serverSocket.soTimeout = MAX_WAIT_TIME
                socket = serverSocket.accept()
                Logger.d(socket.inetAddress.hostAddress)
            }
        } else {
            socket = Socket()
            ConnectionActivity.network?.bindSocket(socket)
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
        Logger.d("handleActionReceive ${requestInfo.uri} isDirectory = ${requestInfo.fileType == FileType.FILE_TYPE_FOLDER}")
        try {
            if (MainActivity.isHost) {
                if (!clientHelper.connected) throw  Exception("client disconnected")
                clientHelper.write(FileRequest(true, requestInfo.rid))
            }
            val socket = getSocket()
            onFileRequestReceiverListener.onRequestAccepted(requestInfo)
            requestInfo.uri = Uri.fromFile(File(App.downloadPathResolver.getSource(requestInfo))).toString()
            val file = Uri.parse(requestInfo.uri).toFile()
            if (requestInfo.fileType == FileType.FILE_TYPE_FOLDER) {
                val zipUtils = ZipUtils(requestInfo)
                zipUtils.openInputStream(socket.getInputStream(), file)
            } else {
                val inputStream = BufferedInputStream(socket.getInputStream())
                val outputStream: OutputStream = FileOutputStream(file, true)
                var read: Int
                while (inputStream.read(BUFFER).apply { read = this } > 0) {
                    outputStream.write(BUFFER, 0, read)
                    requestInfo.transferred += read
                }
                inputStream.close()
                outputStream.flush()
                outputStream.close()
            }
            socket.close()
            onFileRequestReceiverListener.onRequestSuccess(requestInfo, false)
        } catch (e: Exception) {
            onFileRequestReceiverListener.onRequestFailed(requestInfo)
            e.printStackTrace()
        }
    }

    private fun handleActionSend() {
        val uri = Uri.parse(requestInfo.uri)
        try {
            if (MainActivity.isHost) {
                if (!clientHelper.connected) throw  Exception("client disconnected")
                clientHelper.write(FileRequest(false, requestInfo.rid))
                onFileRequestReceiverListener.onRequestAccepted(requestInfo)
            }
            val socket = getSocket()
            if (requestInfo.fileType == FileType.FILE_TYPE_FOLDER) {
                val zipUtils = ZipUtils(requestInfo)
                zipUtils.openZipOutStream(socket.getOutputStream(), uri.toFile())
            } else {
                val inputStream = App.context.contentResolver.openInputStream(uri)!!
                //skip previous sent bytes
                inputStream.skip(requestInfo.transferred)
                val outputStream = socket.getOutputStream()
                var read: Int
                while (inputStream.read(BUFFER).apply { read = this } > 0 && requestInfo.status == StatusType.STATUS_ONGOING) {
                    outputStream.write(BUFFER, 0, read)
                    requestInfo.transferred += read
                }
                inputStream.close()
                outputStream.flush()
                outputStream.close()
            }
            onFileRequestReceiverListener.onRequestSuccess(requestInfo, true)
            socket.close()
        } catch (e: Exception) {
            onFileRequestReceiverListener.onRequestFailed(requestInfo)
            e.printStackTrace()
        }
    }


    companion object {
        @JvmField
        var HOST_ADDRESS: String? = null
        private const val MAX_WAIT_TIME = 3000
        private const val PORT = 7511
        private const val BUFFER_SIZE = 3500
        val BUFFER = ByteArray(BUFFER_SIZE)

        fun startActionSend(onFileRequestReceiverListener: OnFileRequestReceiverListener, requestInfo: RequestInfo, clientHelper: ClientHelper) {
            synchronized(App.taskExecutor) {
                App.taskExecutor.submit(FileService(
                        true, onFileRequestReceiverListener, requestInfo, clientHelper
                ))
            }
        }

        fun startActionReceive(onFileRequestReceiverListener: OnFileRequestReceiverListener, requestInfo: RequestInfo, clientHelper: ClientHelper) {
            synchronized(App.taskExecutor) {
                App.taskExecutor.submit(FileService(
                        false, onFileRequestReceiverListener, requestInfo, clientHelper
                ))
            }
        }

    }

}