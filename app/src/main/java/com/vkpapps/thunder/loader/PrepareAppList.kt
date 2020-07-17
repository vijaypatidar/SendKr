package com.vkpapps.thunder.loader

import android.content.pm.ApplicationInfo
import androidx.documentfile.provider.DocumentFile
import com.vkpapps.thunder.App
import com.vkpapps.thunder.model.AppInfo
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
                        it.applicationInfo.sourceDir,
                        it.applicationInfo.loadIcon(packageManager),
                        it.packageName)

                //check for obb
                try {
                    // check in all storage devices
                    App.context.obbDirs.forEach { obbDir ->
                        val file = File(obbDir.parentFile!!, appInfo.packageName)
                        val obb = DocumentFile.fromFile(file)
                        val listFiles = obb.listFiles()
                        if (listFiles.isNotEmpty()) {
                            appInfo.obbName = listFiles[0].name
                            appInfo.obbSource = listFiles[0].uri.path
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
}