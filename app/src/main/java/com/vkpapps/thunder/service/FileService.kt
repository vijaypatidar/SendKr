package com.vkpapps.thunder.service

import android.app.IntentService
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.vkpapps.thunder.App
import com.vkpapps.thunder.analitics.Logger
import java.io.*
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

/***
 * @author VIJAY PATIDAR
 */
class FileService : IntentService("FileService") {

    private val localBroadcastManager: LocalBroadcastManager by lazy {
        LocalBroadcastManager.getInstance(this)
    }

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

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            try {
                val action = intent.action
                val rid = intent.getStringExtra(PARAM_RID)
                val clientId = intent.getStringExtra(PARAM_CLIENT_ID)
                val isHost = intent.getBooleanExtra(PARAM_IS_HOST, false)
                val source = intent.getStringExtra(PARAM_SOURCE)
                if (ACTION_SEND == action) {
                    handleActionSend(rid!!, source!!, clientId!!, isHost)
                } else if (ACTION_RECEIVE == action) {
                    val name = intent.getStringExtra(PARAM_NAME)
                    handleActionReceive(rid!!, source!!, clientId!!, isHost)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun handleActionReceive(rid: String, source: String, clientId: String, isHost: Boolean) {
        try {
            if (isHost) onAccepted(rid, clientId, false)
            val socket = getSocket(isHost)
            val init = System.currentTimeMillis()
            val `in` = socket.getInputStream()
            val file = File(source)
            val out: OutputStream = FileOutputStream(file)
            val bytes = ByteArray(3000)
            var count: Int
            while (`in`.read(bytes).also { count = it } > 0) {
                out.write(bytes, 0, count)
            }
            `in`.close()
            out.flush()
            out.close()
            socket.close()
            val timeTaken = System.currentTimeMillis() - init
            Logger.d("========================================= $timeTaken")
            onSuccess(rid, timeTaken)
        } catch (e: IOException) {
            onFailed(rid)
            e.printStackTrace()
        }
    }

    private fun handleActionSend(rid: String, source: String, clientId: String, isHost: Boolean) {
        try {
            if (isHost) onAccepted(rid, clientId, true)
            val file = File(source)
            val socket = getSocket(isHost)
            val init = System.currentTimeMillis()
            val inputStream: InputStream = FileInputStream(file)
            val outputStream = socket.getOutputStream()
            val bytes = ByteArray(3000)
            var count: Int
            while (inputStream.read(bytes).also { count = it } > 0) {
                outputStream.write(bytes, 0, count)
            }
            outputStream.flush()
            outputStream.close()
            inputStream.close()
            socket.close()
            val timeTaken = (System.currentTimeMillis() - init) / 1000
            Logger.d("========================================= $timeTaken")
            onSuccess(rid, timeTaken)
        } catch (e: IOException) {
            onFailed(rid)
            e.printStackTrace()
        }
    }

    private fun onSuccess(rid: String, timeTaken: Long) {
        val intent = Intent(STATUS_SUCCESS)
        intent.putExtra(PARAM_RID, rid)
        intent.putExtra(PARAM_TIME_TAKEN, timeTaken)
        localBroadcastManager.sendBroadcast(intent)
    }

    private fun onFailed(rid: String) {
        val intent = Intent(STATUS_FAILED)
        intent.putExtra(PARAM_RID, rid)
        localBroadcastManager.sendBroadcast(intent)
    }

    private fun onAccepted(rid: String, clientID: String, send: Boolean) {
        val intent = Intent(REQUEST_ACCEPTED)
        intent.putExtra(PARAM_RID, rid)
        intent.putExtra(PARAM_CLIENT_ID, clientID)
        intent.putExtra(ACTION_SEND, send)
        localBroadcastManager.sendBroadcast(intent)
    }

    companion object {
        const val ACTION_SEND = "com.vkpapps.thunder.action.SEND"
        const val ACTION_RECEIVE = "com.vkpapps.thunder.action.RECEIVE"
        const val STATUS_SUCCESS = "com.vkpapps.thunder.action.SUCCESS"
        const val STATUS_FAILED = "com.vkpapps.thunder.action.FAILED"
        const val REQUEST_ACCEPTED = "com.vkpapps.thunder.action.ACCEPTED"
        const val PARAM_NAME = "com.vkpapps.thunder.extra.NAME"
        const val PARAM_CLIENT_ID = "com.vkpapps.thunder.extra.CLIENT_ID"
        const val PARAM_RID = "com.vkpapps.thunder.extra.RID"
        const val PARAM_SOURCE = "com.vkpapps.thunder.extra.SOURCE"
        const val PARAM_IS_HOST = "com.vkpapps.thunder.extra.IS_HOST"
        const val PARAM_TIME_TAKEN = "com.vkpapps.thunder.action.TIME_TAKEN"

        @JvmField
        var HOST_ADDRESS: String? = null
        private const val MAX_WAIT_TIME = 800
        private const val PORT = 7511

        fun startActionSend(rid: String, source: String, clientId: String?, isHost: Boolean) {
            val intent = Intent(App.context, FileService::class.java)
            intent.action = ACTION_SEND
            intent.putExtra(PARAM_CLIENT_ID, clientId)
            intent.putExtra(PARAM_IS_HOST, isHost)
            intent.putExtra(PARAM_RID, rid)
            intent.putExtra(PARAM_SOURCE, source)
            App.context.startService(intent)
        }

        fun startActionReceive(name: String, source: String, rid: String, clientId: String?, isHost: Boolean) {
            val intent = Intent(App.context, FileService::class.java)
            intent.action = ACTION_RECEIVE
            intent.putExtra(PARAM_RID, rid)
            intent.putExtra(PARAM_NAME, name)
            intent.putExtra(PARAM_CLIENT_ID, clientId)
            intent.putExtra(PARAM_SOURCE, source)
            intent.putExtra(PARAM_IS_HOST, isHost)
            App.context.startService(intent)
        }
    }
}