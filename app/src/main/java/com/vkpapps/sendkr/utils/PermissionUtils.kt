package com.vkpapps.sendkr.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat

object PermissionUtils {
    @JvmStatic
    fun checkStoragePermission(context: Context?): Boolean {
        return ActivityCompat.checkSelfPermission(context!!, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    @JvmStatic
    fun checkLocationPermission(context: Context?): Boolean {
        return ActivityCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @JvmStatic
    fun checkWriteSettingPermission(context: Context?): Boolean {
        return Settings.System.canWrite(context)
    }

    @JvmStatic
    fun checkLCameraPermission(context: Context?): Boolean {
        return ActivityCompat.checkSelfPermission(context!!, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
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

    @JvmStatic
    fun askCameraPermission(activity: Activity?, code: Int) {
        ActivityCompat.requestPermissions(activity!!, arrayOf(
                Manifest.permission.CAMERA
        ), code)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @JvmStatic
    fun askWriteSettingPermission(activity: Activity?, code: Int) {
        activity?.run {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            intent.data = Uri.parse("package:${activity.packageName}")
            startActivity(intent)
        }

    }
}