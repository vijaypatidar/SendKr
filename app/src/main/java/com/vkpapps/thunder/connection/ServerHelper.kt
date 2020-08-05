package com.vkpapps.thunder.connection

import com.vkpapps.thunder.analitics.Logger
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

    val clientHelpers: ArrayList<ClientHelper> = ArrayList()
    private var live = true
    override fun run() {
        try {
            val serverSocket = ServerSocket(1203)
            while (live) {
                try {
                    val socket = serverSocket.accept()
                    val commandHelper = ClientHelper(socket, onFileRequestListener, user, this)
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

    fun broadcast(command: String) {
        Logger.d("broadcast $command")
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

    fun shutDown() {
        live = false
        for (c in clientHelpers) {
            c.shutDown()
        }
    }

}