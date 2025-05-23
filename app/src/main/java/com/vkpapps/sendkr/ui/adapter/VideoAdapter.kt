package com.vkpapps.sendkr.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.vkpapps.sendkr.R
import com.vkpapps.sendkr.interfaces.OnMediaSelectListener
import com.vkpapps.sendkr.model.MediaInfo
import com.vkpapps.sendkr.utils.IntentUtils
import com.vkpapps.sendkr.utils.MathUtils
import com.vkpapps.sendkr.utils.MyThumbnailUtils

/**
 * @author VIJAY PATIDAR
 */
class VideoAdapter(private val videoInfos: List<MediaInfo>?, private val onMediaSelectListener: OnMediaSelectListener) : RecyclerView.Adapter<VideoAdapter.MyHolder>() {
    private val myThumbnailUtils = MyThumbnailUtils
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.video_list_item, parent, false)
        return MyHolder(view)
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        val videoInfo = videoInfos!![position]
        holder.title.text = videoInfo.name
        holder.size.text = MathUtils.longToStringSize(videoInfo.size.toDouble())
        holder.btnSelected.isChecked = videoInfo.isSelected

        val onClick = View.OnClickListener {
            videoInfo.isSelected = !videoInfo.isSelected
            holder.btnSelected.isChecked = videoInfo.isSelected
            if (videoInfo.isSelected) {
                onMediaSelectListener.onMediaSelected(videoInfo)
            } else {
                onMediaSelectListener.onMediaDeselected(videoInfo)
            }
        }
        holder.btnSelected.setOnClickListener(onClick)
        holder.itemView.setOnClickListener(onClick)
        holder.picture.setOnClickListener {
            IntentUtils.startActionViewIntent(it.context, videoInfo.uri)
        }
        holder.itemView.setOnLongClickListener {
            onMediaSelectListener.onMediaLongClickListener(videoInfo)
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
}