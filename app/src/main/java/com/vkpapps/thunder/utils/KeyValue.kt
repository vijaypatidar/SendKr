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
            val editor = sharedPreferences.edit()
            editor.putBoolean("mode", darkMode).apply()
        }

    var token: String?
        get() = sharedPreferences.getString("token", null)
        set(token) {
            val editor = sharedPreferences.edit()
            editor.putString("token", token).apply()
        }

    var policy: Boolean
        get() = sharedPreferences.getBoolean("policy", false)
        set(policy) {
            val editor = sharedPreferences.edit()
            editor.putBoolean("policy", policy).apply()
        }
}