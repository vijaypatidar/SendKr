package com.vkpapps.sendkr.ui.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.net.toFile
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.vkpapps.sendkr.App
import com.vkpapps.sendkr.App.Companion.user
import com.vkpapps.sendkr.BuildConfig
import com.vkpapps.sendkr.R
import com.vkpapps.sendkr.analitics.Logger.d
import com.vkpapps.sendkr.connection.ClientHelper
import com.vkpapps.sendkr.connection.FileService
import com.vkpapps.sendkr.connection.FileService.Companion.taskExecutor
import com.vkpapps.sendkr.connection.ServerHelper
import com.vkpapps.sendkr.interfaces.*
import com.vkpapps.sendkr.loader.PrepareAppList
import com.vkpapps.sendkr.loader.PrepareDb
import com.vkpapps.sendkr.model.FileStatusRequest
import com.vkpapps.sendkr.model.HistoryInfo
import com.vkpapps.sendkr.model.RawRequestInfo
import com.vkpapps.sendkr.model.RequestInfo
import com.vkpapps.sendkr.model.constant.FileType
import com.vkpapps.sendkr.model.constant.StatusType
import com.vkpapps.sendkr.room.liveViewModel.HistoryViewModel
import com.vkpapps.sendkr.room.liveViewModel.QuickAccessViewModel
import com.vkpapps.sendkr.room.liveViewModel.RequestViewModel
import com.vkpapps.sendkr.ui.dialog.DialogsUtils
import com.vkpapps.sendkr.ui.dialog.PrivacyDialog
import com.vkpapps.sendkr.ui.fragments.DashboardFragment
import com.vkpapps.sendkr.ui.fragments.destinations.FragmentDestinationListener
import com.vkpapps.sendkr.utils.*
import com.vkpapps.sendkr.utils.HashUtils.getRandomId
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
import kotlin.collections.ArrayList

/**
 * @author VIJAY PATIDAR
 */
