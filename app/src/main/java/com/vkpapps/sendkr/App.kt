package com.vkpapps.sendkr

import android.app.Application
import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.vkpapps.sendkr.analitics.Logger
import com.vkpapps.sendkr.model.User
import com.vkpapps.sendkr.utils.AdsUtils
import com.vkpapps.sendkr.utils.UserUtils


/**
 * @author VIJAY PATIDAR
 */
class App : Application() {


    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this)
        context = applicationContext
        user = UserUtils(this).loadUser()
        Logger.logger = BuildConfig.DEBUG

        loadRemoteConfig()
        isPhone = resources.getBoolean(R.bool.isPhone)

    }

    companion object {
        @JvmStatic
        lateinit var user: User

        @JvmStatic
        lateinit var context: Context

        @JvmStatic
        var databasePrepared: Boolean = false

        @JvmStatic
        var isPhone = true
    }

    private fun loadRemoteConfig() {
        val remote = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(Long.MAX_VALUE)
                .build()
        remote.setConfigSettingsAsync(configSettings)
        remote.setDefaultsAsync(R.xml.remote)
        remote.fetchAndActivate().addOnCompleteListener {
            AdsUtils.load = remote.getBoolean("loadAds")
            Logger.d("[App][loadRemoteConfig] loadAds = ${AdsUtils.load}")
        }
    }
}