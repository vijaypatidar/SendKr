package com.vkpapps.thunder.aysnc

import android.content.pm.ApplicationInfo
import android.os.AsyncTask
import com.vkpapps.thunder.App
import com.vkpapps.thunder.model.AppInfo
/***
 * @author VIJAY PATIDAR
 */
class PrepareAppList(private val onAppListPrepareListener: OnAppListPrepareListener) : AsyncTask<Void?, Void?, List<AppInfo>>() {

    override fun doInBackground(vararg params: Void?): List<AppInfo> {
        val appInfos = ArrayList<AppInfo>()
        val packageManager = App.context.packageManager
        val installedApplications = packageManager.getInstalledPackages(0)
        installedApplications.forEach {
            if (it!=null&&(it.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM)==0){
                appInfos.add(AppInfo(it.applicationInfo.loadLabel(packageManager).toString(), it.applicationInfo.sourceDir, it.applicationInfo
                        .loadIcon(packageManager)))
            }
        }
        return appInfos
    }

    override fun onPostExecute(appInfos: List<AppInfo>) {
        super.onPostExecute(appInfos)
        onAppListPrepareListener.onAppListPrepared(appInfos)
    }

    interface OnAppListPrepareListener {
        fun onAppListPrepared(appInfos: List<AppInfo>)
    }

}