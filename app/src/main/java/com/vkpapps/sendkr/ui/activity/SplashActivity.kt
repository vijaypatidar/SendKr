package com.vkpapps.sendkr.ui.activity

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.core.app.NotificationManagerCompat
import com.vkpapps.sendkr.App
import com.vkpapps.sendkr.R
import com.vkpapps.sendkr.loader.PrepareDb
import com.vkpapps.sendkr.utils.PermissionUtils
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/***
 * @author VIJAY PATIDAR
 */
class SplashActivity : MyAppCompatActivity() {
    companion object {
        const val NOTIFICATION_CHANNEL_ID = "NOTIFICATION_CHANNEL_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val typeface = Typeface.createFromAsset(assets, "fonts/baloo_tamma.ttf")
        send.typeface = typeface
        kr.typeface = typeface

        CoroutineScope(IO).launch {
            val arrayList = ArrayList<Parcelable>()
            when (intent?.action) {
                Intent.ACTION_SEND -> {
                    (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM))?.let {
                        arrayList.add(it)
                    }
                }
                Intent.ACTION_SEND_MULTIPLE -> {
                    intent.getParcelableArrayListExtra<Parcelable>(Intent.EXTRA_STREAM)?.let { list ->
                        arrayList.addAll(list)
                    }
                }
            }
            loadData(arrayList)
        }
        createNotificationChannel()
    }

    private suspend fun loadData(list: ArrayList<Parcelable>) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("shared", list)
        if (PermissionUtils.checkStoragePermission(this) && !App.databasePrepared) {
            PrepareDb().prepareAll()
            App.databasePrepared = true
        }
        withContext(Main) {
            startActivity(intent)
            finish()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, "General", NotificationManager.IMPORTANCE_DEFAULT)
            NotificationManagerCompat.from(this).createNotificationChannel(channel)
        }

    }
}