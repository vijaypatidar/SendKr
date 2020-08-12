package com.vkpapps.thunder.ui.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.vkpapps.thunder.R
import com.vkpapps.thunder.model.RequestInfo
import com.vkpapps.thunder.model.constant.FileType
import com.vkpapps.thunder.model.constant.StatusType
import com.vkpapps.thunder.ui.views.HorizontalProgressBar
import com.vkpapps.thunder.utils.MathUtils
import com.vkpapps.thunder.utils.MyThumbnailUtils

class RequestAdapter(context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var requestInfos: List<RequestInfo> = ArrayList()
    private val inflater: LayoutInflater = LayoutInflater.from(context)


    private val animation = RotateAnimation(0f, (-360).toFloat(),
            Animation.RELATIVE_TO_SELF, .5f,
            Animation.RELATIVE_TO_SELF, .5f).apply {
        duration = 1000
        repeatCount = 1
    }

    override fun getItemViewType(position: Int): Int {
        return requestInfos[position].fileType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return DefaultRequestHolder(inflater.inflate(R.layout.default_request_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val requestInfo = requestInfos[position]
        if (holder is DefaultRequestHolder) {
            holder.name.text = requestInfo.name
            holder.size.text = requestInfo.displaySize
            updateStatus(holder, requestInfo)
        }
    }

    override fun getItemCount(): Int {
        return requestInfos.size
    }

    fun setRequestInfos(requestInfos: List<RequestInfo>) {
        this.requestInfos = requestInfos
        notifyDataSetChanged()
    }

    private fun updateStatus(holder: DefaultRequestHolder, requestInfo: RequestInfo) {
        when (requestInfo.status) {
            StatusType.STATUS_PENDING -> {
                holder.status.setImageResource(R.drawable.ic_pending)
                setProgress(holder.progress, 0, requestInfo.size)
                setIconType(holder.thumbnail, requestInfo.fileType)
            }
            StatusType.STATUS_ONGOING -> {
                holder.status.setImageResource(R.drawable.ic_status_ongoing)
                setIconType(holder.thumbnail, requestInfo.fileType)
                holder.status.animation = animation
                setProgress(holder.progress, requestInfo.transferred, requestInfo.size)

            }
            StatusType.STATUS_COMPLETED -> {
                holder.status.setImageResource(R.drawable.ic_status_completed)
                holder.progress.setProgress(100f)
                try {
                    MyThumbnailUtils.loadThumbnail(requestInfo.rid, Uri.parse(requestInfo.uri), requestInfo.fileType, holder.thumbnail)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            StatusType.STATUS_FAILED -> {
                setIconType(holder.thumbnail, requestInfo.fileType)
                setProgress(holder.progress, requestInfo.transferred, requestInfo.size)
                holder.status.setImageResource(R.drawable.ic_status_failed)
            }
            StatusType.STATUS_PAUSE -> {
                setIconType(holder.thumbnail, requestInfo.fileType)
                holder.status.setImageResource(R.drawable.ic_resume)
                setProgress(holder.progress, requestInfo.transferred, requestInfo.size)
            }
        }
    }

    private fun setIconType(thumbnail: AppCompatImageView, type: Int) {
        when (type) {
            FileType.FILE_TYPE_APP -> thumbnail.setImageResource(R.drawable.ic_android)
            FileType.FILE_TYPE_MUSIC -> thumbnail.setImageResource(R.drawable.ic_music)
            FileType.FILE_TYPE_VIDEO -> thumbnail.setImageResource(R.drawable.ic_movie)
            FileType.FILE_TYPE_PHOTO -> thumbnail.setImageResource(R.drawable.ic_photo)
            FileType.FILE_TYPE_FOLDER -> thumbnail.setImageResource(R.drawable.ic_folder)
            else -> thumbnail.setImageResource(R.drawable.ic_file)
        }
    }

    class DefaultRequestHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val progress: HorizontalProgressBar = itemView.findViewById(R.id.progress)
        val name: AppCompatTextView = itemView.findViewById(R.id.name)
        val size: AppCompatTextView = itemView.findViewById(R.id.size)
        val thumbnail: AppCompatImageView = itemView.findViewById(R.id.thumbnail)
        val status: AppCompatImageView = itemView.findViewById(R.id.status)
    }

    private fun setProgress(progressBar: HorizontalProgressBar, transferred: Long, size: Long) {
        try {
            val progress = MathUtils.roundTo((transferred * 100 / size).toDouble())
            progressBar.setProgress(progress.toFloat())
        } catch (e: Exception) {
        }
    }


}