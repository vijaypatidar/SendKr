package com.vkpapps.thunder.ui.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.vkpapps.thunder.R
import com.vkpapps.thunder.analitics.Logger
import com.vkpapps.thunder.interfaces.OnFailureListener
import com.vkpapps.thunder.interfaces.OnSuccessListener
import com.vkpapps.thunder.model.ConnectionBarCode
import com.vkpapps.thunder.ui.dialog.DialogsUtils
import com.vkpapps.thunder.utils.BarCodeUtils
import com.vkpapps.thunder.utils.PermissionUtils
import com.vkpapps.thunder.utils.WifiApUtils
import kotlinx.android.synthetic.main.activity_create_access_point.*

class CreateAccessPointActivity : AppCompatActivity(), OnFailureListener<Int>, OnSuccessListener<String> {
    var progressDialog: AlertDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        Logger.d("[CreateAccessPointActivity][onCreate]")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_access_point)
        supportActionBar?.run {
            this.setDisplayHomeAsUpEnabled(true)
            this.elevation = 0f
        }
        progressDialog = DialogsUtils(this).alertLoadingDialog()

    }

    override fun onResume() {
        super.onResume()
        Logger.d("[CreateAccessPointActivity][onResume]")
        createHotspot()
        btnCreateHotspot.setOnClickListener {
            progressDialog?.show()
            WifiApUtils.turnOnHotspot(this, this, this)
        }

        if (WifiApUtils.isWifiApEnabled()) {
            useExistingSection.visibility = View.VISIBLE
            btnUseExisting.setOnClickListener {
                setResult(RESULT_OK)
                BarCodeUtils().createQR(ConnectionBarCode(ConnectionBarCode.CONNECTION_EXTERNAL_AP))
                finish()
            }
        }

//        if (WifiApUtils.wifiManager.isWifiEnabled) {
//            useRouterSection.visibility = View.VISIBLE
//            btnUseRouter.setOnClickListener {
//                setResult(RESULT_OK)
//                BarCodeUtils().createQR(ConnectionBarCode(ConnectionBarCode.CONNECTION_VIA_ROUTER).apply {
//                    ip = WifiApUtils.wifiManager.dhcpInfo.ipAddress
//                })
//                finish()
//            }
//        }
    }

    private fun createHotspot() {
        progressDialog?.show()
        if (!WifiApUtils.isWifiApEnabled() && !WifiApUtils.wifiManager.isWifiEnabled) {
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
                DialogsUtils(this).alertGpsProviderRequire()
            }
            WifiApUtils.ERROR_DISABLE_HOTSPOT -> {
                DialogsUtils(this).alertDisableHotspot(object : OnSuccessListener<String> {
                    override fun onSuccess(t: String) {
                        startActivity(WifiApUtils.getTetheringSettingIntent())
                    }
                }, object : OnFailureListener<String> {
                    override fun onFailure(t: String) {

                    }
                })
            }
            WifiApUtils.ERROR_DISABLE_WIFI -> {
                DialogsUtils(this).alertDisableWifi(object : OnSuccessListener<String> {
                    override fun onSuccess(t: String) {
                        startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                    }
                }, object : OnFailureListener<String> {
                    override fun onFailure(t: String) {

                    }
                })
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
}