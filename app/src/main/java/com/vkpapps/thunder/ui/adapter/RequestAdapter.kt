package com.vkpapps.thunder.ui.adapter

import android.content.Context
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
import com.vkpapps.thunder.model.constaints.FileType
import com.vkpapps.thunder.model.constaints.StatusType
import com.vkpapps.thunder.utils.MathUtils
import java.util.*

class RequestAdapter(context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val requestInfos: MutableList<RequestInfo> = ArrayList()
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItemViewType(position: Int): Int {
        return requestInfos[position].type
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return DefaultRequestHolder(inflater.inflate(R.layout.default_request_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val requestInfo = requestInfos[position]
        if (holder is DefaultRequestHolder) {
            holder.name.text = requestInfo.name
            setIconType(holder.thumbnail, requestInfo.type)
            updateStatus(holder, requestInfo)
        }
    }

    override fun getItemCount(): Int {
        return requestInfos.size
    }

    fun setRequestInfos(requestInfos: List<RequestInfo>) {
        synchronized(this.requestInfos) {
            this.requestInfos.clear()
            this.requestInfos.addAll(requestInfos)
            notifyDataSetChanged()
        }
    }

    private fun updateStatus(holder: DefaultRequestHolder, requestInfo: RequestInfo) {
        when (requestInfo.status) {
            StatusType.STATUS_PENDING -> holder.status.setImageResource(R.drawable.ic_pending)
            StatusType.STATUS_ONGOING -> {
                holder.status.setImageResource(R.drawable.ic_status_ongoing)
                val animation = RotateAnimation(0f, (-360).toFloat(),
                        Animation.RELATIVE_TO_SELF, .5f,
                        Animation.RELATIVE_TO_SELF, .5f)
                animation.duration = 1000
                animation.repeatCount = Animation.INFINITE
                holder.status.animation = animation
                try {
                    val progress = MathUtils.roundTo((requestInfo.transferred * 100 / requestInfo.size).toDouble())
                    holder.progress.text = "${progress}%"
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            StatusType.STATUS_COMPLETED -> {
                holder.progress.text = "100%"
                holder.status.setImageResource(R.drawable.ic_status_completed)
            }
            StatusType.STATUS_FAILED -> holder.status.setImageResource(R.drawable.ic_status_failed)
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
        val progress: AppCompatTextView = itemView.findViewById(R.id.progress)
        val name: AppCompatTextView = itemView.findViewById(R.id.name)
        val thumbnail: AppCompatImageView = itemView.findViewById(R.id.thumbnail)
        val status: AppCompatImageView = itemView.findViewById(R.id.status)
    }


}