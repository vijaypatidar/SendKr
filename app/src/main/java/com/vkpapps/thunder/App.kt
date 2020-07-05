package com.vkpapps.thunder

import android.app.Application
import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.vkpapps.thunder.analitics.Logger
import com.vkpapps.thunder.model.User
import com.vkpapps.thunder.utils.StorageManager
import com.vkpapps.thunder.utils.UserUtils

/**
 * @author VIJAY PATIDAR
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this)
        context = applicationContext
        user = UserUtils(this).loadUser()
        val storageManager = StorageManager(this)
        Thread {
            storageManager.allAudioFromDevice
        }.start()
        storageManager.deleteDir(storageManager.songDir)
        Logger.logger = BuildConfig.DEBUG
    }

    companion object {
        @JvmStatic
        lateinit var user: User

        @JvmStatic
        lateinit var context:Context
    }
}