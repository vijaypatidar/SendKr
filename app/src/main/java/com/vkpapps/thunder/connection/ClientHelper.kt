package com.vkpapps.thunder.connection

import android.os.Bundle
import com.vkpapps.thunder.analitics.Logger
import com.vkpapps.thunder.interfaces.OnClientConnectionStateListener
import com.vkpapps.thunder.interfaces.OnFileRequestListener
import com.vkpapps.thunder.interfaces.OnObjectReceiveListener
import com.vkpapps.thunder.model.FileRequest
import com.vkpapps.thunder.model.User
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket

/***
 * @author VIJAY PATIDAR
 */
class ClientHelper(private val socket: Socket, private val onFileRequestListener: OnFileRequestListener, var user: User, private val onClientConnectionStateListener: OnClientConnectionStateListener?) : Thread() {
    private var outputStream: ObjectOutputStream? = null
    private var onObjectReceiveListener: OnObjectReceiveListener? = null
    override fun run() {
        val bundle = Bundle()
        bundle.putString("ID", user.userId)
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
                        if (obj is FileRequest) {
                            handleFileControl(obj)
                        } else if (obj is User) {
                            // update user information
                            if (obj.userId == user.userId) {
                                user.name = obj.name
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
            } else {
                return
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        // notify client leaved or disconnected
        onClientConnectionStateListener?.onClientDisconnected(this)
    }
 
    fun write(command: Any) {
        Thread(Runnable {
            try {
                outputStream?.writeObject(command)
                outputStream?.flush()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }).start()
    }

    private fun handleFileControl(request: FileRequest) {
        try {
            when (request.action) {
                FileRequest.DOWNLOAD_REQUEST -> onFileRequestListener.onDownloadRequest(request.data, request.id, request.type)
                FileRequest.UPLOAD_REQUEST -> onFileRequestListener.onUploadRequest(request.data, request.id, request.type)
                FileRequest.DOWNLOAD_REQUEST_CONFIRM -> onFileRequestListener.onDownloadRequestAccepted(request.data, request.id, request.type)
                FileRequest.UPLOAD_REQUEST_CONFIRM -> onFileRequestListener.onUploadRequestAccepted(request.data, request.id, request.type)
                else -> Logger.d("handleFileControl: invalid req " + request.action)
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
        } catch (e: ArrayIndexOutOfBoundsException) {
            e.printStackTrace()
        }
    }

    fun setOnObjectReceiveListener(onObjectReceiveListener: OnObjectReceiveListener) {
        this.onObjectReceiveListener = onObjectReceiveListener
    }

    fun shutDown() {
        try {
            socket.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}