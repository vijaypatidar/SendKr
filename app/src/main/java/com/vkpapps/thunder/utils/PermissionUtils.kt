package com.vkpapps.thunder.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

/**
 * @author VIJAY PATIDAR
 */
object PermissionUtils {
    @JvmStatic
    fun checkStoragePermission(context: Context?): Boolean {
        return ActivityCompat.checkSelfPermission(context!!, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    @JvmStatic
    fun checkLocationPermission(context: Context?): Boolean {
        return ActivityCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    @JvmStatic
    fun askStoragePermission(activity: Activity?, code: Int) {
        ActivityCompat.requestPermissions(activity!!, arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        ), code)
    }

    @JvmStatic
    fun askLocationPermission(activity: Activity?, code: Int) {
        ActivityCompat.requestPermissions(activity!!, arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION
        ), code)
    }
}