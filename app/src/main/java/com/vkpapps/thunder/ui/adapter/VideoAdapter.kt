package com.vkpapps.thunder.ui.adapter

import android.content.Intent
import android.media.MediaScannerConnection
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.net.toFile
import androidx.recyclerview.widget.RecyclerView
import com.vkpapps.thunder.R
import com.vkpapps.thunder.analitics.Logger
import com.vkpapps.thunder.model.VideoInfo
import com.vkpapps.thunder.utils.MyThumbnailUtils

/**
 * @author VIJAY PATIDAR
 */
class VideoAdapter(private val videoInfos: List<VideoInfo>?, private val onVideoSelectListener: OnVideoSelectListener) : RecyclerView.Adapter<VideoAdapter.MyHolder>() {
    private val myThumbnailUtils = MyThumbnailUtils
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.video_list_item, parent, false)
        return MyHolder(view)
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        val videoInfo = videoInfos!![position]
        holder.title.text = videoInfo.name
        holder.size.text = videoInfo.displaySize
        holder.btnSelected.isChecked = videoInfo.isSelected
        holder.btnSelected.setOnClickListener {
            videoInfo.isSelected = !videoInfo.isSelected
            holder.btnSelected.isChecked = videoInfo.isSelected
            if (videoInfo.isSelected) {
                onVideoSelectListener.onVideoSelected(videoInfo)
            } else {
                onVideoSelectListener.onVideoDeselected(videoInfo)
            }
        }
        holder.picture.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_VIEW)
                MediaScannerConnection.scanFile(it.context, arrayOf(videoInfo.uri.toFile().absolutePath), null) { _, uri ->
                    run {
                        val type = it.context.contentResolver.getType(uri)
                        Logger.d("file $uri type = $type")
                        intent.setDataAndType(uri, type)
                        it.context.startActivity(intent)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(it.context, "error occurred", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
        holder.itemView.setOnLongClickListener {
            onVideoSelectListener.onVideoLongClickListener(videoInfo)
            true
        }
        myThumbnailUtils.loadVideoThumbnail(videoInfo.id, videoInfo.uri, holder.picture)
    }

    override fun getItemCount(): Int {
        return videoInfos?.size ?: 0
    }

    class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val picture: AppCompatImageView = itemView.findViewById(R.id.picture)
        val btnSelected: RadioButton = itemView.findViewById(R.id.btnSelect)
        val title: AppCompatTextView = itemView.findViewById(R.id.title)
        val size: AppCompatTextView = itemView.findViewById(R.id.size)

    }

    interface OnVideoSelectListener {
        fun onVideoLongClickListener(videoInfo: VideoInfo)
        fun onVideoSelected(videoInfo: VideoInfo)
        fun onVideoDeselected(videoInfo: VideoInfo)
    }

}