package com.vkpapps.thunder.ui.activity


import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.vkpapps.thunder.R
import com.vkpapps.thunder.analitics.Logger
import com.vkpapps.thunder.model.constant.Constants
import com.vkpapps.thunder.ui.dialog.DialogsUtils
import com.vkpapps.thunder.utils.PermissionUtils
import kotlinx.android.synthetic.main.activity_connection.*

class ConnectionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connection)
        window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        initUI()
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (PermissionUtils.checkLCameraPermission(this)) {
            scanner.startCamera()
        } else {
            showCameraPermissionAskDialog()
        }

        scanner.setResultHandler {
            try {
                Logger.d("scan result" + it.text)
                val split = it.text.split("\n")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val networkSpecifier = WifiNetworkSpecifier.Builder()
                            .setSsid(split[0])
                            .setWpa2Passphrase(split[1])
                            .build()
                    val networkRequest = NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                            .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                            .setNetworkSpecifier(networkSpecifier)
                            .build()
                    connectivityManager.requestNetwork(networkRequest, object : ConnectivityManager.NetworkCallback() {
                        override fun onAvailable(network: Network) {
                            super.onAvailable(network)
                            Logger.d("onAvailable")
                            setResult(RESULT_OK)
                            finish()
                        }

                        override fun onLost(network: Network) {
                            super.onLost(network)
                            Logger.d("onLost")
                        }
                    })
                } else {
                    val configuration = WifiConfiguration()
                    configuration.SSID = split[0]
                    configuration.preSharedKey = split[1]
                    val addNetwork = wifiManager.addNetwork(configuration)
                    Toast.makeText(this, "addNetwork = $addNetwork", Toast.LENGTH_SHORT).show()
                    wifiManager.enableNetwork(addNetwork, true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun showCameraPermissionAskDialog() {
        DialogsUtils(this).alertCameraPermissionRequire(View.OnClickListener {
            PermissionUtils.askCameraPermission(this, Constants.CONNECTION_ACTIVITY_ASK_CAMERA_PERMISSION)
        }, View.OnClickListener {
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
    }

    override fun onPause() {
        super.onPause()
        scanner.stopCamera()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Logger.d("onRequestPermissionsResult $requestCode")
        if (requestCode == Constants.CONNECTION_ACTIVITY_ASK_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scanner.startCamera()
                cameraPermissionDenied.visibility = View.GONE
            } else {
                cameraPermissionDenied.visibility = View.VISIBLE
            }
        }
    }
}