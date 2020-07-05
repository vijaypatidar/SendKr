package com.vkpapps.thunder.connection

import com.vkpapps.thunder.interfaces.OnClientConnectionStateListener
import com.vkpapps.thunder.interfaces.OnFileRequestListener
import com.vkpapps.thunder.interfaces.OnObjectReceiveListener
import com.vkpapps.thunder.model.User
import java.io.IOException
import java.io.Serializable
import java.net.ServerSocket

/**
 * @author VIJAY PATIDAR
 */
class ServerHelper(private val onFileRequestListener: OnFileRequestListener,
                   private val user: User,
                   private val onClientConnectionStateListener: OnClientConnectionStateListener?) : Thread(), OnClientConnectionStateListener, OnObjectReceiveListener {

    val clientHelpers: ArrayList<ClientHelper> = ArrayList()
    private var live = true
    override fun run() {
        try {
            val serverSocket = ServerSocket(1203)
            while (live) {
                try {
                    val socket = serverSocket.accept()
                    val commandHelper = ClientHelper(socket, onFileRequestListener, user, this)
                    commandHelper.setOnObjectReceiveListener(this)
                    commandHelper.start()
                    try {
                        sleep(2000)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
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

    fun sendCommandToOnly(command: Any, clientId: String) {
        for (c in clientHelpers) {
            if (c.user.userId == clientId) {
                c.write(command)
            }
        }
    }

    override fun onClientConnected(clientHelper: ClientHelper) {
        var i = 0
        while (i < clientHelpers.size) {
            if (clientHelpers[i].user.userId == clientHelper.user.userId) {
                clientHelpers.removeAt(i--)
            }
            i++
        }
        clientHelpers.add(clientHelper)
        onClientConnectionStateListener?.onClientConnected(clientHelper)
    }

    override fun onClientDisconnected(clientHelper: ClientHelper) {
        clientHelpers.remove(clientHelper)
        onClientConnectionStateListener?.onClientDisconnected(clientHelper)
    }

    override fun onObjectReceive(obj: Serializable) {
        broadcast(obj)
    }

    fun shutDown() {
        live = false
        for (c in clientHelpers) {
            c.shutDown()
        }
    }

}