class MainActivity : AppCompatActivity(), OnNavigationVisibilityListener, OnUserListRequestListener,
        OnFragmentAttachStatusListener, OnFileRequestListener, OnFileRequestPrepareListener,
        OnFileRequestReceiverListener, OnClientConnectionStateListener, OnFileStatusChangeListener {

    private lateinit var navController: NavController
    private var onUsersUpdateListener: OnUsersUpdateListener? = null
    private var transferringProgressBar: ProgressBar? = null
    private var transferringCountTextView: AppCompatTextView? = null
    private val requestViewModel: RequestViewModel by lazy { ViewModelProvider(this).get(RequestViewModel::class.java) }
    private val historyViewModel: HistoryViewModel by lazy { ViewModelProvider(this).get(HistoryViewModel::class.java) }
    private val quickAccessViewModel: QuickAccessViewModel by lazy { ViewModelProvider(this).get(QuickAccessViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.elevation = 0f
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_app, R.id.navigation_dashboard, R.id.navigation_media)
                .build()
        navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
        NavigationUI.setupWithNavController(navView, navController)
        navController.addOnDestinationChangedListener(FragmentDestinationListener(this))
        setProfileActionView()

        if (!connected || (!isHost && !clientHelper.connected)) {
            d("creating new connection")
            choice()
            UpdateManager().checkForUpdate(this)
        } else {
            d("using old connection")
        }
        fileToShare()
        updateTransferringProgressBar()
        PrivacyDialog(this).isPolicyAccepted
        quickAccessViewModel.refreshData()

        if (BuildConfig.DEBUG) {
            historyViewModel.insert(
                    HistoryInfo("requestInfo.rid", "requestInfo.name", Uri.parse(""), 1)
            )
        }
    }

    private fun setProfileActionView() {
        val menuView: BottomNavigationMenuView = navView.getChildAt(0) as BottomNavigationMenuView
        val profileMenuItemView: BottomNavigationItemView = menuView.getChildAt(4) as BottomNavigationItemView
        val profileActionView = LayoutInflater.from(this).inflate(R.layout.profile_action_view, menuView, false)
        profileMenuItemView.addView(profileActionView)
        if (user.profileByteArray.isNotEmpty()) {
            val profilePic = profileActionView.findViewById<AppCompatImageView>(R.id.myProfilePic)
            profilePic.scaleType = ImageView.ScaleType.CENTER_CROP
            profilePic.setImageBitmap(BitmapUtils.byteArrayToBitmap(user.profileByteArray))
        }
        //create activity for updating profile
        profileActionView.setOnClickListener {
            startActivityForResult(Intent(this, ProfileActivity::class.java), REQUEST_UPDATE_PROFILE)
        }
    }

    private fun choice() {
        DialogsUtils(this).choice({
            if (PermissionUtils.checkStoragePermission(this)) {
                startActivityForResult(Intent(this, CreateAccessPointActivity::class.java), CREATE_AP_ACTIVITY_RESULT)
            } else {
                PermissionUtils.askStoragePermission(this, ASK_PERMISSION_FROM_MAIN_ACTIVITY)
            }
        }, {
            if (PermissionUtils.checkStoragePermission(this)) {
                startActivityForResult(Intent(this, ConnectionActivity::class.java), CONNECTION_ACTIVITY_RESULT)
            } else {
                PermissionUtils.askStoragePermission(this, ASK_PERMISSION_FROM_MAIN_ACTIVITY)
            }
        })
    }

    private fun setup(host: Boolean, hostIP: String? = null) {
        isHost = host
        if (isHost) {
            serverHelper = ServerHelper(this, user, this)
            serverHelper.start()
            connected = true
        } else {
            Thread {
                val socket = Socket()
                try {
                    if (hostIP != null) {
                        FileService.HOST_ADDRESS = hostIP
                    } else {
                        val ipManager = IPManager(this)
                        val address = ipManager.hostIp()
                        FileService.HOST_ADDRESS = address
                    }
                    ConnectionActivity.network?.bindSocket(socket)
                    socket.connect(InetSocketAddress(FileService.HOST_ADDRESS, ServerHelper.PORT), 3000)
                    clientHelper = ClientHelper(socket, this, user, this)
                    clientHelper.start()
                    connected = true
                } catch (e: IOException) {
                    runOnUiThread {
                        DialogsUtils(this).joinHotspotFailed(
                                { setup(false) },
                                { choice() }
                        )
                    }
                    e.printStackTrace()
                }
            }.start()
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
        d("[MainActivity][onDownloadRequest] rid = $rid")
        // this method is only invoked for client
        getRequestInfo(rid)?.run {
            FileService.startActionReceive(this@MainActivity,
                    this,
                    clientHelper
            )
        }
    }

    override fun onUploadRequest(rid: String) {
        d("[MainActivity][onUploadRequest] rid = $rid")
        // this method is only invoked for client
        getRequestInfo(rid)?.run {
            FileService.startActionSend(this@MainActivity, this,
                    clientHelper
            )
        }
    }

    override fun onStatusChange(requestInfo: RequestInfo) {
        d("[MainActivity][onStatusChange] rid = ${requestInfo.rid} status = ${requestInfo.status}")
        if (requestInfo.status != StatusType.STATUS_COMPLETED) {
            if (isHost) {
                serverHelper.clientHelpers.forEach {
                    if (it.user.userId == requestInfo.cid) {
                        it.send(FileStatusRequest(requestInfo.status, requestInfo.rid))
                        if (requestInfo.status == StatusType.STATUS_PENDING || requestInfo.status == StatusType.STATUS_RETRY) {
                            if (requestInfo.send) {
                                FileService.startActionSend(this@MainActivity, requestInfo, it)
                            } else {
                                FileService.startActionReceive(this@MainActivity, requestInfo, it)
                            }
                        }
                        return
                    }
                }
            } else {
                clientHelper.send(FileStatusRequest(requestInfo.status, requestInfo.rid))
            }
        }
    }

    override fun onNewRequestInfo(requestInfo: RequestInfo, clientHelper: ClientHelper) {
        d("[MainActivity][onNewRequestInfo] rid = ${requestInfo.rid} status = ${requestInfo.status}")
        requestInfo.uri = null
        if (isHost) {
            //new file request
            requestViewModel.insert(requestInfo)
            FileService.startActionReceive(this@MainActivity, requestInfo, clientHelper)
            //todo send to other from one client to other

        } else {
            requestViewModel.insert(requestInfo)
        }
    }

    override fun onFileStatusChange(fileStatusRequest: FileStatusRequest, clientHelper: ClientHelper) {
        d("[MainActivity][onFileStatusChange] rid = ${fileStatusRequest.rid} status = ${fileStatusRequest.status}")
        if (isHost) {
            try {
                requestViewModel.getRequestInfo(fileStatusRequest.rid)?.run {
                    if (fileStatusRequest.status == StatusType.STATUS_RETRY) {
                        requestViewModel.incrementPendingRequestCount()
                        this.status = StatusType.STATUS_PENDING
                        this.transferred = 0
                    } else {
                        this.status = fileStatusRequest.status
                    }
                    if (this.status == StatusType.STATUS_PENDING) {
                        if (this.send) {
                            d("[MainActivity][onFileStatusChange] rid = inside if send")
                            FileService.startActionSend(this@MainActivity, this, clientHelper)
                        } else {
                            d("[MainActivity][onFileStatusChange] rid = inside if receive")
                            FileService.startActionReceive(this@MainActivity, this, clientHelper)
                        }
                    }
                }
            } catch (e: Exception) {

            }
        } else {
            requestViewModel.getRequestInfo(fileStatusRequest.rid)?.run {
                if (fileStatusRequest.status == StatusType.STATUS_RETRY) {
                    this.status = StatusType.STATUS_PENDING
                    this.transferred = 0
                } else {
                    this.status = fileStatusRequest.status
                }
            }
        }
    }

    override fun onRequestFailed(requestInfo: RequestInfo) {
        requestViewModel.decrementPendingRequestCount()
    }

    override fun onRequestAccepted(requestInfo: RequestInfo) {

    }

    override fun onRequestSuccess(requestInfo: RequestInfo, send: Boolean) {
        requestViewModel.requestCompleted(requestInfo)
        CoroutineScope(IO).launch {
            if (requestInfo.status == StatusType.STATUS_COMPLETED) {
                requestViewModel.decrementPendingRequestCount()
                if (!send) {
                    historyViewModel.insert(
                            HistoryInfo(requestInfo.rid, requestInfo.name, Uri.parse(requestInfo.uri), requestInfo.fileType)
                    )
                    // notify media store to scan files
                    sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(requestInfo.uri)))
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
            R.id.menu_transferring -> {
                navController.navigate(R.id.transferringFragment)
            }
            R.id.menu_about -> {
                navController.navigate(object : NavDirections {
                    override fun getActionId(): Int {
                        return R.id.action_navigation_home_to_aboutFragment
                    }

                    override fun getArguments(): Bundle {
                        return Bundle()
                    }
                })
            }
            R.id.navigation_setting -> {
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
        menu.findItem(R.id.menu_transferring).actionView?.run {
            transferringProgressBar = this.findViewById(R.id.transferringProgressBar)
            transferringCountTextView = this.findViewById(R.id.pendingCount)
        }
        requestViewModel.notifyPendingCountChange()
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // request made by local Song fragment
        try {

            if (requestCode == ASK_PERMISSION_FROM_GENERIC_FRAGMENT) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    CoroutineScope(IO).launch {
                        PrepareDb().prepareAll()
                    }
                    navController.navigate(R.id.navigation_media)
                } else {
                    Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show()
                }
            } else if (requestCode == ASK_PERMISSION_FROM_SHARED_INTENT) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    CoroutineScope(IO).launch {
                        PrepareDb().prepareAll()
                    }
                    fileToShare()
                } else {
                    Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show()
                }
            } else if (requestCode == ASK_PERMISSION_FROM_MAIN_ACTIVITY) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    CoroutineScope(IO).launch {
                        PrepareDb().prepareAll()
                    }
                } else {
                    Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {

        }
    }

    override fun onBackPressed() {
        val currentDestination = navController.currentDestination
        if (currentDestination != null && currentDestination.id == R.id.navigation_home) {
            DialogsUtils(this).exitAppAlert({
                if (isHost) {
                    serverHelper.shutDown()
                } else {
                    clientHelper.shutDown()
                }
                connected = false
                taskExecutor.shutdownNow()
                App.databasePrepared = false
                finish()
            }, null)
        } else super.onBackPressed()
    }


    override fun sendFiles(requests: List<RawRequestInfo>) {
        d("[MainActivity][sendFiles]  requests = ${requests.size}")
        if (connected && (!isHost || serverHelper.clientHelpers.size != 0)) {
            CoroutineScope(IO).launch {
                for (rawRequestInfo in requests) {
                    if (rawRequestInfo.size != 0L) {
                        val requestInfo = RequestInfo()
                        requestInfo.name = rawRequestInfo.name
                        requestInfo.uri = rawRequestInfo.uri.toString()
                        requestInfo.fileType = rawRequestInfo.type
                        requestInfo.send = true
                        requestInfo.size = rawRequestInfo.size
                        if (isHost) {
                            for (clientHelper in serverHelper.clientHelpers) {
                                val clone = requestInfo.clone()
                                clone.rid = getRandomId()
                                clone.cid = clientHelper.user.userId
                                //preparing intent for service
                                requestViewModel.insert(clone)
                                clientHelper.send(clone)
                                FileService.startActionSend(
                                        this@MainActivity,
                                        clone,
                                        clientHelper)
                            }
                        } else {
                            requestInfo.rid = getRandomId()
                            requestViewModel.insert(requestInfo)
                            clientHelper.send(requestInfo)
                        }
                    }
                }
            }
        } else {
            pendingRequest.addAll(requests)
            requestViewModel.notifyPendingCountChange()
            if (requests.isNotEmpty()) {
                CoroutineScope(Main).launch {
                    DialogsUtils(this@MainActivity).waitingForReceiver(pendingRequest.size)
                }
            }
        }
    }

    override fun onClientConnected(clientHelper: ClientHelper) {
        runOnUiThread {
            onUsersUpdateListener?.onUserUpdated()
        }
        if (clientHelper.user.appVersion < BuildConfig.VERSION_CODE) {
            try {
                PrepareAppList.sendKr?.run {
                    val uri = this.uri
                    sendFiles(Collections.singletonList(RawRequestInfo(this.name,
                            uri,
                            FileType.FILE_TYPE_APP,
                            uri.toFile().length()))
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (pendingRequest.isNotEmpty()) {
            synchronized(pendingRequest) {
                sendFiles(ArrayList<RawRequestInfo>(pendingRequest))
                pendingRequest.clear()
            }
        }
    }

    override fun onClientDisconnected(clientHelper: ClientHelper) {
        runOnUiThread { onUsersUpdateListener?.onUserUpdated() }

        //prompt client when disconnect
        if (!isHost) {
            connected = false
            requestViewModel.clearRequestList()
            runOnUiThread { choice() }
        }
    }

    override fun onClientInformationChanged(clientHelper: ClientHelper) {
        runOnUiThread { onUsersUpdateListener?.onUserUpdated() }
    }

    /**
     * @return return request information
     */
    private fun getRequestInfo(rid: String): RequestInfo? {
        d("[MainActivity][getRequestInfo]  rid = $rid")
        var requestInfo = requestViewModel.getRequestInfo(rid)
        var tryCount = 0
        while (requestInfo == null && tryCount != 15) {
            d("[MainActivity][getRequestInfo]  waiting for rid = $rid  tryCount $tryCount")
            Thread.sleep(90)
            tryCount++
            requestInfo = requestViewModel.getRequestInfo(rid)
        }
        return requestInfo
    }

    private fun fileToShare() {
        if (PermissionUtils.checkStoragePermission(this)) {
            val loading = DialogsUtils(this@MainActivity).alertLoadingDialog()
            loading.show()
            CoroutineScope(IO).launch {
                val toShare = intent.getParcelableArrayListExtra<Parcelable>("shared")
                if (toShare != null) {
                    val rawRequestInfos = ArrayList<RawRequestInfo>()
                    toShare.forEach {
                        val uri = it as Uri
                        val file = DocumentFile.fromSingleUri(this@MainActivity, uri)
                        file?.run {
                            try {
                                rawRequestInfos.add(RawRequestInfo(file.name!!, file.uri, FileTypeResolver.getFileType(file.type), file.length()))
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                    sendFiles(rawRequestInfos)
                }
                withContext(Main) {
                    loading.cancel()
                }
            }
        } else {
            PermissionUtils.askStoragePermission(this, ASK_PERMISSION_FROM_SHARED_INTENT)
        }
    }

    private fun updateTransferringProgressBar() {
        requestViewModel.pendingRequestCountLiveData.observe(this, {
            val visible = if (it == 0 && pendingRequest.size == 0) View.GONE else View.VISIBLE
            transferringProgressBar?.visibility = visible
            transferringCountTextView?.visibility = visible
            val count = if (it == 0) pendingRequest.size else it
            if (count > 100) {
                transferringCountTextView?.text = "9+"
            } else {
                transferringCountTextView?.text = count.toString()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_UPDATE_PROFILE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show()
                setProfileActionView()
                if (connected) {
                    if (isHost) {
                        serverHelper.broadcast(user)
                    } else {
                        clientHelper.send(user)
                    }
                }
            }
        } else if (requestCode == CONNECTION_ACTIVITY_RESULT) {
            if (resultCode == RESULT_OK) {
                val hostIP = data?.getStringExtra(ConnectionActivity.PARAM_CONNECTION_HOST_IP)
                setup(false, hostIP)
            } else {
                choice()
            }
        } else if (requestCode == CREATE_AP_ACTIVITY_RESULT) {
            if (resultCode == RESULT_OK) {
                navController.navigate(R.id.navigation_dashboard)
                setup(true)
            } else {
                choice()
            }
        }
    }

    companion object {
        const val ASK_PERMISSION_FROM_SHARED_INTENT = 0
        const val ASK_PERMISSION_FROM_GENERIC_FRAGMENT = 1
        const val ASK_PERMISSION_FROM_MAIN_ACTIVITY = 2
        const val ASK_LOCATION_PERMISSION = 3
        const val REQUEST_UPDATE_PROFILE = 4
        const val CONNECTION_ACTIVITY_RESULT = 5

        const val CREATE_AP_ACTIVITY_RESULT = 6

        @JvmStatic
        val pendingRequest = ArrayList<RawRequestInfo>()

        @JvmStatic
        var connected: Boolean = false

        @JvmStatic
        var isHost: Boolean = false

        @JvmStatic
        internal lateinit var serverHelper: ServerHelper

        @JvmStatic
        internal lateinit var clientHelper: ClientHelper
    }
}

