package com.vkpapps.thunder.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.play.core.tasks.OnSuccessListener
import com.vkpapps.thunder.App
import com.vkpapps.thunder.analitics.Logger
import com.vkpapps.thunder.ui.activity.MainActivity
import com.vkpapps.thunder.ui.dialog.DialogsUtils
import java.io.File

object WifiApUtils {
    private val wifiManager = App.context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val locationManager = App.context.applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    var reservation: WifiManager.LocalOnlyHotspotReservation? = null


    fun turnOnHotspot(activity: Activity, onSuccessListener: OnSuccessListener<String>, onFailureListener: OnFailureListener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val providerEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            if (PermissionUtils.checkLocationPermission(activity) && providerEnabled && !isWifiApEnabled()) {
                Logger.d("wifiManager.is5GHzBandSupported ${wifiManager.isWifiEnabled}")
                wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "")

                wifiManager.startLocalOnlyHotspot(object : WifiManager.LocalOnlyHotspotCallback() {

                    override fun onStarted(reservation: WifiManager.LocalOnlyHotspotReservation?) {
                        super.onStarted(reservation)
                        WifiApUtils.reservation = reservation
                        try {
                            val SSID: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                reservation!!.softApConfiguration.ssid!!
                            } else {
                                reservation!!.wifiConfiguration!!.SSID
                            }
                            val password: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                reservation.softApConfiguration.passphrase!!
                            } else {
                                reservation.wifiConfiguration!!.preSharedKey
                            }
                            Logger.d("successfully started ssid = $SSID   password = $password ")
                            BarCodeUtils().createQR("${SSID}\n${password}", File(StorageManager(App.context).userDir, "code.png").absolutePath)
                            onSuccessListener.onSuccess("ap created automatically")
                        } catch (e: Exception) {
                            e.printStackTrace()
                            onFailureListener.onFailure(e)
                        }

                    }

                    override fun onStopped() {
                        super.onStopped()
                        Logger.d("onStopped turning off ap")
                    }

                    override fun onFailed(reason: Int) {
                        super.onFailed(reason)
                        onFailureListener.onFailure(Exception("reason = $reason"))
                    }

                }, Handler(Looper.getMainLooper()))
            } else if (!providerEnabled) {
                onFailureListener.onFailure(Exception("Location provider is disabled required!"))
                DialogsUtils(activity).alertGpsProviderRequire()
            } else if (isWifiApEnabled()) {
                activity.startActivity(Intent().apply {
                    setClassName("com.android.settings", "com.android.settings.TetherSettings")
                })
                onFailureListener.onFailure(Exception("Ap is already created by another process"))
            } else {
                onFailureListener.onFailure(Exception("Location permission required!"))
                PermissionUtils.askLocationPermission(activity, MainActivity.ASK_LOCATION_PERMISSION)
            }
        } else {
            wifiManager.isWifiEnabled = false
            if (!isWifiApEnabled()) {
                DialogsUtils(activity).createHotspot()
                onSuccessListener.onSuccess("s")
            } else if (wifiManager.isWifiEnabled) {
                onFailureListener.onFailure(Exception("Ap is already created by another process"))
            }
        }
    }

    fun disableWifiAp() {
        reservation?.close()
    }

    private fun isWifiApEnabled(): Boolean {
        try {
            val method = WifiManager::class.java.getMethod("isWifiApEnabled")
            return method.invoke(wifiManager) as Boolean
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
}