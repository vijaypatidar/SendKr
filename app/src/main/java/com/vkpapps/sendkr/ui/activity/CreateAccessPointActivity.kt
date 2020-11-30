package com.vkpapps.sendkr.ui.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.vkpapps.apmanager.APManager
import com.vkpapps.sendkr.BuildConfig
import com.vkpapps.sendkr.R
import com.vkpapps.sendkr.analitics.Logger
import com.vkpapps.sendkr.interfaces.OnFailureListener
import com.vkpapps.sendkr.interfaces.OnSuccessListener
import com.vkpapps.sendkr.model.ConnectionBarCode
import com.vkpapps.sendkr.ui.activity.base.MyAppCompatActivity
import com.vkpapps.sendkr.ui.dialog.DialogsUtils
import com.vkpapps.sendkr.utils.BarCodeUtils
import com.vkpapps.sendkr.utils.IPManager
import com.vkpapps.sendkr.utils.PermissionUtils
import kotlinx.android.synthetic.main.activity_create_access_point.*
import java.lang.Exception

class CreateAccessPointActivity : MyAppCompatActivity(), APManager.OnFailureListener, APManager.OnSuccessListener {
    private var progressDialog: AlertDialog? = null
    private var alertGpsProviderRequire: AlertDialog? = null
    private var alertDisableHotspot: AlertDialog? = null
    private var alertDisableWifi: AlertDialog? = null
    private val apManager: APManager by lazy { APManager.getApManager(this@CreateAccessPointActivity) }

    override fun onDestroy() {
        super.onDestroy()
        progressDialog?.dismiss()
        alertGpsProviderRequire?.dismiss()
        alertDisableHotspot?.dismiss()
        alertDisableWifi?.dismiss()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Logger.d("[CreateAccessPointActivity][onCreate]")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_access_point)
        supportActionBar?.run {
            this.setDisplayHomeAsUpEnabled(true)
            this.elevation = 0f
        }
        initAlertsDialog()
        btnCreateHotspot.setOnClickListener {
            progressDialog?.show()
            apManager.turnOnHotspot(this, this, this)
        }
        btnUseExisting.setOnClickListener {
            progressDialog?.show()
            setResult(RESULT_OK)
            BarCodeUtils().createQR(ConnectionBarCode(ConnectionBarCode.CONNECTION_EXTERNAL_AP))
            finish()
        }
        btnUseRouter.setOnClickListener {
            progressDialog?.show()
            setResult(RESULT_OK)
            BarCodeUtils().createQR(ConnectionBarCode(ConnectionBarCode.CONNECTION_VIA_ROUTER).apply {
                ip = IPManager(this@CreateAccessPointActivity).deviceIp()
            })
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        Logger.d("[CreateAccessPointActivity][onResume]")
        createHotspot()
        if (BuildConfig.DEBUG) {
            useExistingSection.visibility = if (apManager.isWifiApEnabled) View.VISIBLE else View.GONE
            useRouterSection.visibility = if (apManager.wifiManager.isWifiEnabled
                    && apManager.isDeviceConnectedToWifi) View.VISIBLE else View.GONE
        }
    }

    private fun createHotspot() {
        Logger.d("[CreateAccessPointActivity][createHotspot]")
        progressDialog?.show()
        if (!apManager.isWifiApEnabled && !(apManager.wifiManager.isWifiEnabled && apManager.isDeviceConnectedToWifi)) {
            apManager.turnOnHotspot(this, this, this)
        } else if (apManager.isWifiApEnabled) {
            onFailure(APManager.ERROR_DISABLE_HOTSPOT, null)
        } else {
            onFailure(APManager.ERROR_DISABLE_WIFI, null)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onFailure(t: Int, e: Exception?) {
        progressDialog?.hide()
        when (t) {
            APManager.ERROR_LOCATION_PERMISSION_DENIED -> {
                PermissionUtils.askLocationPermission(this, MainActivity.ASK_LOCATION_PERMISSION)
            }
            APManager.ERROR_GPS_PROVIDER_DISABLED -> {
                alertGpsProviderRequire?.show()
            }
            APManager.ERROR_WRITE_SETTINGS_PERMISSION_REQUIRED -> {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    apManager.utils.askWriteSettingPermission(this)
                }
            }
            APManager.ERROR_DISABLE_HOTSPOT -> {
                alertDisableHotspot?.show()
            }
            APManager.ERROR_DISABLE_WIFI -> {
                alertDisableWifi?.show()
            }
        }

        e?.let { it ->
            it.message?.let { msg -> FirebaseCrashlytics.getInstance().log(msg) }
        }
    }

    override fun onSuccess(ssid: String, password: String) {
        val connectionBarCode = ConnectionBarCode(ConnectionBarCode.CONNECTION_INTERNAL_AP)
        connectionBarCode.ssid = ssid
        connectionBarCode.password = password
        BarCodeUtils().createQR(connectionBarCode)
        setResult(RESULT_OK)
        finish()
    }

    private fun initAlertsDialog() {
        alertGpsProviderRequire = DialogsUtils(this).alertGpsProviderRequire()
        progressDialog = DialogsUtils(this).alertLoadingDialog()
        alertDisableHotspot = DialogsUtils(this).alertDisableHotspot(object : OnSuccessListener<String> {
            override fun onSuccess(t: String) {
                startActivity(apManager.utils.tetheringSettingIntent)
            }
        }, object : OnFailureListener<String> {
            override fun onFailure(t: String) {

            }
        })
        alertDisableWifi = DialogsUtils(this).alertDisableWifi(object : OnSuccessListener<String> {
            override fun onSuccess(t: String) {
                startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            }
        }, object : OnFailureListener<String> {
            override fun onFailure(t: String) {

            }
        })
    }
}