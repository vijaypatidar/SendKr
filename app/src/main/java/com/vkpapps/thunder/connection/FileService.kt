package com.vkpapps.thunder.connection

import com.vkpapps.thunder.App
import com.vkpapps.thunder.analitics.Logger
import com.vkpapps.thunder.interfaces.OnFileRequestReceiverListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.*
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

/***
 * @author VIJAY PATIDAR
 */
class FileService(private val send: Boolean, private val onFileRequestReceiverListener: OnFileRequestReceiverListener, private val rid: String, private val source: String, private val clientId: String, private val isHost: Boolean) : Runnable {


    @Throws(IOException::class)
    private fun getSocket(isHost: Boolean): Socket {
        lateinit var socket: Socket
        if (isHost) {
            ServerSocket(PORT).use { serverSocket ->
                serverSocket.soTimeout = MAX_WAIT_TIME
                socket = serverSocket.accept()
            }
        } else {
            socket = Socket()
            socket.connect(InetSocketAddress(HOST_ADDRESS, PORT), MAX_WAIT_TIME)
        }
        return socket
    }

    override fun run() {
        try {
            if (send) {
                handleActionSend(rid, source, clientId, isHost)
            } else {
                handleActionReceive(rid, source, clientId, isHost)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleActionReceive(rid: String, source: String, clientId: String, isHost: Boolean) {

        try {
            if (isHost) onFileRequestReceiverListener.onRequestAccepted(rid, clientId, false)
            val socket = getSocket(isHost)
            val init = System.currentTimeMillis()
            val file = File(source)
            if (file.isDirectory) {
                val zipUtils = ZipUtils()
                CoroutineScope(Default).launch {
                    Logger.d("receiving file ----------------")
                    while (!socket.isClosed) {
                        onFileRequestReceiverListener.onProgressChange(rid, zipUtils.transferred)
                        Logger.d("inside while for receiving ${zipUtils.transferred}")
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
                        Logger.d("inside while for receiving $transferredByte")
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
            Logger.d("timeTaken =  $timeTaken")
            onFileRequestReceiverListener.onRequestSuccess(rid, timeTaken)
        } catch (e: IOException) {
            onFileRequestReceiverListener.onRequestFailed(rid)
            e.printStackTrace()
        }
    }

    private fun handleActionSend(rid: String, source: String, clientId: String, isHost: Boolean) {
        try {
            if (isHost) onFileRequestReceiverListener.onRequestAccepted(rid, clientId, true)
            val socket = getSocket(isHost)
            val file = File(source)
            val init = System.currentTimeMillis()
            if (file.isDirectory) {
                val zipUtils = ZipUtils()
                CoroutineScope(Default).launch {
                    while (!socket.isClosed) {
                        onFileRequestReceiverListener.onProgressChange(rid, zipUtils.transferred)
                        delay(PROGRESS_UPDATE_TIME)
                    }
                    onFileRequestReceiverListener.onProgressChange(rid, zipUtils.transferred)
                }
                zipUtils.openZipOutStream(socket.getOutputStream(), file)
            } else {
                val inputStream: InputStream = FileInputStream(file)
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
            val timeTaken = (System.currentTimeMillis() - init) / 1000
            Logger.d("timeTaken =  $timeTaken")
            onFileRequestReceiverListener.onRequestSuccess(rid, timeTaken)
        } catch (e: IOException) {
            onFileRequestReceiverListener.onRequestFailed(rid)
            e.printStackTrace()
        }
    }


    companion object {
        const val ACTION_PROGRESS_CHANGE = "com.vkpapps.thunder.action.ACTION_PROGRESS_CHANGE"
        const val STATUS_SUCCESS = "com.vkpapps.thunder.action.SUCCESS"
        const val STATUS_FAILED = "com.vkpapps.thunder.action.FAILED"
        const val REQUEST_ACCEPTED = "com.vkpapps.thunder.action.ACCEPTED"

        @JvmField
        var HOST_ADDRESS: String? = null
        private const val MAX_WAIT_TIME = 1500
        private const val PORT = 7511

        private const val PROGRESS_UPDATE_TIME: Long = 2000

        fun startActionSend(onFileRequestReceiverListener: OnFileRequestReceiverListener, rid: String, source: String, clientId: String, isHost: Boolean) {
            synchronized(App.executor) {
                App.executor.submit(FileService(
                        true, onFileRequestReceiverListener, rid, source, clientId, isHost
                ))
            }
        }

        fun startActionReceive(onFileRequestReceiverListener: OnFileRequestReceiverListener, name: String, source: String, rid: String, clientId: String, isHost: Boolean) {
            synchronized(App.executor) {
                App.executor.submit(FileService(
                        false, onFileRequestReceiverListener, rid, source, clientId, isHost
                ))
            }
        }

    }

}