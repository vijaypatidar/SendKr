package com.vkpapps.thunder.utils

import android.app.Activity
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import com.vkpapps.thunder.analitics.Logger
import com.vkpapps.thunder.ui.activity.MainActivity

class WifiUtils {
    fun turnOnHotspot(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (PermissionUtils.checkLocationPermission(activity)) {
                val wifiManager = activity.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                wifiManager.startLocalOnlyHotspot(object : WifiManager.LocalOnlyHotspotCallback() {

                    override fun onStarted(reservation: WifiManager.LocalOnlyHotspotReservation?) {
                        super.onStarted(reservation)
                        Logger.d("successfully started ${reservation?.wifiConfiguration?.SSID}   ${reservation?.wifiConfiguration?.preSharedKey} ")
                    }

                }, Handler())
            } else {
                PermissionUtils.askLocationPermission(activity, MainActivity.ASK_LOCATION_PERMISSION)
            }
        } else {

        }
    }
}