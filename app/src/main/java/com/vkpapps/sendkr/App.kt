package com.vkpapps.sendkr

import android.app.Application
import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.vkpapps.sendkr.analitics.Logger
import com.vkpapps.sendkr.model.User
import com.vkpapps.sendkr.utils.DownloadPathResolver
import com.vkpapps.sendkr.utils.UserUtils
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

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
        downloadPathResolver = DownloadPathResolver(this)
    }

    companion object {
        @JvmStatic
        lateinit var user: User

        @JvmStatic
        lateinit var downloadPathResolver: DownloadPathResolver

        @JvmStatic
        lateinit var context: Context

        @JvmStatic
        var databasePrepared: Boolean = false

        @JvmStatic
        val taskExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    }
}