package com.vkpapps.thunder.ui.activity

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.vkpapps.thunder.App
import com.vkpapps.thunder.BuildConfig
import com.vkpapps.thunder.R
import com.vkpapps.thunder.analitics.Logger.d
import com.vkpapps.thunder.connection.ClientHelper
import com.vkpapps.thunder.connection.ServerHelper
import com.vkpapps.thunder.interfaces.*
import com.vkpapps.thunder.loader.PrepareDb
import com.vkpapps.thunder.model.*
import com.vkpapps.thunder.receivers.FileRequestReceiver
import com.vkpapps.thunder.receivers.FileRequestReceiver.OnFileRequestReceiverListener
import com.vkpapps.thunder.room.database.MyRoomDatabase
import com.vkpapps.thunder.room.liveViewModel.HistoryViewModel
import com.vkpapps.thunder.room.liveViewModel.RequestViewModel
import com.vkpapps.thunder.service.FileService
import com.vkpapps.thunder.ui.fragments.DashboardFragment
import com.vkpapps.thunder.ui.fragments.destinations.FragmentDestinationListener
import com.vkpapps.thunder.utils.DirectoryResolver
import com.vkpapps.thunder.utils.HashUtils.getRandomId
import com.vkpapps.thunder.utils.IPManager
import com.vkpapps.thunder.utils.PermissionUtils
import com.vkpapps.thunder.utils.UpdateManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.util.*

/**
 * @author VIJAY PATIDAR
 */
