package com.vkpapps.sendkr.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.vkpapps.sendkr.R
import com.vkpapps.sendkr.interfaces.OnMediaSelectListener
import com.vkpapps.sendkr.model.MediaInfo
import com.vkpapps.sendkr.utils.IntentUtils
import com.vkpapps.sendkr.utils.MyThumbnailUtils

/**
 * @author VIJAY PATIDAR
 */
class PhotoAdapter(private val photoInfos: List<MediaInfo>?, private val onMediaSelectListener: OnMediaSelectListener) : RecyclerView.Adapter<PhotoAdapter.MyHolder>() {
    private val myThumbnailUtils = MyThumbnailUtils
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.photo_list_item, parent, false)
        return MyHolder(view)
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        val photoInfo = photoInfos!![position]
        holder.btnSelected.visibility = if (photoInfo.isSelected) View.VISIBLE else View.GONE
        holder.itemView.setOnClickListener {
            photoInfo.isSelected = !photoInfo.isSelected
            holder.btnSelected.visibility = if (photoInfo.isSelected) View.VISIBLE else View.GONE
            if (photoInfo.isSelected) {
                onMediaSelectListener.onMediaSelected(photoInfo)
            } else {
                onMediaSelectListener.onMediaDeselected(photoInfo)
            }
        }
        holder.itemView.setOnLongClickListener {
            onMediaSelectListener.onMediaLongClickListener(photoInfo)
            true
        }
        myThumbnailUtils.loadPhotoThumbnail(photoInfo.uri, holder.picture)
        holder.btnFullscreen.setOnClickListener {
            IntentUtils.startActionViewIntent(it.context, photoInfo.uri)
        }
    }

    override fun getItemCount(): Int {
        return photoInfos?.size ?: 0
    }

    class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val picture: AppCompatImageView = itemView.findViewById(R.id.picture)
        val btnSelected: AppCompatImageView = itemView.findViewById(R.id.btnSelect)
        val btnFullscreen: AppCompatImageButton = itemView.findViewById(R.id.btnFullscreen)
    }

}