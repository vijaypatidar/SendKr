package com.vkpapps.thunder.utils

import android.content.Context
import android.os.Build
import com.vkpapps.thunder.BuildConfig
import com.vkpapps.thunder.analitics.Logger.d
import com.vkpapps.thunder.model.User
import com.vkpapps.thunder.ui.activity.MainActivity.Companion.GSON
import java.io.*
import kotlin.random.Random

/**
 * @author VIJAY PATIDAR
 */
class UserUtils(val context: Context) {
    fun loadUser(): User {
        try {
            val objectInputStream = ObjectInputStream(
                    FileInputStream(
                            File(StorageManager(this.context).userDir, "user")
                    )
            )
            val obj = objectInputStream.readObject()
            objectInputStream.close()
            //return user
            if (obj is String) {
                val user = GSON.fromJson(obj, User::class.java)
                user.appVersion = BuildConfig.VERSION_CODE
                return user
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
        // return default user
        val user = User()
        user.name = Build.MODEL
        user.appVersion = BuildConfig.VERSION_CODE
        user.userId = HashUtils.getHashValue(Random.nextBytes(20))
        d(user.userId)
        setUser(user)
        return user
    }

    fun setUser(user: User) {
        try {
            val file = File(StorageManager(this.context).userDir, "user")
            val outputStream = ObjectOutputStream(FileOutputStream(file))
            outputStream.writeObject(GSON.toJson(user))
            outputStream.flush()
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}