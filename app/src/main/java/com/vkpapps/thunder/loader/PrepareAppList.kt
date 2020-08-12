package com.vkpapps.thunder.loader

import android.content.pm.ApplicationInfo
import android.net.Uri
import androidx.core.net.toFile
import androidx.documentfile.provider.DocumentFile
import com.vkpapps.thunder.App
import com.vkpapps.thunder.model.AppInfo
import com.vkpapps.thunder.utils.MathUtils
import java.io.File

/***
 * @author VIJAY PATIDAR
 */
object PrepareAppList {
    val appList: List<AppInfo> by lazy {
        val appInfos = ArrayList<AppInfo>()
        val packageManager = App.context.packageManager
        val installedApplications = packageManager.getInstalledPackages(0)

        installedApplications.forEach {
            if (it != null && (it.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0) {
                val appInfo = AppInfo(
                        it.applicationInfo.loadLabel(packageManager).toString() + ".apk",
                        Uri.fromFile(File(it.applicationInfo.sourceDir)),
                        it.packageName)

                appInfo.size = MathUtils.longToStringSize(appInfo.uri.toFile().length().toDouble())
                //check for obb
                try {
                    // check in all storage devices
                    App.context.obbDirs.forEach { obbDir ->
                        val file = File(obbDir.parentFile!!, appInfo.packageName)
                        val obb = DocumentFile.fromFile(file)
                        val listFiles = obb.listFiles()
                        if (listFiles.isNotEmpty()) {
                            appInfo.obbName = listFiles[0].name
                            appInfo.obbUri = listFiles[0].uri
                            appInfo.obbSize = MathUtils.longToStringSize(appInfo.obbUri!!.toFile().length().toDouble())
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                appInfos.add(appInfo)
            }
        }

        appInfos.sortBy { appInfo -> appInfo.name }
        appInfos
    }

    val thunder: AppInfo? by lazy {
        var appInfo: AppInfo? = null
        appList.forEach {
            if (it.packageName == App.context.packageName) {
                appInfo = it
            }
        }
        appInfo
    }
}