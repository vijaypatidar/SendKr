package com.vkpapps.thunder.ui.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.net.toFile
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.vkpapps.thunder.R
import com.vkpapps.thunder.analitics.Logger
import com.vkpapps.thunder.model.HistoryInfo
import com.vkpapps.thunder.model.constaints.FileType
import com.vkpapps.thunder.ui.adapter.HistoryAdapter.HistoryViewHolder
import com.vkpapps.thunder.ui.fragments.FileFragment
import com.vkpapps.thunder.utils.MyThumbnailUtils
import com.vkpapps.thunder.utils.StorageManager
import java.io.File
import java.util.*

/**
 * @author VIJAY PATIDAR
 */
class HistoryAdapter(context: Context, private val onHistorySelectListener: OnHistorySelectListener) : RecyclerView.Adapter<HistoryViewHolder>() {
    private val historyInfos: MutableList<HistoryInfo> = ArrayList()
    private val thumbnails: File = StorageManager(context).thumbnails
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
            val ab = AlertDialog.Builder(v.context)
            ab.setTitle("Delete history")
            ab.setMessage("Remove " + historyInfo.name + " from history.")
            ab.create().show()
            true
        }
        if (historyInfo.type == FileType.FILE_TYPE_FOLDER) {
            holder.itemView.setOnClickListener { v: View? ->
                Navigation.findNavController(v!!).navigate(object : NavDirections {
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
        } else {
            holder.itemView.setOnClickListener {
                try {
                    val intent = Intent(Intent.ACTION_VIEW)
                    MediaScannerConnection.scanFile(it.context, arrayOf(historyInfo.uri.toFile().absolutePath), null) { _, uri ->
                        run {
                            val type = it.context.contentResolver.getType(uri)
                            Logger.d("file $uri type = $type")
                            intent.setDataAndType(uri, type)
                            it.context.startActivity(intent)
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(it.context, "file not found", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }
        }
        val icon = File(thumbnails, historyInfo.id)
        try {
            myThumbnailUtils.loadThumbnail(icon, historyInfo.uri.toFile().absolutePath, historyInfo.type, holder.logo)
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
        fun onHistoryDeselected(historyInfo: HistoryInfo)
    }

}