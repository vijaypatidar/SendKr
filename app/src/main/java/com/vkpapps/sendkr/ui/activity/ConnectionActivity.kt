package com.vkpapps.sendkr.ui.activity


import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import com.google.zxing.BarcodeFormat
import com.vkpapps.sendkr.App
import com.vkpapps.sendkr.R
import com.vkpapps.sendkr.analitics.Logger
import com.vkpapps.sendkr.interfaces.OnFailureListener
import com.vkpapps.sendkr.interfaces.OnSuccessListener
import com.vkpapps.sendkr.model.ConnectionBarCode
import com.vkpapps.sendkr.model.constant.Constants
import com.vkpapps.sendkr.ui.dialog.DialogsUtils
import com.vkpapps.sendkr.utils.BarCodeUtils
import com.vkpapps.sendkr.utils.PermissionUtils
import kotlinx.android.synthetic.main.activity_connection.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class ConnectionActivity : AppCompatActivity() {
    companion object {
        var network: Network? = null
        const val PARAM_CONNECTION_TYPE = "com.vkpapps.thunder.PARAM_CONNECTION_TYPE"
    }

    private val wifiManager = App.context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val connectivityManager = App.context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connection)
        window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        initUI()

        scanner.setFormats(Collections.singletonList(BarcodeFormat.QR_CODE))
        scanner.setResultHandler {
            connect(it.text)
        }
        startScanner()
    }

    private fun startScanner() {
        if (PermissionUtils.checkLCameraPermission(this)) {
            scanner.startCamera()
        } else {
            showCameraPermissionAskDialog()
        }
    }


    private fun showCameraPermissionAskDialog() {
        DialogsUtils(this).alertCameraPermissionRequire({
            PermissionUtils.askCameraPermission(this, Constants.CONNECTION_ACTIVITY_ASK_CAMERA_PERMISSION)
        }, {
            cameraPermissionDenied.visibility = View.VISIBLE
        })
    }

    private fun initUI() {
        scanner.setBorderCornerRadius(25)
        scanner.setBorderStrokeWidth(10)
        scanner.setMaskColor(Color.TRANSPARENT)
        scanner.setLaserColor(Color.parseColor("#00de7a"))
        scanner.setBorderColor(Color.parseColor("#fc8210"))
        scanner.setIsBorderCornerRounded(true)
        btnAllow.setOnClickListener {
            showCameraPermissionAskDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        scanner.startCamera()
        if (!wifiManager.isWifiEnabled) {
            DialogsUtils(this).alertEnableWifi(object : OnSuccessListener<String> {
                override fun onSuccess(t: String) {
                    startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                }
            }, object : OnFailureListener<String> {
                override fun onFailure(t: String) {
                    Toast.makeText(this@ConnectionActivity, "Failed,Wi-Fi is disabled.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            })
        }
    }

    override fun onPause() {
        super.onPause()
        scanner.stopCamera()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.CONNECTION_ACTIVITY_ASK_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scanner.startCamera()
                cameraPermissionDenied.visibility = View.GONE
            } else {
                cameraPermissionDenied.visibility = View.VISIBLE
            }
        }
    }

    private fun connect(input: String) {
        try {
            val connectionBarCode = GsonBuilder().create().fromJson(input, ConnectionBarCode::class.java)
            Logger.d("[ConnectionActivity][connect] scan result = $input")
            BarCodeUtils().createQR(connectionBarCode)
            if (connectionBarCode.connectionType == ConnectionBarCode.CONNECTION_INTERNAL_AP) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val networkSpecifier = WifiNetworkSpecifier.Builder()
                            .setSsid(connectionBarCode.ssid!!)
                            .setWpa2Passphrase(connectionBarCode.password!!)
                            .build()
                    val networkRequest = NetworkRequest.Builder()
                            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                            .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                            .setNetworkSpecifier(networkSpecifier)
                            .build()
                    connectivityManager.requestNetwork(networkRequest, object : ConnectivityManager.NetworkCallback() {
                        override fun onAvailable(network: Network) {
                            super.onAvailable(network)
                            ConnectionActivity.network = network
                            Logger.d("onAvailable")
                            setResult(RESULT_OK, Intent().apply {
                                putExtra(PARAM_CONNECTION_TYPE, connectionBarCode.connectionType)
                            })
                            finish()
                        }

                        override fun onLost(network: Network) {
                            super.onLost(network)
                            Logger.d("onLost")
                        }
                    })
                } else {
                    val configuration = WifiConfiguration()
                    configuration.SSID = "\"${connectionBarCode.ssid}\""
                    configuration.preSharedKey = "\"${connectionBarCode.password}\""
                    val addNetwork = wifiManager.addNetwork(configuration)
                    wifiManager.disconnect()
                    wifiManager.enableNetwork(addNetwork, true)
                    setupNetwork(connectionBarCode)
                }
            } else if (connectionBarCode.connectionType == ConnectionBarCode.CONNECTION_EXTERNAL_AP) {
                setupNetwork(connectionBarCode)
            } else if (connectionBarCode.connectionType == ConnectionBarCode.CONNECTION_VIA_ROUTER) {
                setupNetwork(connectionBarCode)
            }
        } catch (e: Exception) {
            startScanner()
            Toast.makeText(this, "invalid connection code", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun setupNetwork(connectionBarCode: ConnectionBarCode) {
        val networkRequest = NetworkRequest.Builder().apply {
            addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        }.build()
        val networkCallback = object : NetworkCallback() {
            override fun onAvailable(network: Network) {
                ConnectionActivity.network = network
                setResult(RESULT_OK, Intent().apply {
                    putExtra(PARAM_CONNECTION_TYPE, connectionBarCode.connectionType)
                })
                finish()
            }

            override fun onUnavailable() {
                if (connectionBarCode.connectionType == ConnectionBarCode.CONNECTION_EXTERNAL_AP) {
                    Toast.makeText(this@ConnectionActivity, "Please connect your Wi-Fi to group owner Hotspot manually.And then try again.", Toast.LENGTH_LONG).show()
                }
                scanner.startCamera()
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            connectivityManager.requestNetwork(networkRequest, networkCallback, 5000)
        } else {
            connectivityManager.requestNetwork(networkRequest, networkCallback)
            CoroutineScope(Main).launch {
                delay(7000)
                networkCallback.onUnavailable()
            }
        }
    }
}