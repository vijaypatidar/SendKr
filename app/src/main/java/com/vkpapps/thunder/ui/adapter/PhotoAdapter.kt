package com.vkpapps.thunder.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.vkpapps.thunder.R
import com.vkpapps.thunder.model.PhotoInfo
import com.vkpapps.thunder.utils.IntentUtils
import com.vkpapps.thunder.utils.MyThumbnailUtils

/**
 * @author VIJAY PATIDAR
 */
class PhotoAdapter(private val photoInfos: List<PhotoInfo>?, private val onPhotoSelectListener: OnPhotoSelectListener, view: View) : RecyclerView.Adapter<PhotoAdapter.MyHolder>() {
    private val myThumbnailUtils = MyThumbnailUtils
    private val controller: NavController = Navigation.findNavController(view)
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
                onPhotoSelectListener.onPhotoSelected(photoInfo)
            } else {
                onPhotoSelectListener.onPhotoDeselected(photoInfo)
            }
        }
        holder.itemView.setOnLongClickListener {
            onPhotoSelectListener.onPhotoLongClickListener(photoInfo)
            true
        }
        myThumbnailUtils.loadPhotoThumbnail(photoInfo.uri, holder.picture)
        holder.btnFullscreen.setOnClickListener {
            IntentUtils.startIntentToViewImage(it.context, photoInfo.uri)
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

    interface OnPhotoSelectListener {
        fun onPhotoLongClickListener(photoInfo: PhotoInfo)
        fun onPhotoSelected(photoInfo: PhotoInfo)
        fun onPhotoDeselected(photoInfo: PhotoInfo)
    }

}