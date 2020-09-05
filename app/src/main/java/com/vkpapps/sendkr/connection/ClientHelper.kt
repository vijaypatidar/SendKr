package com.vkpapps.sendkr.connection

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.vkpapps.sendkr.analitics.Logger
import com.vkpapps.sendkr.interfaces.OnClientConnectionStateListener
import com.vkpapps.sendkr.interfaces.OnFileRequestListener
import com.vkpapps.sendkr.model.*
import com.vkpapps.sendkr.ui.activity.MainActivity
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

        @JvmStatic
        val gson: Gson = GsonBuilder().apply {
            serializeNulls()
            excludeFieldsWithoutExposeAnnotation()
        }.create()
    }

    val hostAddress: String = socket.inetAddress.hostAddress

    private var outputStream: ObjectOutputStream? = null
    var connected: Boolean = true

    override fun run() {
        try {
            outputStream = ObjectOutputStream(socket.getOutputStream())
            // send identity to connected device
            send(user)
            val inputStream = ObjectInputStream(socket.getInputStream())
            val initCommand = gson.fromJson(inputStream.readObject() as String, Signal::class.java)

            if (initCommand.type == Signal.INPUT_TYPE_USER) {
                user = initCommand.user!!
                //notify user added
                onClientConnectionStateListener?.onClientConnected(this)
                var retry = 0
                while (!socket.isClosed) {
                    try {
                        val signal = gson.fromJson(inputStream.readObject() as String, Signal::class.java)
                        when (signal.type) {
                            Signal.INPUT_TYPE_FILE_REQUEST -> {
                                handleFileControl(signal.fileRequest!!)
                            }
                            Signal.INPUT_TYPE_REQUEST_INFO -> {
                                onFileRequestListener.onNewRequestInfo(signal.requestInfo!!.apply {
                                    if (MainActivity.isHost) {
                                        cid = user.userId//init cid used by host to identify client
                                    }
                                }, this)
                            }
                            Signal.INPUT_TYPE_FILE_STATUS_REQUEST -> {
                                onFileRequestListener.onFileStatusChange(signal.fileStatusRequest!!, this)
                            }
                            Signal.INPUT_TYPE_USER -> {
                                // update user information
                                if (signal.user!!.userId == user.userId) {
                                    user.name = signal.user!!.name
                                    user.profileByteArray = signal.user!!.profileByteArray
                                    onClientConnectionStateListener?.onClientInformationChanged(this@ClientHelper)
                                }
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

    private fun send(signal: String) {
        synchronized(signalExecutors) {
            Logger.d("[ClientHelper][send] signal = $signal")
            signalExecutors.submit {
                outputStream?.let {
                    synchronized(it) {
                        try {
                            outputStream?.writeObject(signal)
                            outputStream?.flush()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    fun send(requestInfo: RequestInfo) {
        val toJson = gson.toJson(Signal(requestInfo))
        send(toJson)
    }

    fun send(fileStatusRequest: FileStatusRequest) {
        val toJson = gson.toJson(Signal(fileStatusRequest))
        send(toJson)
    }

    fun send(user: User) {
        val toJson = gson.toJson(Signal(user))
        send(toJson)
    }

    fun send(fileRequest: FileRequest) {
        val toJson = gson.toJson(Signal(fileRequest))
        send(toJson)
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