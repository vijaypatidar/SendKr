package com.vkpapps.thunder.utils

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.vkpapps.thunder.App
import com.vkpapps.thunder.analitics.Logger
import com.vkpapps.thunder.interfaces.OnFailureListener
import com.vkpapps.thunder.interfaces.OnSuccessListener
import java.io.File
import java.lang.reflect.Method


object WifiApUtils {
    var ssid: String = "Android_share214"
    var password: String = "asfdgiytv1@3a"
    const val ERROR_ENABLE_GPS_PROVIDER = 0
    const val ERROR_LOCATION_PERMISSION_DENIED = 4
    const val ERROR_DISABLE_HOTSPOT = 1
    const val ERROR_DISABLE_WIFI = 5
    const val ERROR_UNKNOWN = 3

    val wifiManager = App.context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val locationManager = App.context.applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    var reservation: WifiManager.LocalOnlyHotspotReservation? = null


    fun turnOnHotspot(activity: Context, onSuccessListener: OnSuccessListener<String>, onFailureListener: OnFailureListener<Int>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val providerEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            if (PermissionUtils.checkLocationPermission(activity) && providerEnabled && !isWifiApEnabled()) {
                wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "")
                wifiManager.startLocalOnlyHotspot(object : WifiManager.LocalOnlyHotspotCallback() {
                    override fun onStarted(reservation: WifiManager.LocalOnlyHotspotReservation?) {
                        super.onStarted(reservation)
                        WifiApUtils.reservation = reservation
                        try {
                            ssid = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                reservation!!.softApConfiguration.ssid!!
                            } else {
                                reservation!!.wifiConfiguration!!.SSID
                            }
                            password = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                reservation.softApConfiguration.passphrase!!
                            } else {
                                reservation.wifiConfiguration!!.preSharedKey
                            }
                            Logger.d("successfully started ssid = $ssid   password = $password ")
                            onSuccessListener.onSuccess("ap created automatically")
                        } catch (e: Exception) {
                            e.printStackTrace()
                            onFailureListener.onFailure(ERROR_UNKNOWN)
                        }
                    }

                    override fun onStopped() {
                        super.onStopped()
                        Logger.d("onStopped turning off ap")
                    }

                    override fun onFailed(reason: Int) {
                        super.onFailed(reason)
                        onFailureListener.onFailure(if (reason == ERROR_TETHERING_DISALLOWED) ERROR_DISABLE_HOTSPOT else ERROR_UNKNOWN)
                    }

                }, Handler(Looper.getMainLooper()))
            } else if (!providerEnabled) {
                onFailureListener.onFailure(ERROR_ENABLE_GPS_PROVIDER)
            } else if (isWifiApEnabled()) {
                onFailureListener.onFailure(ERROR_DISABLE_HOTSPOT)
            } else {
                onFailureListener.onFailure(ERROR_LOCATION_PERMISSION_DENIED)
            }
        } else {
            try {
                val wifiConfiguration = WifiConfiguration()
                wifiConfiguration.SSID = ssid
                wifiConfiguration.preSharedKey = password
                wifiManager.isWifiEnabled = false
                setWifiApEnabled(wifiConfiguration, true)
                BarCodeUtils().createQR("${ssid}\n${password}", File(StorageManager(App.context).userDir, "code.png").absolutePath)
                onSuccessListener.onSuccess("created")
            } catch (e: Exception) {
                e.printStackTrace()
                onFailureListener.onFailure(0)
            }
        }
    }

    fun disableWifiAp() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                reservation?.close()
            } else {
                setWifiApEnabled(null, false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isWifiApEnabled(): Boolean {
        try {
            val method = WifiManager::class.java.getMethod("isWifiApEnabled")
            return method.invoke(wifiManager) as Boolean
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    @Throws(Exception::class)
    private fun setWifiApEnabled(wifiConfiguration: WifiConfiguration?, enable: Boolean) {
        val method: Method = WifiManager::class.java.getMethod("setWifiApEnabled", WifiConfiguration::class.java, Boolean::class.javaPrimitiveType)
        method.invoke(wifiManager, wifiConfiguration, enable)
    }

    fun getTetheringSettingIntent(): Intent {
        return Intent().apply {
            setClassName("com.android.settings", "com.android.settings.TetherSettings")
        }
    }

}