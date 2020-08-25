package com.vkpapps.sendkr.connection

import com.vkpapps.sendkr.analitics.Logger
import com.vkpapps.sendkr.interfaces.OnClientConnectionStateListener
import com.vkpapps.sendkr.interfaces.OnFileRequestListener
import com.vkpapps.sendkr.model.FileRequest
import com.vkpapps.sendkr.model.FileStatusRequest
import com.vkpapps.sendkr.model.RequestInfo
import com.vkpapps.sendkr.model.User
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
        @JvmStatic
        private val signalExecutors = Executors.newSingleThreadExecutor()
    }

    private var outputStream: ObjectOutputStream? = null
    var connected: Boolean = true

    override fun run() {
        try {
            outputStream = ObjectOutputStream(socket.getOutputStream())
            // send identity to connected device
            outputStream!!.writeObject(user)
            outputStream!!.flush()
            val inputStream = ObjectInputStream(socket.getInputStream())
            var obj = inputStream.readObject()
            if (obj is User) {
                user = obj
                //notify user added
                onClientConnectionStateListener?.onClientConnected(this)
                var retry = 0
                while (!socket.isClosed) {
                    try {
                        obj = inputStream.readObject()
                        when (obj) {
                            is FileRequest -> {
                                handleFileControl(obj)
                            }
                            is RequestInfo -> {
                                onFileRequestListener.onNewRequestInfo(obj, this)
                            }
                            is FileStatusRequest -> {
                                onFileRequestListener.onFileStatusChange(obj, this)
                            }
                            is User -> {
                                // update user information
                                if (obj.userId == user.userId) {
                                    user.name = obj.name
                                    user.profileByteArray = obj.profileByteArray
                                    onClientConnectionStateListener?.onClientInformationChanged(this@ClientHelper)
                                }
                            }
                            else -> {
                                Logger.e("invalid object received $obj")
                                }
                            }
                    } catch (e: Exception) {
                        retry++
                        if (retry == 10) break
                        e.printStackTrace()
                    }
                }
            } else {
                socket.close()
                return
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        // notify client leaved or disconnected
        connected = false
        onClientConnectionStateListener?.onClientDisconnected(this)
    }

    fun write(command: Any) {
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
            if (request.send) {
                onFileRequestListener.onUploadRequest(request.rid)
            } else {
                onFileRequestListener.onDownloadRequest(request.rid)
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