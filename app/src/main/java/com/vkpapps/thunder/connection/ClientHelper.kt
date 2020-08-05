package com.vkpapps.thunder.connection

import com.vkpapps.thunder.analitics.Logger
import com.vkpapps.thunder.interfaces.OnClientConnectionStateListener
import com.vkpapps.thunder.interfaces.OnFileRequestListener
import com.vkpapps.thunder.model.FileRequest
import com.vkpapps.thunder.model.SerializedMessage
import com.vkpapps.thunder.model.User
import com.vkpapps.thunder.ui.activity.MainActivity.Companion.GSON
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket
import java.util.concurrent.Executors

/***
 * @author VIJAY PATIDAR
 */
class ClientHelper(private val socket: Socket, private val onFileRequestListener: OnFileRequestListener, var user: User, private val onClientConnectionStateListener: OnClientConnectionStateListener?) : Thread() {
    companion object {
        private val signalExecutors = Executors.newSingleThreadExecutor()

    }

    private var outputStream: ObjectOutputStream? = null
    var connected: Boolean = true

    override fun run() {
        try {
            outputStream = ObjectOutputStream(socket.getOutputStream())
            // send identity to connected device
            outputStream!!.writeObject(GSON.toJson(user))
            outputStream!!.flush()
            val inputStream = ObjectInputStream(socket.getInputStream())
            var obj = inputStream.readObject()
            Logger.d("connection handshake ${obj as String}")
            user = GSON.fromJson(obj, User::class.java)
            //notify user added
            onClientConnectionStateListener?.onClientConnected(this)
            var retry = 0
            while (!socket.isClosed) {
                try {
                    obj = inputStream.readObject()
                    if (obj is String) {
                        Logger.d("object received $obj")
                        val serializedMessage = GSON.fromJson(obj, SerializedMessage::class.java)

                        serializedMessage.fileRequest?.run {
                            handleFileControl(this)
                        }
                        serializedMessage.requestInfo?.run {
                            // update user information
                            this.cid = user.userId
                            onFileRequestListener.onNewRequestInfo(this)
                        }
                        serializedMessage.user?.run {
                            // update user information
                            if (this.userId == user.userId) {
                                user.copyFrom(this)
                            }
                        }
                    } else {
                        Logger.e("invalid object received $obj")
                    }
                } catch (e: Exception) {
                    retry++
                    if (retry == 10) break
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        // notify client leaved or disconnected
        connected = false
        onClientConnectionStateListener?.onClientDisconnected(this)
    }

    fun write(command: String) {
        signalExecutors.submit {
            outputStream?.let {
                synchronized(it) {
                    try {
                        outputStream?.writeObject(command)
                        outputStream?.flush()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun handleFileControl(request: FileRequest) {
        try {
            when (request.action) {
                FileRequest.DOWNLOAD_REQUEST_CONFIRM -> onFileRequestListener.onDownloadRequest(request.rid)
                FileRequest.UPLOAD_REQUEST_CONFIRM -> onFileRequestListener.onUploadRequest(request.rid)
                else -> Logger.d("handleFileControl: invalid req " + request.action)
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
        } catch (e: ArrayIndexOutOfBoundsException) {
            e.printStackTrace()
        }
    }

    fun shutDown() {
        try {
            socket.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}