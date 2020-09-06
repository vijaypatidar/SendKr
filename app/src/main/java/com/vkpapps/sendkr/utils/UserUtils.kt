package com.vkpapps.sendkr.utils

import android.content.Context
import android.os.Build
import com.vkpapps.sendkr.BuildConfig
import com.vkpapps.sendkr.connection.ClientHelper
import com.vkpapps.sendkr.model.User
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
                            File(StorageManager.userDir, "user")
                    )
            )
            val user = ClientHelper.gson.fromJson(objectInputStream.readObject() as String, User::class.java)
            user.appVersion = BuildConfig.VERSION_CODE
            return user
        } catch (e: Exception) {
            e.printStackTrace()
        }
        // return default user
        val user = User()
        user.name = Build.MODEL
        user.appVersion = BuildConfig.VERSION_CODE
        user.userId = HashUtils.getHashValue(Random.nextBytes(20))
        setUser(user)
        return user
    }

    fun setUser(user: User) {
        try {
            val file = File(StorageManager.userDir, "user")
            val outputStream = ObjectOutputStream(FileOutputStream(file))
            outputStream.writeObject(ClientHelper.gson.toJson(user))
            outputStream.flush()
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}