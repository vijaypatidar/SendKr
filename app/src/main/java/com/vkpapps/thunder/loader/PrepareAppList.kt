package com.vkpapps.thunder.loader

import android.content.pm.ApplicationInfo
import com.vkpapps.thunder.App
import com.vkpapps.thunder.model.AppInfo

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
                appInfos.add(AppInfo(it.applicationInfo.loadLabel(packageManager).toString() + ".apk", it.applicationInfo.sourceDir, it.applicationInfo
                        .loadIcon(packageManager), it.packageName))
            }
        }
        appInfos.sortBy { appInfo -> appInfo.name }
        appInfos
    }
}