class MainActivity : AppCompatActivity(), OnNavigationVisibilityListener, OnUserListRequestListener,
        OnFragmentAttachStatusListener, OnFileRequestListener, OnFileRequestPrepareListener,
        OnFileRequestReceiverListener, OnClientConnectionStateListener {

    private lateinit var serverHelper: ServerHelper
    private lateinit var clientHelper: ClientHelper
    private var isHost = false
    private var user = App.user
    private lateinit var navController: NavController
    private var onUsersUpdateListener: OnUsersUpdateListener? = null
    private var database = MyRoomDatabase.getDatabase(this)
    private val requestViewModel: RequestViewModel by lazy {
        ViewModelProvider(this).get(RequestViewModel::class.java)
    }
    private val directoryResolver: DirectoryResolver by lazy {
        DirectoryResolver(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        user = App.user
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_app, R.id.navigation_dashboard, R.id.navigation_files)
                .build()
        navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
        NavigationUI.setupWithNavController(navView, navController)
        navController.addOnDestinationChangedListener(FragmentDestinationListener(this))
        choice()
        UpdateManager(true).checkForUpdate(true, this)

        // check for policy accepted or not
//        PrivacyDialog(this).isPolicyAccepted
        setupReceiver()
        if (!PermissionUtils.checkStoragePermission(this)) {
            PermissionUtils.askStoragePermission(this, 9098)
        }
    }

    private fun choice() {
        val ab = AlertDialog.Builder(this)
        val view = LayoutInflater.from(this).inflate(R.layout.choice_alert_dialog, null)
        ab.setView(view)
        ab.setCancelable(false)
        val alertDialog = ab.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
        view.findViewById<View>(R.id.btnCreateParty).setOnClickListener {
            setup(true)
            alertDialog.cancel()
        }
        view.findViewById<View>(R.id.btnJoinParty).setOnClickListener {
            setup(false)
            alertDialog.cancel()
        }
    }

    private fun setup(host: Boolean) {
        isHost = host
        if (isHost) {
            serverHelper = ServerHelper(this, user, this)
            serverHelper.start()
        } else {
            Thread(Runnable {
                val socket = Socket()
                try {
                    val ipManager = IPManager(this)
                    val address = ipManager.hostIp()
                    d("setup: connection address $address")
                    FileService.HOST_ADDRESS = address.substring(0, address.lastIndexOf(".") + 1) + "1"
                    socket.connect(InetSocketAddress(FileService.HOST_ADDRESS, 1203), 5000)
                    clientHelper = ClientHelper(socket, this, user, this)
                    clientHelper.start()
                } catch (e: IOException) {
                    runOnUiThread {
                        val ab = androidx.appcompat.app.AlertDialog.Builder(this)
                        ab.setTitle("No host found!")
                        ab.setMessage("There is no host on this wifi")
                        ab.setCancelable(false)
                        ab.setPositiveButton("retry") { _: DialogInterface?, _: Int -> setup(false) }
                        ab.setNegativeButton("Create group") { _: DialogInterface?, _: Int -> choice() }
                        ab.create().show()
                    }
                    e.printStackTrace()
                }
            }).start()
        }
    }

    override fun onNavVisibilityChange(visible: Boolean) {
        if (navView.visibility == View.VISIBLE == visible) return
        val animation = AnimationUtils.loadAnimation(this,
                if (visible) R.anim.slide_in_from_bottom else R.anim.slide_out_to_bottom
        )
        navView.animation = animation
        navView.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun onDownloadRequest(rid: String) {
        CoroutineScope(IO).launch {
            val requestInfo = database.requestDao().getRequestInfo(rid)
            d("rid = $rid source = ${requestInfo.source} name = ${requestInfo.name}")
            withContext(Main) {
                FileService.startActionReceive(requestInfo.name,
                        requestInfo.source,
                        rid,
                        requestInfo.cid,
                        isHost
                )
            }
        }
        updateStatus(rid, StatusType.STATUS_ONGOING)
    }

    override fun onUploadRequest(rid: String) {
        CoroutineScope(IO).launch {
            val requestInfo = database.requestDao().getRequestInfo(rid)
            d("rid = $rid source = ${requestInfo.source} name = ${requestInfo.name}")
            withContext(Main) {
                FileService.startActionSend(rid,
                        requestInfo.source,
                        requestInfo.cid,
                        isHost
                )
            }
        }
        updateStatus(rid, StatusType.STATUS_ONGOING)
    }


    override fun onNewRequestInfo(obj: RequestInfo) {
        CoroutineScope(IO).launch {
            obj.source = directoryResolver.getSource(obj)
            if (isHost) {
                database.requestDao().insert(obj)
                withContext(Main) {
                    FileService.startActionReceive(
                            obj.name,
                            obj.source,
                            obj.rid,
                            obj.cid,
                            true)
                }
                //sender cid
                val scid = obj.cid
                for (clientHelper in serverHelper.clientHelpers) {
                    if (clientHelper.user.userId != scid) {
                        val rid = getRandomId()
                        val clone = obj.clone(rid, clientHelper.user.userId)
                        clientHelper.write(clone)
                        //preparing intent for service
                        database.requestDao().insert(clone)

                        withContext(Main) {
                            FileService.startActionSend(
                                    clone.rid,
                                    clone.source,
                                    clone.cid,
                                    true)
                        }

                    }
                }
            } else {
                database.requestDao().insert(obj)
            }
        }
    }

    override fun onRequestFailed(rid: String) {
        updateStatus(rid, StatusType.STATUS_FAILED)
    }

    override fun onRequestAccepted(rid: String, cid: String, send: Boolean) {
        if (isHost) {
            serverHelper.clientHelpers.forEach {
                if (it.user.userId == cid) {
                    it.write(
                            FileRequest(if (send) FileRequest.DOWNLOAD_REQUEST_CONFIRM else FileRequest.UPLOAD_REQUEST_CONFIRM, rid)
                    )
                }
            }
        }
        updateStatus(rid, StatusType.STATUS_ONGOING)
    }

    override fun onRequestSuccess(rid: String, timeTaken: Long) {
        updateStatus(rid, StatusType.STATUS_COMPLETED)
        val historyViewModel = ViewModelProvider(this).get(HistoryViewModel::class.java)
        CoroutineScope(IO).launch {
            val requestInfo = database.requestDao().getRequestInfo(rid)
            historyViewModel.insert(
                    HistoryInfo(requestInfo.name, requestInfo.source, requestInfo.type)
            )
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when {
            item.itemId == android.R.id.home -> {
                onBackPressed()
            }

            R.id.menu_transferring == item.itemId -> {
                navController.navigate(R.id.transferringFragment)
            }
            item.itemId == R.id.menu_about -> {
                navController.navigate(object : NavDirections {
                    override fun getActionId(): Int {
                        return R.id.action_navigation_home_to_aboutFragment
                    }

                    override fun getArguments(): Bundle {
                        return Bundle()
                    }
                })
            }
            item.itemId == R.id.navigation_setting -> {
                navController.navigate(object : NavDirections {
                    override fun getActionId(): Int {
                        return R.id.action_navigation_home_to_navigation_setting
                    }

                    override fun getArguments(): Bundle {
                        return Bundle()
                    }
                })
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupReceiver() {
        val instance = LocalBroadcastManager.getInstance(this)
        val requestReceiver = FileRequestReceiver(this)
        val intentFilter = IntentFilter()
        intentFilter.addAction(FileService.STATUS_FAILED)
        intentFilter.addAction(FileService.STATUS_SUCCESS)
        intentFilter.addAction(FileService.REQUEST_ACCEPTED)
        instance.registerReceiver(requestReceiver, intentFilter)
    }

    override fun onRequestUsers(): List<ClientHelper> {
        return if (isHost) {
            serverHelper.clientHelpers
        } else ArrayList<ClientHelper>(setOf(clientHelper))
    }

    override fun onFragmentAttached(fragment: Fragment) {
        if (fragment is DashboardFragment) {
            onUsersUpdateListener = fragment
        }
    }

    override fun onFragmentDetached(fragment: Fragment) {
        if (fragment is DashboardFragment) {
            onUsersUpdateListener = null
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // request made by local Song fragment
        if (requestCode == 101) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                CoroutineScope(IO).launch {
                    PrepareDb().prepareAll()
                }
                navController.navigate(R.id.navigation_files)
            } else {
                Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == 9098) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                CoroutineScope(IO).launch {
                    PrepareDb().prepareAll()
                }
            } else {
                Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun send(o: Any) {
        if (isHost) {
            serverHelper.broadcast(o)
        } else {
            clientHelper.write(o)
        }
    }

    override fun onBackPressed() {
        val currentDestination = navController.currentDestination
        if (currentDestination != null && currentDestination.id == R.id.navigation_home) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Are you want to exit?")
            builder.setPositiveButton("Yes") { _: DialogInterface?, _: Int ->
                if (isHost) {
                    serverHelper.shutDown()
                } else {
                    clientHelper.shutDown()
                }
                finish()
            }
            builder.setNegativeButton("No", null)
            builder.create().show()
        } else super.onBackPressed()
    }

    override fun sendFiles(requests: List<RawRequestInfo>) {
        CoroutineScope(IO).launch {
            for (rawRequestInfo in requests) {
                val requestInfo = RequestInfo()
                requestInfo.name = rawRequestInfo.name
                requestInfo.source = rawRequestInfo.source
                requestInfo.type = rawRequestInfo.type
                if (isHost) {
                    for (clientHelper in serverHelper.clientHelpers) {
                        val rid = getRandomId()
                        val clone = requestInfo.clone(rid, clientHelper.user.userId)
                        //preparing intent for service
                        database.requestDao().insert(clone)
                        clientHelper.write(clone)
                        withContext(Main) {
                            FileService.startActionSend(
                                    clone.rid,
                                    clone.source,
                                    clone.cid,
                                    true)
                        }
                    }
                } else {
                    requestInfo.rid = getRandomId()
                    requestInfo.cid = user.userId
                    database.requestDao().insert(requestInfo)
                    send(requestInfo)
                }
            }
        }
    }

    override fun onClientConnected(clientHelper: ClientHelper) {
        runOnUiThread {
            onUsersUpdateListener?.onUserUpdated()
            Toast.makeText(this@MainActivity, "${clientHelper.user.name} connected", Toast.LENGTH_SHORT).show()
        }
        if (clientHelper.user.appVersion < BuildConfig.VERSION_CODE) {
            val source = packageManager.getInstallerPackageName(packageName)
            if (source != null)
                sendFiles(Collections.singletonList(RawRequestInfo(getString(R.string.app_name), source, FileType.FILE_TYPE_APP)))
        }
    }

    override fun onClientDisconnected(clientHelper: ClientHelper) {
        runOnUiThread { onUsersUpdateListener?.onUserUpdated() }

        //prompt client when disconnect to a party to create or rejoin the party
        if (!isHost) {
            runOnUiThread { choice() }
        }
    }

    private fun updateStatus(rid: String, status: Int) {
        requestViewModel.updateStatus(rid, status)
    }
}

