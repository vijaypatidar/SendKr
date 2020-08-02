package com.vkpapps.thunder.ui.activity

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
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
import com.vkpapps.thunder.model.constaints.FileType
import com.vkpapps.thunder.model.constaints.StatusType
import com.vkpapps.thunder.room.database.MyRoomDatabase
import com.vkpapps.thunder.room.liveViewModel.HistoryViewModel
import com.vkpapps.thunder.room.liveViewModel.RequestViewModel
import com.vkpapps.thunder.ui.fragments.DashboardFragment
import com.vkpapps.thunder.ui.fragments.destinations.FragmentDestinationListener
import com.vkpapps.thunder.utils.*
import com.vkpapps.thunder.utils.HashUtils.getRandomId
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
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

    private var user = App.user
    private lateinit var navController: NavController
    private var onUsersUpdateListener: OnUsersUpdateListener? = null
    private var database = MyRoomDatabase.getDatabase(App.context)
    private val requestViewModel: RequestViewModel by lazy { ViewModelProvider(this).get(RequestViewModel::class.java) }
    private val downloadPathResolver: DownloadPathResolver by lazy { DownloadPathResolver(this) }

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
        setProfileActionView()

        if (!connected || (!isHost && !clientHelper.connected)) {
            d("creating new connection")
            choice()
            UpdateManager(true).checkForUpdate(true, this)
        } else {
            d("using old connection")
        }

        fileToShare()
    }

    private fun setProfileActionView() {
        val menuView: BottomNavigationMenuView = navView.getChildAt(0) as BottomNavigationMenuView
        val profileMenuItemView: BottomNavigationItemView = menuView.getChildAt(4) as BottomNavigationItemView
        val profileActionView = LayoutInflater.from(this).inflate(R.layout.profile_action_view, menuView, false)
        profileMenuItemView.addView(profileActionView)
        if (user.profileByteArray.isNotEmpty()) {
            val profilePic = profileActionView.findViewById<AppCompatImageView>(R.id.myProfilePic)
            profilePic.scaleType = ImageView.ScaleType.CENTER_CROP
            profilePic.setImageBitmap(BitmapUtils().byteArrayToBitmap(user.profileByteArray))
        }
        //create activity for updating profile
        profileActionView.setOnClickListener {
            startActivityForResult(Intent(this, ProfileActivity::class.java), REQUEST_UPDATE_PROFILE)
        }
        (menuView.getChildAt(0) as BottomNavigationItemView).dispatchSetSelected(true)
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
            connected = true
        } else {
            Thread(Runnable {
                val socket = Socket()
                try {
                    val ipManager = IPManager(this)
                    val address = ipManager.hostIp()
                    d("setup: connection address $address")
                    FileService.HOST_ADDRESS = address
                    socket.connect(InetSocketAddress(FileService.HOST_ADDRESS, 1203), 5000)
                    clientHelper = ClientHelper(socket, this, user, this)
                    clientHelper.start()
                    connected = true
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
        // this method is only invoked for client
        CoroutineScope(IO).launch {
            val requestInfo = getRequestInfo(rid)
            d("onDownloadRequest rid = $rid source = ${requestInfo.uri} name = ${requestInfo.name}")
            FileService.startActionReceive(this@MainActivity, Uri.parse(requestInfo.uri),
                    rid,
                    clientHelper,
                    requestInfo.fileType == FileType.FILE_TYPE_FOLDER
            )
            updateStatus(rid, StatusType.STATUS_ONGOING)
        }
    }

    override fun onUploadRequest(rid: String) {
        // this method is only invoked for client
        CoroutineScope(IO).launch {
            val requestInfo = getRequestInfo(rid)
            d("onUploadRequest rid = $rid source = ${requestInfo.uri} name = ${requestInfo.name}")
            FileService.startActionSend(this@MainActivity, rid,
                    Uri.parse(requestInfo.uri),
                    clientHelper,
                    requestInfo.fileType == FileType.FILE_TYPE_FOLDER
            )
            updateStatus(rid, StatusType.STATUS_ONGOING)
        }
    }

    override fun onNewRequestInfo(obj: RequestInfo) {
        CoroutineScope(IO).launch {
            obj.uri = Uri.fromFile(File(downloadPathResolver.getSource(obj))).toString()
            d(" new file request type = ${obj.fileType} ${obj.name}")
            if (isHost) {
                database.requestDao().insert(obj)
                serverHelper.clientHelpers.forEach {
                    if (it.user.userId == obj.cid) {
                        FileService.startActionReceive(
                                this@MainActivity,
                                Uri.parse(obj.uri),
                                obj.rid,
                                it,
                                obj.fileType == FileType.FILE_TYPE_FOLDER)
                    }
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

                        FileService.startActionSend(
                                this@MainActivity,
                                clone.rid,
                                Uri.parse(clone.uri),
                                clientHelper,
                                obj.fileType == FileType.FILE_TYPE_FOLDER)
                    }
                }
            } else {
                d("client new req size of file = ${obj.size}")
                database.requestDao().insert(obj)
            }
        }
    }

    override fun onRequestFailed(rid: String) {
        updateStatus(rid, StatusType.STATUS_FAILED)
    }

    override fun onRequestAccepted(rid: String, cid: String, send: Boolean) {
        updateStatus(rid, StatusType.STATUS_ONGOING)
    }

    override fun onRequestSuccess(rid: String, timeTaken: Long, send: Boolean) {
        updateStatus(rid, StatusType.STATUS_COMPLETED)
        if (!send) {
            val historyViewModel = ViewModelProvider(this).get(HistoryViewModel::class.java)
            CoroutineScope(IO).launch {
                val requestInfo = getRequestInfo(rid)
                historyViewModel.insert(
                        HistoryInfo(requestInfo.name, Uri.parse(requestInfo.uri), requestInfo.fileType)
                )
            }
        }
    }

    override fun onProgressChange(rid: String, transferred: Long) {
        d("progress change rid = $rid transferred = $transferred")
        requestViewModel.updateProgress(rid, transferred)
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
        if (requestCode == ASK_PERMISSION_FROM_GENERIC_FRAGMENT) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                CoroutineScope(IO).launch {
                    PrepareDb().prepareAll()
                }
                navController.navigate(R.id.navigation_files)
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
        if (connected && (!isHost || serverHelper.clientHelpers.size != 0)) {
            CoroutineScope(IO).launch {
                for (rawRequestInfo in requests) {
                    val requestInfo = RequestInfo()
                    requestInfo.name = rawRequestInfo.name
                    requestInfo.uri = rawRequestInfo.uri.toString()
                    requestInfo.fileType = rawRequestInfo.type
                    requestInfo.size = rawRequestInfo.size
                    if (isHost) {
                        for (clientHelper in serverHelper.clientHelpers) {
                            val rid = getRandomId()
                            val clone = requestInfo.clone(rid, clientHelper.user.userId)
                            //preparing intent for service
                            database.requestDao().insert(clone)
                            clientHelper.write(clone)
                            FileService.startActionSend(
                                    this@MainActivity,
                                    clone.rid,
                                    Uri.parse(clone.uri),
                                    clientHelper,
                                    rawRequestInfo.type == FileType.FILE_TYPE_FOLDER)
                        }
                    } else {
                        requestInfo.rid = getRandomId()
                        requestInfo.cid = user.userId
                        database.requestDao().insert(requestInfo)
                        clientHelper.write(requestInfo)
                    }
                }
            }
        } else {
            pendingRequest.addAll(requests)
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
                    Logger.d("i have new version $uri")
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
            runOnUiThread { choice() }
        }
    }

    private fun updateStatus(rid: String, status: Int) {
        requestViewModel.updateStatus(rid, status)
    }

    /**
     * this method will return request information
     */
    private suspend fun getRequestInfo(rid: String): RequestInfo {
        d("requested for RequestInfo where rid = $rid to insert")
        var requestInfos: List<RequestInfo> = database.requestDao().getRequestInfo(rid)
        while (requestInfos.isEmpty()) {
            delay(100)
            requestInfos = database.requestDao().getRequestInfo(rid)
            Logger.e("waiting for rid = $rid to insert")
        }
        return requestInfos[0]
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_UPDATE_PROFILE) {
            if (resultCode == RESULT_OK) {
                setProfileActionView()
                if (connected) {
                    if (isHost) {
                        serverHelper.broadcast(user)
                    } else {
                        clientHelper.write(user)
                    }
                }
            }
        }
    }

    companion object {
        const val ASK_PERMISSION_FROM_SHARED_INTENT = 0
        const val ASK_PERMISSION_FROM_GENERIC_FRAGMENT = 1
        const val ASK_PERMISSION_FROM_MAIN_ACTIVITY = 2
        const val REQUEST_UPDATE_PROFILE = 3

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

