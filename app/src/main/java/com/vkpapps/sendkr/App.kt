package com.vkpapps.sendkr

import android.app.Application
import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.vkpapps.sendkr.analitics.Logger
import com.vkpapps.sendkr.model.User
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
    }

    companion object {
        @JvmStatic
        lateinit var user: User

        @JvmStatic
        lateinit var context: Context

        @JvmStatic
        var databasePrepared: Boolean = false

    }
}