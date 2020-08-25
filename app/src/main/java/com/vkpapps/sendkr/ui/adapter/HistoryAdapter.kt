package com.vkpapps.sendkr.ui.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.vkpapps.sendkr.R
import com.vkpapps.sendkr.model.HistoryInfo
import com.vkpapps.sendkr.model.constant.FileType
import com.vkpapps.sendkr.ui.adapter.HistoryAdapter.HistoryViewHolder
import com.vkpapps.sendkr.ui.dialog.DialogsUtils
import com.vkpapps.sendkr.ui.fragments.FileFragment
import com.vkpapps.sendkr.utils.MyThumbnailUtils
import com.vkpapps.sendkr.utils.OpenUtils
import java.util.*

/**
 * @author VIJAY PATIDAR
 */
class HistoryAdapter(context: Context, private val onHistorySelectListener: OnHistorySelectListener) : RecyclerView.Adapter<HistoryViewHolder>() {
    private val historyInfos: MutableList<HistoryInfo> = ArrayList()
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val myThumbnailUtils = MyThumbnailUtils

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val inflate = inflater.inflate(R.layout.history_list_item, parent, false)
        return HistoryViewHolder(inflate)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val historyInfo = historyInfos[position]
        holder.name.text = historyInfo.name
        holder.btnSelect.isChecked = historyInfo.isSelected
        holder.btnSelect.setOnClickListener {
            historyInfo.isSelected = !historyInfo.isSelected
            holder.btnSelect.isChecked = historyInfo.isSelected
            if (historyInfo.isSelected) {
                onHistorySelectListener.onHistorySelected(historyInfo)
            } else {
                onHistorySelectListener.onHistoryDeselected(historyInfo)
            }
        }
        holder.itemView.setOnLongClickListener { v: View ->
            DialogsUtils(v.context).clearSelectedHistoryDialog("Remove " + historyInfo.name + " from history.") {
                onHistorySelectListener.onHistoryDeleteRequestSelected(historyInfo)
            }
            true
        }
        holder.itemView.setOnClickListener { v: View ->
            when (historyInfo.type) {
                FileType.FILE_TYPE_FOLDER -> {
                    Navigation.findNavController(v).navigate(object : NavDirections {
                        override fun getActionId(): Int {
                            return R.id.fileFragment
                        }

                        override fun getArguments(): Bundle {
                            val bundle = Bundle()
                            bundle.putString(FileFragment.FRAGMENT_TITLE, historyInfo.name)
                            bundle.putString(FileFragment.FILE_ROOT, historyInfo.uri.toString())
                            return bundle
                        }
                    })
                }
                else -> {
                    OpenUtils.open(historyInfo.type, v.context, historyInfo.uri)
                }
            }
        }

        try {
            myThumbnailUtils.loadThumbnail(historyInfo.id, historyInfo.uri, historyInfo.type, holder.logo)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return historyInfos.size
    }

    fun setHistoryInfos(historyInfos: List<HistoryInfo>) {
        synchronized(this.historyInfos) {
            this.historyInfos.clear()
            this.historyInfos.addAll(historyInfos)
            notifyDataSetChanged()
        }
    }

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView = itemView.findViewById(R.id.name)
        var logo: AppCompatImageView = itemView.findViewById(R.id.icon)
        var btnSelect: RadioButton = itemView.findViewById(R.id.btnSelect)

    }

    interface OnHistorySelectListener {
        fun onHistorySelected(historyInfo: HistoryInfo)
        fun onHistoryDeleteRequestSelected(historyInfo: HistoryInfo)
        fun onHistoryDeselected(historyInfo: HistoryInfo)
    }

}