package com.vkpapps.sendkr.ui.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import com.vkpapps.sendkr.BuildConfig
import com.vkpapps.sendkr.R
import com.vkpapps.sendkr.analitics.Logger
import com.vkpapps.sendkr.interfaces.OnFailureListener
import com.vkpapps.sendkr.interfaces.OnSuccessListener
import com.vkpapps.sendkr.model.ConnectionBarCode
import com.vkpapps.sendkr.ui.dialog.DialogsUtils
import com.vkpapps.sendkr.utils.BarCodeUtils
import com.vkpapps.sendkr.utils.IPManager
import com.vkpapps.sendkr.utils.PermissionUtils
import com.vkpapps.sendkr.utils.WifiApUtils
import kotlinx.android.synthetic.main.activity_create_access_point.*

class CreateAccessPointActivity : MyAppCompatActivity(), OnFailureListener<Int>, OnSuccessListener<String> {
    private var progressDialog: AlertDialog? = null
    private var alertGpsProviderRequire: AlertDialog? = null
    private var alertDisableHotspot: AlertDialog? = null
    private var alertDisableWifi: AlertDialog? = null

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
            WifiApUtils.turnOnHotspot(this, this, this)
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
            useExistingSection.visibility = if (WifiApUtils.isWifiApEnabled()) View.VISIBLE else View.GONE
            useRouterSection.visibility = if (WifiApUtils.wifiManager.isWifiEnabled
                    && IPManager(this@CreateAccessPointActivity).deviceIp() != "0.0.0.0") View.VISIBLE else View.GONE
        }
    }

    private fun createHotspot() {
        Logger.d("[CreateAccessPointActivity][createHotspot]")
        progressDialog?.show()
        if (!WifiApUtils.isWifiApEnabled() && !(WifiApUtils.wifiManager.isWifiEnabled && IPManager(this@CreateAccessPointActivity).deviceIp() != "0.0.0.0")) {
            WifiApUtils.turnOnHotspot(this, this, this)
        } else if (WifiApUtils.isWifiApEnabled()) {
            onFailure(WifiApUtils.ERROR_DISABLE_HOTSPOT)
        } else {
            onFailure(WifiApUtils.ERROR_DISABLE_WIFI)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onFailure(t: Int) {
        progressDialog?.hide()
        when (t) {
            WifiApUtils.ERROR_LOCATION_PERMISSION_DENIED -> {
                PermissionUtils.askLocationPermission(this, MainActivity.ASK_LOCATION_PERMISSION)
            }
            WifiApUtils.ERROR_ENABLE_GPS_PROVIDER -> {
                alertGpsProviderRequire?.show()
            }
            WifiApUtils.ERROR_WRITE_PERMITSiON_REQUIRED -> {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    PermissionUtils.askWriteSettingPermission(this, 11111)
                }
            }
            WifiApUtils.ERROR_DISABLE_HOTSPOT -> {
                alertDisableHotspot?.show()
            }
            WifiApUtils.ERROR_DISABLE_WIFI -> {
                alertDisableWifi?.show()
            }
        }
    }

    override fun onSuccess(t: String) {
        val connectionBarCode = ConnectionBarCode(ConnectionBarCode.CONNECTION_INTERNAL_AP)
        connectionBarCode.ssid = WifiApUtils.ssid
        connectionBarCode.password = WifiApUtils.password
        BarCodeUtils().createQR(connectionBarCode)
        setResult(RESULT_OK)
        finish()
    }

    private fun initAlertsDialog() {
        alertGpsProviderRequire = DialogsUtils(this).alertGpsProviderRequire()
        progressDialog = DialogsUtils(this).alertLoadingDialog()
        alertDisableHotspot = DialogsUtils(this).alertDisableHotspot(object : OnSuccessListener<String> {
            override fun onSuccess(t: String) {
                startActivity(WifiApUtils.getTetheringSettingIntent())
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