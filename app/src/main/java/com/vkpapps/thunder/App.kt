package com.vkpapps.thunder

import android.app.Application
import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.vkpapps.thunder.analitics.Logger
import com.vkpapps.thunder.model.User
import com.vkpapps.thunder.room.database.MyRoomDatabase
import com.vkpapps.thunder.utils.UserUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
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
        CoroutineScope(IO).launch {
            MyRoomDatabase.getDatabase(this@App).requestDao().deleteAll()
        }
    }

    companion object {
        @JvmStatic
        lateinit var user: User

        @JvmStatic
        lateinit var context: Context

        @JvmStatic
        val executor = Executors.newSingleThreadExecutor()
    }
}