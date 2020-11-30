package com.vkpapps.sendkr.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
import androidx.core.app.ActivityCompat

object PermissionUtils {
    @JvmStatic
    fun checkStoragePermission(context: Context?): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ActivityCompat.checkSelfPermission(context!!, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    @JvmStatic
    fun checkLocationPermission(context: Context?): Boolean {
        return ActivityCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    @JvmStatic
    fun checkLCameraPermission(context: Context?): Boolean {
        return ActivityCompat.checkSelfPermission(context!!, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    @JvmStatic
    fun askStoragePermission(activity: Activity?, code: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity?.startActivityForResult(Intent(ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION),code)
        } else {
            ActivityCompat.requestPermissions(activity!!, arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), code)
        }
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

}