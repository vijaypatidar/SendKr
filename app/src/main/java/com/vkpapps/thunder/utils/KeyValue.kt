package com.vkpapps.thunder.utils

import android.content.Context
import android.content.SharedPreferences

/***
 * @author VIJAY PATIDAR
 */
class KeyValue(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("mode", Context.MODE_PRIVATE)
    var isDarkMode: Boolean
        get() = sharedPreferences.getBoolean("mode", false)
        set(darkMode) {
            sharedPreferences.edit().putBoolean("mode", darkMode).apply()
        }

    var token: String?
        get() = sharedPreferences.getString("token", null)
        set(token) {
            sharedPreferences.edit().putString("token", token).apply()
        }

    var policy: Boolean
        get() = sharedPreferences.getBoolean("policy", false)
        set(policy) {
            sharedPreferences.edit().putBoolean("policy", policy).apply()

        }

    var externalStoragePath: String?
        get() = sharedPreferences.getString("externalStoragePath", null)
        set(policy) {
            sharedPreferences.edit().putString("externalStoragePath", policy).apply()
        }
}