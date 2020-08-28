package com.vkpapps.sendkr.connection

import android.net.Uri
import androidx.core.net.toFile
import com.vkpapps.sendkr.App
import com.vkpapps.sendkr.interfaces.OnFileRequestReceiverListener
import com.vkpapps.sendkr.model.FileRequest
import com.vkpapps.sendkr.model.RequestInfo
import com.vkpapps.sendkr.model.constant.FileType
import com.vkpapps.sendkr.model.constant.StatusType
import com.vkpapps.sendkr.ui.activity.ConnectionActivity
import com.vkpapps.sendkr.ui.activity.MainActivity
import com.vkpapps.sendkr.utils.DownloadPathResolver
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

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
            }
        } else {
            socket = Socket()
            ConnectionActivity.network?.bindSocket(socket)
            socket.connect(InetSocketAddress(HOST_ADDRESS, PORT))
        }
        return socket
    }

    override fun run() {
        if (requestInfo.status != StatusType.STATUS_PENDING || requestInfo.status == StatusType.STATUS_COMPLETED) return
        //change status to ongoing
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
        try {
            if (MainActivity.isHost) {
                if (!clientHelper.connected) throw  Exception("client disconnected")
                clientHelper.send(FileRequest(true, requestInfo.rid))
            }
            val socket = getSocket()
            requestInfo.status = StatusType.STATUS_ONGOING
            onFileRequestReceiverListener.onRequestAccepted(requestInfo)
            if (requestInfo.uri == null) {
                requestInfo.uri = Uri.fromFile(File(DownloadPathResolver.getSource(requestInfo))).toString()
            }
            val file = Uri.parse(requestInfo.uri).toFile()
            if (requestInfo.fileType == FileType.FILE_TYPE_FOLDER) {
                val zipUtils = ZipUtils(requestInfo)
                zipUtils.openInputStream(socket.getInputStream(), file)
            } else {
                val inputStream = socket.getInputStream()
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
            if (requestInfo.transferred == requestInfo.size) {
                //change status to complete if total bytes are transferred
                requestInfo.status = StatusType.STATUS_COMPLETED
            }
            onFileRequestReceiverListener.onRequestSuccess(requestInfo, false)
        } catch (e: Exception) {
            requestInfo.status = StatusType.STATUS_FAILED
            onFileRequestReceiverListener.onRequestFailed(requestInfo)
            e.printStackTrace()
        }
    }

    private fun handleActionSend() {
        val uri = Uri.parse(requestInfo.uri)
        try {
            if (MainActivity.isHost) {
                if (!clientHelper.connected) throw  Exception("client disconnected")
                clientHelper.send(FileRequest(false, requestInfo.rid))
            }
            val socket = getSocket()
            requestInfo.status = StatusType.STATUS_ONGOING
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
            socket.close()
            if (requestInfo.transferred == requestInfo.size) {
                //change status to complete if total bytes are transferred
                requestInfo.status = StatusType.STATUS_COMPLETED
            }
            onFileRequestReceiverListener.onRequestSuccess(requestInfo, true)
        } catch (e: Exception) {
            requestInfo.status = StatusType.STATUS_FAILED
            onFileRequestReceiverListener.onRequestFailed(requestInfo)
            e.printStackTrace()
        }
    }


    companion object {
        @JvmField
        var HOST_ADDRESS: String? = null
        private const val MAX_WAIT_TIME = 7000
        private const val PORT = 7511
        private const val BUFFER_SIZE = 10000
        val BUFFER = ByteArray(BUFFER_SIZE)

        @JvmStatic
        val taskExecutor: ExecutorService = Executors.newSingleThreadExecutor()

        fun startActionSend(onFileRequestReceiverListener: OnFileRequestReceiverListener, requestInfo: RequestInfo, clientHelper: ClientHelper) {
            synchronized(taskExecutor) {
                taskExecutor.submit(FileService(
                        true, onFileRequestReceiverListener, requestInfo, clientHelper
                ))
            }
        }

        fun startActionReceive(onFileRequestReceiverListener: OnFileRequestReceiverListener, requestInfo: RequestInfo, clientHelper: ClientHelper) {
            synchronized(taskExecutor) {
                taskExecutor.submit(FileService(
                        false, onFileRequestReceiverListener, requestInfo, clientHelper
                ))
            }
        }

    }

}