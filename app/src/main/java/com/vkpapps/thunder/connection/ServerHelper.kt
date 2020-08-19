package com.vkpapps.thunder.connection

import com.vkpapps.thunder.interfaces.OnClientConnectionStateListener
import com.vkpapps.thunder.interfaces.OnFileRequestListener
import com.vkpapps.thunder.model.User
import java.io.IOException
import java.net.ServerSocket

/**
 * @author VIJAY PATIDAR
 */
class ServerHelper(private val onFileRequestListener: OnFileRequestListener,
                   private val user: User,
                   private val onClientConnectionStateListener: OnClientConnectionStateListener?) : Thread(), OnClientConnectionStateListener {

    companion object {
        const val PORT = 3110
    }

    val clientHelpers: ArrayList<ClientHelper> = ArrayList()
    private var live = true
    override fun run() {
        try {
            val serverSocket = ServerSocket(PORT, 0)
            while (live) {
                try {
                    val socket = serverSocket.accept()
                    val commandHelper = ClientHelper(socket, onFileRequestListener, user, this)
                    commandHelper.start()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun broadcast(command: Any) {
        for (c in clientHelpers) {
            c.write(command)
        }
    }

    override fun onClientConnected(clientHelper: ClientHelper) {
        clientHelpers.add(clientHelper)
        onClientConnectionStateListener?.onClientConnected(clientHelper)
    }

    override fun onClientDisconnected(clientHelper: ClientHelper) {
        clientHelpers.remove(clientHelper)
        onClientConnectionStateListener?.onClientDisconnected(clientHelper)
    }

    override fun onClientInformationChanged(clientHelper: ClientHelper) {
        onClientConnectionStateListener?.onClientInformationChanged(clientHelper)
    }

    fun shutDown() {
        live = false
        for (c in clientHelpers) {
            c.shutDown()
        }
    }

}