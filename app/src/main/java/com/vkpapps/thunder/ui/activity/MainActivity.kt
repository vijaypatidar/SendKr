package com.vkpapps.thunder.ui.activity

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
import com.vkpapps.thunder.App
import com.vkpapps.thunder.App.Companion.user
import com.vkpapps.thunder.BuildConfig
import com.vkpapps.thunder.R
import com.vkpapps.thunder.analitics.Logger
import com.vkpapps.thunder.analitics.Logger.d
import com.vkpapps.thunder.connection.ClientHelper
import com.vkpapps.thunder.connection.FileService
import com.vkpapps.thunder.connection.ServerHelper
import com.vkpapps.thunder.interfaces.*
import com.vkpapps.thunder.loader.PrepareAppList
import com.vkpapps.thunder.loader.PrepareDb
import com.vkpapps.thunder.model.HistoryInfo
import com.vkpapps.thunder.model.RawRequestInfo
import com.vkpapps.thunder.model.RequestInfo
import com.vkpapps.thunder.model.constant.FileType
import com.vkpapps.thunder.model.constant.StatusType
import com.vkpapps.thunder.room.liveViewModel.HistoryViewModel
import com.vkpapps.thunder.room.liveViewModel.RequestViewModel
import com.vkpapps.thunder.ui.dialog.DialogsUtils
import com.vkpapps.thunder.ui.dialog.PrivacyDialog
import com.vkpapps.thunder.ui.fragments.DashboardFragment
import com.vkpapps.thunder.ui.fragments.destinations.FragmentDestinationListener
import com.vkpapps.thunder.utils.*
import com.vkpapps.thunder.utils.HashUtils.getRandomId
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
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
        OnFileRequestReceiverListener, OnClientConnectionStateListener {

    private lateinit var navController: NavController
    private var onUsersUpdateListener: OnUsersUpdateListener? = null
    private var transferringProgressBar: ProgressBar? = null
    private var transferringCountTextView: AppCompatTextView? = null
    private val requestViewModel: RequestViewModel by lazy { ViewModelProvider(this).get(RequestViewModel::class.java) }
    private val historyViewModel: HistoryViewModel by lazy { ViewModelProvider(this).get(HistoryViewModel::class.java) }

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
            UpdateManager(true).checkForUpdate(true, this)
        } else {
            d("using old connection")
        }
        fileToShare()
        updateTransferringProgressBar()
        PrivacyDialog(this).isPolicyAccepted
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
        (menuView.getChildAt(0) as BottomNavigationItemView).dispatchSetSelected(true)
    }

    private fun choice() {
        if (PermissionUtils.checkStoragePermission(this)) {
            DialogsUtils(this).choice({
                startActivityForResult(Intent(this, CreateAccessPointActivity::class.java), CREATE_AP_ACTIVITY_RESULT)
            }, {
                startActivityForResult(Intent(this, ConnectionActivity::class.java), CONNECTION_ACTIVITY_RESULT)
            })
        } else {
            PermissionUtils.askStoragePermission(this, ASK_PERMISSION_FROM_MAIN_ACTIVITY)
        }
    }

    private fun setup(host: Boolean) {
        isHost = host
        if (isHost) {
            serverHelper = ServerHelper(this, user, this)
            serverHelper.start()
            connected = true
        } else {
            Thread {
                val socket = Socket()
                try {
                    val ipManager = IPManager(this)
                    val address = ipManager.hostIp()
                    d("setup: connection address $address")
                    FileService.HOST_ADDRESS = address
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
        // this method is only invoked for client
        val requestInfo = getRequestInfo(rid)
        FileService.startActionReceive(this@MainActivity,
                requestInfo,
                clientHelper
        )
    }

    override fun onUploadRequest(rid: String) {
        // this method is only invoked for client
        val requestInfo = getRequestInfo(rid)
        requestInfo.status = StatusType.STATUS_ONGOING
        d("onUploadRequest rid = $rid source = ${requestInfo.uri} name = ${requestInfo.name}")
        FileService.startActionSend(this@MainActivity, requestInfo,
                clientHelper
        )
    }

    override fun onNewRequestInfo(requestInfo: RequestInfo) {
        d(" new file request type = ${requestInfo.displaySize} ${requestInfo.name}")
        Logger.d("size=================${requestInfo.size}")
        if (isHost) {
            //new file request
            if (requestInfo.transferred == 0L) {
                requestViewModel.insert(requestInfo)
                serverHelper.clientHelpers.forEach {
                    if (it.user.userId == requestInfo.sid) {
                        FileService.startActionReceive(
                                this@MainActivity,
                                requestInfo,
                                it)
                    }
                }

                for (clientHelper in serverHelper.clientHelpers) {
                    if (clientHelper.user.userId != requestInfo.sid) {
                        val clone = requestInfo.clone()
                        clone.rid = getRandomId()
                        clone.cid = clientHelper.user.userId
                        clientHelper.write(clone)
                        //preparing intent for service
                        requestViewModel.insert(clone)

                        FileService.startActionSend(
                                this@MainActivity,
                                clone,
                                clientHelper)
                    }
                }
            } else {
                try {
                    val requestInfo1 = requestViewModel.getRequestInfo(requestInfo.rid)
                    requestInfo1?.status = requestInfo.status
                } catch (e: Exception) {

                }
            }
        } else {
            d("client new req size of file = ${requestInfo.size}")
            if (requestInfo.transferred == 0L) {
                requestViewModel.insert(requestInfo)
            } else {
                try {
                    val requestInfo1 = requestViewModel.getRequestInfo(requestInfo.rid)
                    requestInfo1?.status = requestInfo.status
                } catch (e: Exception) {

                }
            }
        }
    }


    override fun onRequestFailed(requestInfo: RequestInfo) {
        requestInfo.status = StatusType.STATUS_FAILED
        requestViewModel.decrementPendingRequestCount()
    }

    override fun onRequestAccepted(requestInfo: RequestInfo) {
        requestInfo.status = StatusType.STATUS_ONGOING
    }

    override fun onRequestSuccess(requestInfo: RequestInfo, send: Boolean) {
        CoroutineScope(IO).launch {
            if (requestInfo.transferred == requestInfo.size) {
                requestInfo.status = StatusType.STATUS_COMPLETED
                requestViewModel.decrementPendingRequestCount()
                if (!send) {
                    historyViewModel.insert(
                            HistoryInfo(requestInfo.rid, requestInfo.name, Uri.parse(requestInfo.uri), requestInfo.fileType)
                    )
                    // notify media store to scan files
                    sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(requestInfo.uri)))
                }
            } else {
                requestInfo.status = StatusType.STATUS_PAUSE
            }
            requestViewModel.notifyDataSetChanged()
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
                App.taskExecutor.shutdownNow()
                App.databasePrepared = false
                finish()
            }, null)
        } else super.onBackPressed()
    }

    override fun sendFiles(requests: List<RawRequestInfo>) {
        if (connected && (!isHost || serverHelper.clientHelpers.size != 0)) {
            CoroutineScope(IO).launch {
                for (rawRequestInfo in requests) {
                    if (rawRequestInfo.size != 0L) {
                        val requestInfo = RequestInfo()
                        requestInfo.name = rawRequestInfo.name
                        requestInfo.uri = rawRequestInfo.uri.toString()
                        requestInfo.fileType = rawRequestInfo.type
                        requestInfo.sid = user.userId
                        requestInfo.size = rawRequestInfo.size
                        requestInfo.displaySize = MathUtils.longToStringSize(rawRequestInfo.size.toDouble())
                        if (isHost) {
                            for (clientHelper in serverHelper.clientHelpers) {
                                val clone = requestInfo.clone()
                                clone.rid = getRandomId()
                                clone.cid = clientHelper.user.userId
                                //preparing intent for service
                                requestViewModel.insert(clone)
                                clientHelper.write(clone)
                                FileService.startActionSend(
                                        this@MainActivity,
                                        clone,
                                        clientHelper)
                            }
                        } else {
                            requestInfo.rid = getRandomId()
                            requestInfo.cid = user.userId
                            requestViewModel.insert(requestInfo)
                            clientHelper.write(requestInfo)
                        }
                    }
                }
            }
        } else {
            pendingRequest.addAll(requests)
            requestViewModel.notifyPendingCountChange()
            if (requests.isNotEmpty())
                CoroutineScope(Main).launch {
                    DialogsUtils(this@MainActivity).waitingForReceiver(pendingRequest.size)
                }
        }
    }

    override fun onClientConnected(clientHelper: ClientHelper) {
        runOnUiThread {
            onUsersUpdateListener?.onUserUpdated()
            Toast.makeText(this@MainActivity, "${clientHelper.user.name} connected", Toast.LENGTH_SHORT).show()
        }
        if (clientHelper.user.appVersion < BuildConfig.VERSION_CODE) {
            try {
                PrepareAppList.thunder?.run {
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
    private fun getRequestInfo(rid: String): RequestInfo {
        d("requested for RequestInfo where rid = $rid to insert")
        var requestInfo = requestViewModel.getRequestInfo(rid)
        var tryCount = 0
        while (requestInfo == null || tryCount != 10) {
            Logger.e("waiting for rid = $rid to insert")
            Thread.sleep(90)
            tryCount++
            requestInfo = requestViewModel.getRequestInfo(rid)
        }
        return requestInfo
    }

    private fun fileToShare() {
        if (PermissionUtils.checkStoragePermission(this)) {
            val toShare = intent.getParcelableArrayListExtra<Parcelable>("shared")
            if (toShare != null) {
                d("toShare = ${toShare.size} $toShare")
                val rawRequestInfos = ArrayList<RawRequestInfo>()
                toShare.forEach {
                    val uri = it as Uri
                    val file = DocumentFile.fromSingleUri(this@MainActivity, uri)
                    file?.run {
                        try {
                            rawRequestInfos.add(RawRequestInfo(file.name!!, file.uri, DownloadDestinationFolderResolver.getFileType(file.type), file.length()))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                sendFiles(rawRequestInfos)
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
                        clientHelper.write(user)
                    }
                }
            }
        } else if (requestCode == CONNECTION_ACTIVITY_RESULT) {
            if (resultCode == RESULT_OK) {
                setup(false)
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
        private val pendingRequest = ArrayList<RawRequestInfo>()

        @JvmStatic
        var connected: Boolean = false

        @JvmStatic
        var isHost: Boolean = false

        @JvmStatic
        private lateinit var serverHelper: ServerHelper

        @JvmStatic
        private lateinit var clientHelper: ClientHelper
    }
}

