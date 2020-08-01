package com.vkpapps.thunder.ui.adapter

import android.content.Intent
import android.media.MediaScannerConnection
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.net.toFile
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.vkpapps.thunder.R
import com.vkpapps.thunder.analitics.Logger
import com.vkpapps.thunder.model.FileInfo
import com.vkpapps.thunder.ui.adapter.FileAdapter.MyViewHolder
import com.vkpapps.thunder.ui.fragments.FileFragment
import com.vkpapps.thunder.utils.MyThumbnailUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

/***
 * @author VIJAY PATIDAR
 */
class FileAdapter(private val onFileSelectListener: OnFileSelectListener, private val view: View) : RecyclerView.Adapter<MyViewHolder>() {
    private val fileInfos: MutableList<FileInfo> = ArrayList()
    private val thumbnailUtils: MyThumbnailUtils = MyThumbnailUtils
    override fun getItemViewType(position: Int): Int {
        return if (fileInfos[position].file.isDirectory) {
            0
        } else {
            1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return if (viewType == 0) {
            MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.file_list_item_folder, parent, false))
        } else {
            MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.file_list_item_file, parent, false))
        }
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val fileInfo = fileInfos[position]
        //common for both type
        holder.name.text = fileInfo.file.name
        if (fileInfo.file.isDirectory) {
            CoroutineScope(IO).launch {
                val text = if (fileInfo.fileCount > 0) fileInfo.fileCount.toString() + " files | ${fileInfo.displaySize}" else "empty folder"
                withContext(Main) {
                    holder.info.text = text
                }
            }
            holder.itemView.setOnClickListener {
                Navigation.findNavController(view).navigate(object : NavDirections {
                    override fun getActionId(): Int {
                        return R.id.action_navigation_files_to_files
                    }

                    override fun getArguments(): Bundle {
                        val bundle = Bundle()
                        bundle.putString(FileFragment.FILE_ROOT, fileInfo.file.uri.toString())
                        bundle.putString(FileFragment.FRAGMENT_TITLE, fileInfo.name)
                        return bundle
                    }
                })
            }
        } else {
            holder.info.text = fileInfo.displaySize
            thumbnailUtils.loadThumbnail(fileInfo.id,
                    fileInfo.uri,
                    fileInfo.type,
                    holder.icon
            )
            holder.itemView.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                MediaScannerConnection.scanFile(it.context, arrayOf(fileInfo.uri.toFile().absolutePath), null) { _, uri ->
                    run {
                        val type = it.context.contentResolver.getType(uri)
                        Logger.d("file $uri type = $type")
                        intent.setDataAndType(uri, type)
                        it.context.startActivity(Intent.createChooser(intent, "Open with"))
                    }
                }
            }
        }
        holder.btnSelected.isChecked = fileInfo.isSelected
        holder.btnSelected.setOnClickListener {
            fileInfo.isSelected = !fileInfo.isSelected
            if (fileInfo.isSelected) {
                onFileSelectListener.onFileSelected(fileInfo)
            } else {
                onFileSelectListener.onFileDeselected(fileInfo)
            }
            holder.btnSelected.isChecked = fileInfo.isSelected
        }
    }

    override fun getItemCount(): Int {
        return fileInfos.size
    }

    fun setFileInfos(fileInfos: List<FileInfo>) {
        synchronized(this.fileInfos) {
            this.fileInfos.clear()
            this.fileInfos.addAll(fileInfos)
            notifyDataSetChanged()
        }
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView = itemView.findViewById(R.id.fileName)
        var info: TextView = itemView.findViewById(R.id.info)
        var icon: AppCompatImageView = itemView.findViewById(R.id.icon)
        var btnSelected: RadioButton = itemView.findViewById(R.id.btnSelect)
    }

    interface OnFileSelectListener {
        fun onFileSelected(fileInfo: FileInfo)
        fun onFileDeselected(fileInfo: FileInfo)
    }

}