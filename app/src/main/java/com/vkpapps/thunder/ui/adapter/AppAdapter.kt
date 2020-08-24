package com.vkpapps.thunder.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.net.toFile
import androidx.recyclerview.widget.RecyclerView
import com.vkpapps.thunder.R
import com.vkpapps.thunder.model.AppInfo
import com.vkpapps.thunder.ui.adapter.AppAdapter.AppHolder
import com.vkpapps.thunder.utils.MathUtils
import com.vkpapps.thunder.utils.MyThumbnailUtils.loadApkThumbnail
import java.util.*

/***
 * @author VIJAY PATIDAR
 */
class AppAdapter(appInfos: ArrayList<AppInfo>, onAppSelectListener: OnAppSelectListener) : RecyclerView.Adapter<AppHolder>() {
    private val onAppSelectListener: OnAppSelectListener
    private val appInfos: List<AppInfo>?
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppHolder {
        return AppHolder(LayoutInflater.from(parent.context).inflate(R.layout.app_list_item, parent, false))
    }

    override fun onBindViewHolder(appHolder: AppHolder, position: Int) {
        val appInfo = appInfos!![position]
        appHolder.appTitle.text = appInfo.name
        loadApkThumbnail(appInfo.id, appInfo.uri, appHolder.appIcon)
        appHolder.btnSelected.isChecked = appInfo.isSelected
        appHolder.packageName.text = MathUtils.longToStringSize(appInfo.uri.toFile().length().toDouble())
        appHolder.btnSelected.setOnClickListener { v: View? ->
            appInfo.isSelected = !appInfo.isSelected
            if (appInfo.obbUri != null && appInfo.isSelected != appInfo.isObbSelected) {
                appInfo.isObbSelected = appInfo.isSelected
                appHolder.btnObbSelected.isChecked = appInfo.isObbSelected
                selectionChange(appInfo, appInfo.isObbSelected)
            }
            selectionChange(appInfo, appInfo.isSelected)
            appHolder.btnSelected.isChecked = appInfo.isSelected
        }
        if (appInfo.obbUri != null) {
            appHolder.obb.visibility = View.VISIBLE
            appHolder.obbName.text = appInfo.obbName
            appHolder.obbSize.text = MathUtils.longToStringSize(appInfo.obbUri!!.toFile().length().toDouble())
            appHolder.btnObbSelected.isChecked = appInfo.isObbSelected
            appHolder.btnObbSelected.setOnClickListener { v: View? ->
                appInfo.isObbSelected = !appInfo.isObbSelected
                appHolder.btnObbSelected.isChecked = appInfo.isObbSelected
                selectionChange(appInfo, appInfo.isObbSelected)
            }
        } else {
            appHolder.obb.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return appInfos?.size ?: 0
    }

    private fun selectionChange(appInfo: AppInfo, isSelected: Boolean) {
        if (isSelected) {
            onAppSelectListener.onAppSelected(appInfo)
        } else {
            onAppSelectListener.onAppDeselected(appInfo)
        }
    }

    interface OnAppSelectListener {
        fun onAppSelected(appInfo: AppInfo)
        fun onAppDeselected(appInfo: AppInfo)
    }

    class AppHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var appTitle: TextView
        var packageName: TextView
        var obbName: TextView
        var obbSize: TextView
        var appIcon: AppCompatImageView
        var btnSelected: RadioButton
        var btnObbSelected: RadioButton
        var obb: View

        init {
            appTitle = itemView.findViewById(R.id.appName)
            appIcon = itemView.findViewById(R.id.appIcon)
            btnSelected = itemView.findViewById(R.id.btnSelect)
            btnObbSelected = itemView.findViewById(R.id.btnSelectObb)
            packageName = itemView.findViewById(R.id.packageName)
            obbName = itemView.findViewById(R.id.obbName)
            obbSize = itemView.findViewById(R.id.obbSize)
            obb = itemView.findViewById(R.id.obbContainer)
        }
    }

    init {
        this.appInfos = appInfos
        this.onAppSelectListener = onAppSelectListener
    }
}